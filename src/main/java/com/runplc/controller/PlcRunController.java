package com.runplc.controller;

import com.runplc.entity.PlcRun;
import com.runplc.entity.PlcRunDetail;
import com.runplc.service.PlcRunService;
import com.runplc.service.PlcRunDetailService;
import com.runplc.util.AjaxResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plc-run")
@Api(tags = "PLC运行管理接口")
public class PlcRunController {

    @Autowired
    private PlcRunService plcRunService;
    
    @Autowired
    private PlcRunDetailService plcRunDetailService;

    /**
     * 创建PLC运行记录
     * @param req 创建请求（plcAddrIds 仅用于生成 plc_run_detail，不再存入 plc_run）
     * @return 操作结果
     */
    @PostMapping
    @ApiOperation("创建PLC运行记录")
    public AjaxResult create(@RequestBody @ApiParam("创建请求") PlcRunCreateRequest req) {
        if (req == null || req.getName() == null || req.getName().trim().isEmpty()) {
            return AjaxResult.error(508, "组合名称不能为空");
        }

        String name = req.getName().trim();
        PlcRun existed = plcRunService.getByName(name);
        if (existed != null) {
            return AjaxResult.error(508, "组合名称不能重复");
        }

        PlcRun plcRun = new PlcRun();
        plcRun.setName(name);

        boolean success;
        try {
            success = plcRunService.save(plcRun);
        } catch (Exception e) {
            return AjaxResult.error(508, "创建失败：组合名称不能重复");
        }
        if (success) {
            if (plcRun.getId() != null && req.getPlcAddrIds() != null && !req.getPlcAddrIds().isEmpty()) {
                String[] addrIds = req.getPlcAddrIds().split(",");
                for (int i = 0; i < addrIds.length; i++) {
                    try {
                        PlcRunDetail detail = new PlcRunDetail();
                        detail.setPlcRunId(plcRun.getId());
                        detail.setPlcAddrId(Integer.parseInt(addrIds[i]));
                        detail.setSortNo(i + 1);
                        // 使用默认值: plcValue = 0, timeOutSecond = 60
                        detail.setPlcValue(0);
                        detail.setTimeOutSecond(60);
                        plcRunDetailService.save(detail);
                    } catch (NumberFormatException e) {
                        // 忽略无效的ID
                    }
                }
            }
            return AjaxResult.success("创建成功");
        } else {
            return AjaxResult.error("创建失败");
        }
    }

    /**
     * 根据ID删除PLC运行记录
     * @param id 记录ID
     * @return 操作结果
     */
    @PostMapping("/delete")
    @ApiOperation("根据ID删除PLC运行记录")
    public AjaxResult delete(@RequestParam @ApiParam("PLC运行记录ID") Integer id) {
        // 先删除关联的plc_run_detail记录
        plcRunDetailService.deleteByPlcRunId(id);
        // 再删除主记录
        return plcRunService.deleteById(id) ? AjaxResult.success("删除成功") : AjaxResult.error("删除失败");
    }

    /**
     * 更新PLC运行记录
     * @param plcRun PLC运行对象
     * @return 操作结果
     */
    @PostMapping("/update")
    @ApiOperation("更新PLC运行记录")
    public AjaxResult update(@RequestBody @ApiParam("PLC运行对象") PlcRun plcRun) {
        if (plcRun == null || plcRun.getId() == null) {
            return AjaxResult.error(508, "ID不能为空");
        }

        if (plcRun.getName() == null || plcRun.getName().trim().isEmpty()) {
            return AjaxResult.error(508, "组合名称不能为空");
        }

        String name = plcRun.getName().trim();
        PlcRun existed = plcRunService.getByName(name);
        if (existed != null && existed.getId() != null && !existed.getId().equals(plcRun.getId())) {
            return AjaxResult.error(508, "组合名称不能重复");
        }

        plcRun.setName(name);
        try {
            return plcRunService.update(plcRun) ? AjaxResult.success("更新成功") : AjaxResult.error("更新失败");
        } catch (Exception e) {
            return AjaxResult.error(508, "更新失败：组合名称不能重复");
        }
    }

    /**
     * 根据ID查询PLC运行记录
     * @param id 记录ID
     * @return PLC运行对象
     */
    @GetMapping("/get")
    @ApiOperation("根据ID查询PLC运行记录")
    public AjaxResult getById(@RequestParam @ApiParam("PLC运行记录ID") Integer id) {
        PlcRun plcRun = plcRunService.getById(id);
        return plcRun != null ? AjaxResult.success("查询成功", plcRun) : AjaxResult.error("未找到指定记录");
    }

    /**
     * 查询所有PLC运行记录
     * @return PLC运行列表
     */
    @GetMapping
    @ApiOperation("查询所有PLC运行记录")
    public AjaxResult getAll() {
        List<PlcRun> plcRunList = plcRunService.getAll();
        return AjaxResult.success("查询成功", plcRunList);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlcRunCreateRequest {
        private String name;
        private String plcAddrIds;
    }
}