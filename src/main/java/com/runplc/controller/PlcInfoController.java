package com.runplc.controller;

import com.runplc.entity.PlcInfo;
import com.runplc.service.PlcInfoService;
import com.runplc.util.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/plc-info")
@Api(tags = "PLC信息管理接口")
public class PlcInfoController {

    @Autowired
    private PlcInfoService plcInfoService;
    
    // IP地址正则表达式
    private static final Pattern IP_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    /**
     * 创建PLC信息记录
     * @param plcInfo PLC信息对象
     * @return 操作结果
     */
    @PostMapping
    @ApiOperation("创建PLC信息记录")
    public AjaxResult create(@RequestBody @ApiParam("PLC信息对象") PlcInfo plcInfo) {
        // 验证IP地址格式
        if (!isValidIpAddress(plcInfo.getIpAddr())) {
            return AjaxResult.error(508, "IP地址格式不正确");
        }

        if (plcInfo.getPortNo() == null) {
            return AjaxResult.error(508, "端口号不能为空");
        }
        
        // 检查IP地址是否已存在
        PlcInfo existingPlc = plcInfoService.getByIpAddrAndPortNo(plcInfo.getIpAddr(), plcInfo.getPortNo());
        if (existingPlc != null) {
            return AjaxResult.error(508, "该PLC已存在");
        }
        
        return plcInfoService.save(plcInfo) ? AjaxResult.success("创建成功") : AjaxResult.error("创建失败");
    }

    /**
     * 根据ID删除PLC信息记录
     * @param id 记录ID
     * @return 操作结果
     */
    @PostMapping("/delete")
    @ApiOperation("根据ID删除PLC信息记录")
    public AjaxResult delete(@RequestParam @ApiParam("PLC信息记录ID") Integer id) {
        return plcInfoService.deleteById(id) ? AjaxResult.success("删除成功") : AjaxResult.error("删除失败");
    }

    /**
     * 更新PLC信息记录
     * @param plcInfo PLC信息对象
     * @return 操作结果
     */
    @PostMapping("/update")
    @ApiOperation("更新PLC信息记录")
    public AjaxResult update(@RequestBody @ApiParam("PLC信息对象") PlcInfo plcInfo) {
        // 验证IP地址格式
        if (!isValidIpAddress(plcInfo.getIpAddr())) {
            return AjaxResult.error(508, "IP地址格式不正确");
        }

        if (plcInfo.getPortNo() == null) {
            return AjaxResult.error(508, "端口号不能为空");
        }
        
        // 检查是否存在具有相同IP地址但不同ID的记录
        PlcInfo existingPlc = plcInfoService.getByIpAddrAndPortNo(plcInfo.getIpAddr(), plcInfo.getPortNo());
        if (existingPlc != null && !existingPlc.getId().equals(plcInfo.getId())) {
            return AjaxResult.error(508, "该PLC已存在");
        }
        
        return plcInfoService.update(plcInfo) ? AjaxResult.success("更新成功") : AjaxResult.error("更新失败");
    }

    /**
     * 根据ID查询PLC信息记录
     * @param id 记录ID
     * @return PLC信息对象
     */
    @GetMapping("/get")
    @ApiOperation("根据ID查询PLC信息记录")
    public AjaxResult getById(@RequestParam @ApiParam("PLC信息记录ID") Integer id) {
        PlcInfo plcInfo = plcInfoService.getById(id);
        return plcInfo != null ? AjaxResult.success("查询成功", plcInfo) : AjaxResult.error("未找到指定记录");
    }

    /**
     * 查询所有PLC信息记录
     * @return PLC信息列表
     */
    @GetMapping
    @ApiOperation("查询所有PLC信息记录")
    public AjaxResult getAll() {
        List<PlcInfo> plcInfoList = plcInfoService.getAll();
        return AjaxResult.success("查询成功", plcInfoList);
    }
    
    /**
     * 验证IP地址格式是否正确
     * @param ip IP地址
     * @return 是否有效
     */
    private boolean isValidIpAddress(String ip) {
        return ip != null && IP_PATTERN.matcher(ip).matches();
    }
}