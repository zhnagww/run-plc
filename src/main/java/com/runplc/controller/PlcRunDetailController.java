package com.runplc.controller;

import com.runplc.entity.PlcRunDetail;
import com.runplc.service.PlcRunDetailService;
import com.runplc.util.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plc-run-detail")
@Api(tags = "PLC运行详情管理接口")
public class PlcRunDetailController {

    @Autowired
    private PlcRunDetailService plcRunDetailService;

    /**
     * 创建PLC运行详情记录
     * @param plcRunDetail PLC运行详情对象
     * @return 操作结果
     */
    @PostMapping
    @ApiOperation("创建PLC运行详情记录")
    public AjaxResult create(@RequestBody @ApiParam("PLC运行详情对象") PlcRunDetail plcRunDetail) {
        return plcRunDetailService.save(plcRunDetail) ? AjaxResult.success("创建成功") : AjaxResult.error("创建失败");
    }

    /**
     * 更新PLC运行详情记录
     * @param plcRunDetail PLC运行详情对象
     * @return 操作结果
     */
    @PostMapping("/update")
    @ApiOperation("更新PLC运行详情记录")
    public AjaxResult update(@RequestBody @ApiParam("PLC运行详情对象") PlcRunDetail plcRunDetail) {
        return plcRunDetailService.update(plcRunDetail) ? AjaxResult.success("更新成功") : AjaxResult.error("更新失败");
    }

    /**
     * 只更新sort_no字段
     * @param plcRunDetail 包含ID和sortNo的PLC运行详情对象
     * @return 操作结果
     */
    @PostMapping("/update-sort-no")
    @ApiOperation("只更新sort_no字段")
    public AjaxResult updateSortNo(@RequestBody @ApiParam("包含ID和sortNo的PLC运行详情对象") PlcRunDetail plcRunDetail) {
        return plcRunDetailService.updateSortNo(plcRunDetail.getId(), plcRunDetail.getSortNo()) ? AjaxResult.success("更新成功") : AjaxResult.error("更新失败");
    }

    /**
     * 根据ID删除PLC运行详情记录
     * @param id 记录ID
     * @return 操作结果
     */
    @PostMapping("/delete")
    @ApiOperation("根据ID删除PLC运行详情记录")
    public AjaxResult delete(@RequestParam @ApiParam("PLC运行详情记录ID") Integer id) {
        return plcRunDetailService.deleteById(id) ? AjaxResult.success("删除成功") : AjaxResult.error("删除失败");
    }

    /**
     * 根据plc_run_id查询PLC运行详情记录
     * @param plcRunId plc_run表的ID
     * @return PLC运行详情列表
     */
    @GetMapping("/get-by-plc-run-id")
    @ApiOperation("根据plc_run_id查询PLC运行详情记录")
    public AjaxResult getByPlcRunId(@RequestParam @ApiParam("plc_run表的ID") Integer plcRunId) {
        List<PlcRunDetail> plcRunDetailList = plcRunDetailService.getByPlcRunId(plcRunId);
        return AjaxResult.success("查询成功", plcRunDetailList);
    }

    /**
     * 根据ID查询PLC运行详情记录
     * @param id 记录ID
     * @return PLC运行详情对象
     */
    @GetMapping("/get")
    @ApiOperation("根据ID查询PLC运行详情记录")
    public AjaxResult getById(@RequestParam @ApiParam("PLC运行详情记录ID") Integer id) {
        PlcRunDetail plcRunDetail = plcRunDetailService.getById(id);
        return plcRunDetail != null ? AjaxResult.success("查询成功", plcRunDetail) : AjaxResult.error("未找到指定记录");
    }
}