package com.runplc.controller;

import com.runplc.entity.PlcAddr;
import com.runplc.service.PlcAddrService;
import com.runplc.service.PlcInfoService;
import com.runplc.util.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plc-addr")
@Api(tags = "PLC地址管理接口")
public class PlcAddrController {

    @Autowired
    private PlcAddrService plcAddrService;
    
    @Autowired
    private PlcInfoService plcInfoService;

    /**
     * 创建PLC地址记录
     * @param plcAddr PLC地址对象
     * @return 操作结果
     */
    @PostMapping
    @ApiOperation("创建PLC地址记录")
    public AjaxResult create(@RequestBody @ApiParam("PLC地址对象") PlcAddr plcAddr) {
        // 检查PLC点位编号是否为空或非数字
        if (plcAddr.getPlcNo() == null) {
            return AjaxResult.error(508, "PLC点位编号不能为空");
        }

        if (plcAddr.getPlcInfoId() == null) {
            return AjaxResult.error(508, "PLC信息ID不能为空");
        }

        if (plcAddr.getName() == null || plcAddr.getName().trim().isEmpty()) {
            return AjaxResult.error(508, "名称不能为空");
        }
        
        // 检查关联的plc_info是否存在
        if (plcInfoService.getById(plcAddr.getPlcInfoId()) == null) {
            return AjaxResult.error(508, "关联的PLC信息不存在");
        }

        PlcAddr existed = plcAddrService.getByPlcInfoIdAndPlcNo(plcAddr.getPlcInfoId(), plcAddr.getPlcNo());
        if (existed != null) {
            return AjaxResult.error(508, "同一PLC下点位编号不能重复");
        }

        try {
            return plcAddrService.save(plcAddr) ? AjaxResult.success("创建成功") : AjaxResult.error("创建失败");
        } catch (Exception e) {
            return AjaxResult.error(508, "创建失败：同一PLC下点位编号不能重复");
        }
    }

    /**
     * 根据ID删除PLC地址记录
     * @param id 记录ID
     * @return 操作结果
     */
    @PostMapping("/delete")
    @ApiOperation("根据ID删除PLC地址记录")
    public AjaxResult delete(@RequestParam @ApiParam("PLC地址记录ID") Integer id) {
        return plcAddrService.deleteById(id) ? AjaxResult.success("删除成功") : AjaxResult.error("删除失败");
    }

    /**
     * 更新PLC地址记录
     * @param plcAddr PLC地址对象
     * @return 操作结果
     */
    @PostMapping("/update")
    @ApiOperation("更新PLC地址记录")
    public AjaxResult update(@RequestBody @ApiParam("PLC地址对象") PlcAddr plcAddr) {
        if (plcAddr.getId() == null) {
            return AjaxResult.error(508, "ID不能为空");
        }
        // 检查PLC点位编号是否为空或非数字
        if (plcAddr.getPlcNo() == null) {
            return AjaxResult.error(508, "PLC点位编号不能为空");
        }

        if (plcAddr.getPlcInfoId() == null) {
            return AjaxResult.error(508, "PLC信息ID不能为空");
        }

        if (plcAddr.getName() == null || plcAddr.getName().trim().isEmpty()) {
            return AjaxResult.error(508, "名称不能为空");
        }
        
        // 检查关联的plc_info是否存在
        if (plcInfoService.getById(plcAddr.getPlcInfoId()) == null) {
            return AjaxResult.error(508, "关联的PLC信息不存在");
        }

        PlcAddr existed = plcAddrService.getByPlcInfoIdAndPlcNo(plcAddr.getPlcInfoId(), plcAddr.getPlcNo());
        if (existed != null && !existed.getId().equals(plcAddr.getId())) {
            return AjaxResult.error(508, "同一PLC下点位编号不能重复");
        }
        
        try {
            return plcAddrService.update(plcAddr) ? AjaxResult.success("更新成功") : AjaxResult.error("更新失败");
        } catch (Exception e) {
            return AjaxResult.error(508, "更新失败：同一PLC下点位编号不能重复");
        }
    }

    /**
     * 根据ID查询PLC地址记录
     * @param id 记录ID
     * @return PLC地址对象
     */
    @GetMapping("/get")
    @ApiOperation("根据ID查询PLC地址记录")
    public AjaxResult getById(@RequestParam @ApiParam("PLC地址记录ID") Integer id) {
        PlcAddr plcAddr = plcAddrService.getById(id);
        return plcAddr != null ? AjaxResult.success("查询成功", plcAddr) : AjaxResult.error("未找到指定记录");
    }

    /**
     * 查询所有PLC地址记录
     * @return PLC地址列表
     */
    @GetMapping
    @ApiOperation("查询所有PLC地址记录")
    public AjaxResult getAll() {
        List<PlcAddr> plcAddrList = plcAddrService.getAll();
        return AjaxResult.success("查询成功", plcAddrList);
    }
    
    /**
     * 根据plc_info_id查询PLC地址记录
     * @param plcInfoId plc_info表的ID
     * @return PLC地址列表
     */
    @GetMapping("/get-by-plc-info-id")
    @ApiOperation("根据plc_info_id查询PLC地址记录")
    public AjaxResult getByPlcInfoId(@RequestParam @ApiParam("PLC信息ID") Integer plcInfoId) {
        List<PlcAddr> plcAddrList = plcAddrService.getByPlcInfoId(plcInfoId);
        return AjaxResult.success("查询成功", plcAddrList);
    }
}