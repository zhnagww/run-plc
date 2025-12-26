package com.runplc.controller;

import com.runplc.entity.PlcAddr;
import com.runplc.entity.PlcInfo;
import com.runplc.modbus.PlcService;
import com.runplc.service.PlcAddrService;
import com.runplc.service.PlcInfoService;
import com.runplc.util.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/plc-debug")
@Api(tags = "PLC调试接口")
public class PlcDebugController {

    @Autowired
    private PlcAddrService plcAddrService;
    @Autowired
    private PlcService plcService;
    @Autowired
    private PlcInfoService plcInfoService;

    /**
     * 写入PLC值
     * @param debugRequest 调试请求对象，包含plcAddrId和value
     * @return 操作结果
     */
    @PostMapping("/write")
    @ApiOperation("写入PLC值")
    public AjaxResult writePlcValue(@RequestBody @ApiParam("调试请求对象") DebugRequest debugRequest) {
        // 获取PLC地址信息
        PlcAddr plcAddr = plcAddrService.getById(debugRequest.getPlcAddrId());
        if (plcAddr == null) {
            return AjaxResult.error(508, "PLC地址信息不存在");
        }
        // 获取PLC信息
        PlcInfo plcInfo = plcInfoService.getById(plcAddr.getPlcInfoId());
        // 模拟PLC写入操作
        // 在实际应用中，这里应该连接到真实的PLC设备并执行写入操作
        //boolean success = simulatePlcWrite(plcAddr, debugRequest.getValue());
        boolean success = plcService.writePLC(plcInfo.getIpAddr(), plcInfo.getPortNo(), plcAddr.getPlcNo(), Short.valueOf(String.valueOf(debugRequest.getValue())));
        if (success) {
            return AjaxResult.success("写入成功");
        } else {
            return AjaxResult.error("读取失败: 可能PLC连不上 ip:"+plcInfo.getIpAddr()+" port:"+plcInfo.getPortNo());
        }
    }

    /**
     * 读取PLC值
     * @param plcAddrId PLC地址ID
     * @return 读取到的值
     */
    @GetMapping("/read")
    @ApiOperation("读取PLC值")
    public AjaxResult readPlcValue(@RequestParam @ApiParam("PLC地址ID") Integer plcAddrId) {
        // 获取PLC地址信息
        PlcAddr plcAddr = plcAddrService.getById(plcAddrId);
        if (plcAddr == null) {
            return AjaxResult.error(508, "PLC地址信息不存在");
        }
        // 获取PLC信息
        PlcInfo plcInfo = plcInfoService.getById(plcAddr.getPlcInfoId());
        Integer value = plcService.readPLC(plcInfo.getIpAddr(), plcInfo.getPortNo(), plcAddr.getPlcNo());
        // 模拟PLC读取操作
        // 在实际应用中，这里应该连接到真实的PLC设备并执行读取操作
        //Integer value = simulatePlcRead(plcAddr);
        if (value != null && value!=9999) {
            Map<String, Object> result = new HashMap<>();
            result.put("value", value);
            return AjaxResult.success("读取成功", result);
        } else {
            return AjaxResult.error("读取失败: 可能PLC连不上 ip:"+plcInfo.getIpAddr()+" port:"+plcInfo.getPortNo());
        }
    }

    /**
     * 模拟PLC写入操作
     * 在实际应用中，应替换为真实的PLC通信代码
     * @param plcAddr PLC地址信息
     * @param value 要写入的值
     * @return 是否写入成功
     */
    private boolean simulatePlcWrite(PlcAddr plcAddr, Integer value) {
        // 这里应该实现真实的PLC写入逻辑
        // 为演示目的，我们只是简单地返回true
        System.out.println("模拟写入PLC: 地址=" + plcAddr.getPlcNo() + ", 值=" + value);
        return true;
    }

    /**
     * 模拟PLC读取操作
     * 在实际应用中，应替换为真实的PLC通信代码
     * @param plcAddr PLC地址信息
     * @return 读取到的值
     */
    private Integer simulatePlcRead(PlcAddr plcAddr) {
        // 这里应该实现真实的PLC读取逻辑
        // 为演示目的，我们返回一个随机值
        System.out.println("模拟读取PLC: 地址=" + plcAddr.getPlcNo());
        return (int) (Math.random() * 100); // 返回0-99之间的随机数
    }

    /**
     * 调试请求对象
     */
    public static class DebugRequest {
        private Integer plcAddrId;
        private Integer value;

        public Integer getPlcAddrId() {
            return plcAddrId;
        }

        public void setPlcAddrId(Integer plcAddrId) {
            this.plcAddrId = plcAddrId;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }
}