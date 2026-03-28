package com.bank.itarch.controller;

import com.bank.itarch.common.Result;
import com.bank.itarch.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/report")
@RequiredArgsConstructor
@Tag(name = "报表中心")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/statistics/systems")
    @Operation(summary = "系统统计")
    public Result<Map<String, Object>> systemStatistics() {
        return Result.success(reportService.systemStatistics());
    }

    @GetMapping("/statistics/resources")
    @Operation(summary = "资源统计")
    public Result<Map<String, Object>> resourceStatistics() {
        return Result.success(reportService.resourceStatistics());
    }

    @GetMapping("/statistics/tech-distribution")
    @Operation(summary = "技术分布统计")
    public Result<Map<String, Object>> techDistribution() {
        return Result.success(reportService.techStackDistribution());
    }

    @GetMapping("/statistics/department")
    @Operation(summary = "部门分布统计")
    public Result<Map<String, Object>> departmentStatistics() {
        return Result.success(reportService.departmentStatistics());
    }

    @GetMapping("/panorama/systems")
    @Operation(summary = "系统全景图")
    public Result<Map<String, Object>> systemPanorama() {
        return Result.success(reportService.systemPanorama());
    }

    @GetMapping("/panorama/tech-stacks")
    @Operation(summary = "技术栈全景图")
    public Result<Map<String, Object>> techStackPanorama() {
        return Result.success(reportService.techStackPanorama());
    }

    @GetMapping("/panorama/data-distribution")
    @Operation(summary = "数据分布全景图")
    public Result<Map<String, Object>> dataDistribution() {
        return Result.success(reportService.dataDistribution());
    }

    @GetMapping("/panorama/data-flows")
    @Operation(summary = "数据流全景图")
    public Result<Map<String, Object>> dataFlowPanorama() {
        return Result.success(reportService.dataFlowPanorama());
    }
}
