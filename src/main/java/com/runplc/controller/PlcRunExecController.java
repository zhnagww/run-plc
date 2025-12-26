package com.runplc.controller;

import com.runplc.entity.PlcAddr;
import com.runplc.entity.PlcInfo;
import com.runplc.entity.PlcRunDetail;
import com.runplc.modbus.PlcService;
import com.runplc.service.PlcAddrService;
import com.runplc.service.PlcInfoService;
import com.runplc.service.PlcRunDetailService;
import com.runplc.util.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api/plc-run-exec")
@Api(tags = "组合执行接口")
public class PlcRunExecController {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final ConcurrentHashMap<String, ExecSession> SESSIONS = new ConcurrentHashMap<>();

    @Autowired
    private PlcRunDetailService plcRunDetailService;

    @Autowired
    private PlcAddrService plcAddrService;

    @Autowired
    private PlcInfoService plcInfoService;

    @Autowired
    private PlcService plcService;

    @GetMapping(value = "/execute", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiOperation("执行组合动作（SSE日志流式输出）")
    public SseEmitter execute(
            @RequestParam @ApiParam("组合ID") Integer plcRunId,
            @RequestParam @ApiParam("循环次数") Integer loopCount,
            @RequestParam(required = false) @ApiParam("运行ID（可选）") String runId
    ) {
        String actualRunId = (runId == null || runId.trim().isEmpty()) ? UUID.randomUUID().toString() : runId.trim();
        SseEmitter emitter = new SseEmitter(0L);
        ExecSession session = new ExecSession(actualRunId, emitter);

        ExecSession existed = SESSIONS.putIfAbsent(actualRunId, session);
        if (existed != null) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("success", false);
                payload.put("message", "runId已存在，请刷新后重试");
                payload.put("runId", actualRunId);
                emitter.send(SseEmitter.event().name("status").data(payload));
            } catch (IOException ignored) {
            } finally {
                emitter.complete();
            }
            return emitter;
        }

        emitter.onCompletion(() -> cleanup(actualRunId));
        emitter.onTimeout(() -> cleanup(actualRunId));
        emitter.onError(e -> cleanup(actualRunId));

        Future<?> f = EXECUTOR.submit(() -> run(actualRunId, plcRunId, loopCount));
        session.future = f;

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("runId", actualRunId);
            emitter.send(SseEmitter.event().name("run").data(payload));
        } catch (IOException ignored) {
        }

        return emitter;
    }

    @PostMapping("/stop")
    @ApiOperation("停止执行")
    public AjaxResult stop(@RequestParam @ApiParam("运行ID") String runId) {
        if (runId == null || runId.trim().isEmpty()) {
            return AjaxResult.error(508, "runId不能为空");
        }
        ExecSession session = SESSIONS.get(runId.trim());
        if (session == null) {
            return AjaxResult.error(508, "运行不存在或已结束");
        }
        session.cancelled.set(true);
        Future<?> f = session.future;
        if (f != null) {
            f.cancel(true);
        }
        safeLog(runId.trim(), "收到停止指令，正在停止...");
        return AjaxResult.success("停止指令已发送");
    }

    private void run(String runId, Integer plcRunId, Integer loopCount) {
        ExecSession session = SESSIONS.get(runId);
        if (session == null) {
            return;
        }

        if (plcRunId == null) {
            finish(runId, false, "plcRunId不能为空");
            return;
        }
        if (loopCount == null || loopCount <= 0) {
            finish(runId, false, "循环次数必须大于0");
            return;
        }

        List<PlcRunDetail> details = plcRunDetailService.getByPlcRunId(plcRunId);
        if (details == null || details.isEmpty()) {
            finish(runId, false, "该组合没有步骤");
            return;
        }

        safeLog(runId, "开始执行，组合ID=" + plcRunId + "，循环次数=" + loopCount);

        for (int loop = 1; loop <= loopCount; loop++) {
            if (isCancelled(runId)) {
                finish(runId, false, "已停止");
                return;
            }

            safeLog(runId, "==== 循环 " + loop + "/" + loopCount + " 开始 ====");

            Set<Integer> clearAddrIds = new LinkedHashSet<>();
            for (PlcRunDetail d : details) {
                if (d == null) {
                    continue;
                }
                PlcAddr addr = plcAddrService.getById(d.getPlcAddrId());
                if (addr != null && addr.getType() != null && addr.getType() == 1) {
                    if (addr.getId() != null) {
                        clearAddrIds.add(addr.getId());
                    }
                }
            }

            for (PlcRunDetail d : details) {
                if (d == null) {
                    continue;
                }
                PlcAddr addr = plcAddrService.getById(d.getPlcAddrId());
                if (addr == null || addr.getId() == null || addr.getType() == null) {
                    finish(runId, false, "步骤点位不存在，plcAddrId=" + d.getPlcAddrId());
                    return;
                }

                if (isCancelled(runId)) {
                    finish(runId, false, "已停止");
                    return;
                }

                if (addr.getType() == 1 && clearAddrIds.contains(addr.getId())) {
                    boolean ok = writeValue(runId, d, addr, 0);
                    if (!ok) {
                        if (isCancelled(runId)) {
                            finish(runId, false, "已停止");
                        } else {
                            finish(runId, false, "清0失败");
                        }
                        return;
                    }
                    clearAddrIds.remove(addr.getId());
                }
            }

            safeLog(runId, "清0完成");

            for (PlcRunDetail d : details) {
                if (d == null) {
                    continue;
                }

                if (isCancelled(runId)) {
                    finish(runId, false, "已停止");
                    return;
                }

                PlcAddr addr = plcAddrService.getById(d.getPlcAddrId());
                if (addr == null || addr.getType() == null) {
                    finish(runId, false, "步骤点位不存在，plcAddrId=" + d.getPlcAddrId());
                    return;
                }

                Integer sortNo = d.getSortNo();
                int plcValue = d.getPlcValue() == null ? 0 : d.getPlcValue();
                int timeoutSecond = d.getTimeOutSecond() == null ? 60 : d.getTimeOutSecond();

                if (addr.getType() == 1) {
                    safeLog(runId, "步骤 sort_no=" + sortNo + " 写入，点位=" + addr.getPlcNo() + "，值=" + plcValue);
                    boolean ok = writeValue(runId, d, addr, plcValue);
                    if (!ok) {
                        if (isCancelled(runId)) {
                            finish(runId, false, "已停止");
                        } else {
                            finish(runId, false, "写入失败");
                        }
                        return;
                    }
                    safeLog(runId, "写入成功");
                } else if (addr.getType() == 2) {
                    safeLog(runId, "步骤 sort_no=" + sortNo + " 读取，点位=" + addr.getPlcNo() + "，期望值=" + plcValue + "，超时=" + timeoutSecond + "s");
                    ReadResult rr = readUntilMatch(runId, addr, plcValue, timeoutSecond);
                    if (!rr.success) {
                        finish(runId, false, rr.message);
                        return;
                    }
                    safeLog(runId, "读取成功，值=" + rr.value);
                } else {
                    finish(runId, false, "未知点位类型 type=" + addr.getType() + " plcAddrId=" + d.getPlcAddrId());
                    return;
                }
            }

            safeLog(runId, "==== 循环 " + loop + "/" + loopCount + " 完成 ====");
        }

        finish(runId, true, "全部执行完成");
    }

    private boolean writeValue(String runId, PlcRunDetail d, PlcAddr addr, int value) {
        PlcInfo plcInfo = plcInfoService.getById(addr.getPlcInfoId());
        if (plcInfo == null) {
            safeLog(runId, "PLC信息不存在，plcInfoId=" + addr.getPlcInfoId());
            return false;
        }

        if (isCancelled(runId)) {
            return false;
        }

        boolean ok;
        try {
            ok = plcService.writePLC(plcInfo.getIpAddr(), plcInfo.getPortNo(), addr.getPlcNo(), (short) value);
        } catch (Exception e) {
            ok = false;
        }

        if (!ok) {
            safeLog(runId, "写入返回false，sort_no=" + d.getSortNo() + " 点位=" + addr.getPlcNo() + " 值=" + value + " PLC=" + plcInfo.getIpAddr() + ":" + plcInfo.getPortNo());
        }

        return ok;
    }

    private ReadResult readUntilMatch(String runId, PlcAddr addr, int targetValue, int timeoutSecond) {
        PlcInfo plcInfo = plcInfoService.getById(addr.getPlcInfoId());
        if (plcInfo == null) {
            return ReadResult.fail("PLC信息不存在，plcInfoId=" + addr.getPlcInfoId());
        }

        int value;
        for (int i = 0; i < timeoutSecond; i++) {
            if (isCancelled(runId)) {
                return ReadResult.fail("已停止");
            }

            try {
                value = plcService.readPLC(plcInfo.getIpAddr(), plcInfo.getPortNo(), addr.getPlcNo());
            } catch (Exception e) {
                value = 9999;
            }

            if (value == targetValue) {
                return ReadResult.ok(value);
            }

            if (value != 0) {
                if (value == 9999) {
                    return ReadResult.fail("读取失败: 可能PLC连不上 ip:" + plcInfo.getIpAddr() + " port:" + plcInfo.getPortNo());
                }
                return ReadResult.fail("读取错误：点位=" + addr.getPlcNo() + " 期望值=" + targetValue + " 实际值=" + value);
            }

            safeLog(runId, "读取中... 点位=" + addr.getPlcNo() + " 值=0 剩余超时=" + (timeoutSecond - i - 1) + "s");

            if (sleepOneSecond(runId)) {
                return ReadResult.fail("已停止");
            }
        }

        return ReadResult.fail("读取超时：点位=" + addr.getPlcNo() + " 期望值=" + targetValue + " 超时=" + timeoutSecond + "s");
    }

    private boolean sleepOneSecond(String runId) {
        try {
            Thread.sleep(1000);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
    }

    private boolean isCancelled(String runId) {
        ExecSession session = SESSIONS.get(runId);
        return session == null || session.cancelled.get() || Thread.currentThread().isInterrupted();
    }

    private void safeLog(String runId, String msg) {
        ExecSession session = SESSIONS.get(runId);
        if (session == null) {
            return;
        }
        try {
            session.emitter.send(SseEmitter.event().name("log").data(msg));
        } catch (Exception e) {
            cleanup(runId);
        }
    }

    private void finish(String runId, boolean success, String message) {
        ExecSession session = SESSIONS.get(runId);
        if (session == null) {
            return;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("success", success);
            payload.put("message", message);
            payload.put("runId", runId);
            session.emitter.send(SseEmitter.event().name("status").data(payload));
        } catch (IOException ignored) {
        } finally {
            try {
                session.emitter.complete();
            } catch (Exception ignored) {
            }
            cleanup(runId);
        }
    }

    private void cleanup(String runId) {
        ExecSession session = SESSIONS.remove(runId);
        if (session == null) {
            return;
        }
        session.cancelled.set(true);
        Future<?> f = session.future;
        if (f != null) {
            f.cancel(true);
        }
    }

    private static class ExecSession {
        private final SseEmitter emitter;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private volatile Future<?> future;

        private ExecSession(String runId, SseEmitter emitter) {
            this.emitter = emitter;
        }
    }

    private static class ReadResult {
        private final boolean success;
        private final int value;
        private final String message;

        private ReadResult(boolean success, int value, String message) {
            this.success = success;
            this.value = value;
            this.message = message;
        }

        private static ReadResult ok(int value) {
            return new ReadResult(true, value, null);
        }

        private static ReadResult fail(String message) {
            return new ReadResult(false, 0, message);
        }
    }
}
