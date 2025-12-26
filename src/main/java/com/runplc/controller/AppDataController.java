package com.runplc.controller;

import com.runplc.entity.AppData;
import com.runplc.service.AppDataService;
import com.runplc.util.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/app-data")
@Api(tags = "应用数据管理接口")
public class AppDataController {

    @Autowired
    private AppDataService appDataService;

    /**
     * 创建记录
     * @param appData 数据对象
     * @return 操作结果
     */
    @PostMapping
    @ApiOperation("创建应用数据记录")
    public AjaxResult create(@RequestBody @ApiParam("应用数据对象") AppData appData) {
        return appDataService.save(appData) ? AjaxResult.success("创建成功") : AjaxResult.error("创建失败");
    }

    /**
     * 根据ID删除记录
     * @param id 记录ID
     * @return 操作结果
     */
    @PostMapping("/delete")
    @ApiOperation("根据ID删除应用数据记录")
    public AjaxResult delete(@RequestParam @ApiParam("数据记录ID") Integer id) {
        return appDataService.deleteById(id) ? AjaxResult.success("删除成功") : AjaxResult.error("删除失败");
    }

    /**
     * 更新记录
     * @param appData 数据对象
     * @return 操作结果
     */
    @PostMapping("/update")
    @ApiOperation("更新应用数据记录")
    public AjaxResult update(@RequestBody @ApiParam("应用数据对象") AppData appData) {
        return appDataService.update(appData) ? AjaxResult.success("更新成功") : AjaxResult.error("更新失败");
    }

    /**
     * 根据ID查询记录
     * @param id 记录ID
     * @return 数据对象
     */
    @GetMapping("/get")
    @ApiOperation("根据ID查询应用数据记录")
    public AjaxResult getById(@RequestParam @ApiParam("数据记录ID") Integer id) {
        AppData appData = appDataService.getById(id);
        return appData != null ? AjaxResult.success("查询成功", appData) : AjaxResult.error("未找到指定记录");
    }

    /**
     * 查询所有记录
     * @return 数据列表
     */
    @GetMapping
    @ApiOperation("查询所有应用数据记录")
    public AjaxResult getAll() {
        List<AppData> appDataList = appDataService.getAll();
        return AjaxResult.success("查询成功", appDataList);
    }

    /**
     * 根据dataKey查询记录
     * @param dataKey 键值
     * @return 数据对象
     */
    @GetMapping("/get-by-key")
    @ApiOperation("根据dataKey查询应用数据记录")
    public AjaxResult getByDataKey(@RequestParam @ApiParam("数据键值") String dataKey) {
        AppData appData = appDataService.getByDataKey(dataKey);
        return appData != null ? AjaxResult.success("查询成功", appData) : AjaxResult.error("未找到指定记录");
    }
}