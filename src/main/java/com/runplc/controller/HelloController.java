package com.runplc.controller;

import com.runplc.util.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "基础接口")
public class HelloController {

    @GetMapping("/hello")
    @ApiOperation("欢迎接口")
    public AjaxResult hello() {
        return AjaxResult.success("Hello, Spring Boot!");
    }
}