package com.bank.itarch.meta.controller;

import com.bank.itarch.common.Result;
import com.bank.itarch.meta.model.dto.MetaGraphDTO;
import com.bank.itarch.meta.service.MetaGraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 拓扑查询控制器
 */
@Tag(name = "拓扑查询")
@RestController
@RequestMapping("/v1/meta/graph")
@RequiredArgsConstructor
public class MetaGraphController {

    private final MetaGraphService metaGraphService;

    @Operation(summary = "获取实体拓扑图")
    @GetMapping("/entity/{entityId}")
    public Result<MetaGraphDTO> getEntityGraph(
            @Parameter(description = "实体ID") @PathVariable Long entityId,
            @Parameter(description = "深度") @RequestParam(required = false, defaultValue = "1") Integer depth) {
        MetaGraphDTO graph = metaGraphService.getEntityGraph(entityId, depth);
        return Result.success(graph);
    }

    @Operation(summary = "获取模型拓扑图")
    @GetMapping("/model/{modelId}")
    public Result<MetaGraphDTO> getModelGraph(
            @Parameter(description = "模型ID") @PathVariable Long modelId) {
        MetaGraphDTO graph = metaGraphService.getModelGraph(modelId, null);
        return Result.success(graph);
    }

    @Operation(summary = "递归查询实体关系")
    @GetMapping("/recursive/{entityId}")
    public Result<List<Object>> recursiveQuery(
            @Parameter(description = "实体ID") @PathVariable Long entityId,
            @Parameter(description = "关系编码") @RequestParam String relCode,
            @Parameter(description = "最大深度") @RequestParam(required = false, defaultValue = "3") Integer maxDepth) {
        List<Object> result = metaGraphService.recursiveQuery(entityId, relCode, maxDepth);
        return Result.success(result);
    }

    @Operation(summary = "查询最短路径")
    @GetMapping("/shortest-path")
    public Result<List<Object>> shortestPath(
            @Parameter(description = "源实体ID") @RequestParam Long sourceEntityId,
            @Parameter(description = "目标实体ID") @RequestParam Long targetEntityId) {
        List<Object> path = metaGraphService.shortestPath(sourceEntityId, targetEntityId);
        return Result.success(path);
    }

    @Operation(summary = "批量查询实体关系")
    @PostMapping("/batch")
    public Result<MetaGraphDTO> batchQueryGraph(
            @RequestBody List<Long> entityIds,
            @RequestParam(required = false, defaultValue = "1") Integer depth) {
        MetaGraphDTO graph = metaGraphService.batchQueryGraph(entityIds, depth);
        return Result.success(graph);
    }

    @Operation(summary = "检测环")
    @GetMapping("/cycle/{modelId}")
    public Result<Boolean> detectCycle(
            @Parameter(description = "模型ID") @PathVariable Long modelId) {
        boolean hasCycle = metaGraphService.detectCycle(modelId);
        return Result.success(hasCycle);
    }

    @Operation(summary = "获取孤立实体")
    @GetMapping("/isolated/{modelId}")
    public Result<List<Object>> getIsolatedEntities(
            @Parameter(description = "模型ID") @PathVariable Long modelId) {
        List<Object> isolated = metaGraphService.getIsolatedEntities(modelId);
        return Result.success(isolated);
    }

    @Operation(summary = "统计关系数量")
    @GetMapping("/stats/{modelId}")
    public Result<Map<String, Long>> countRelationsByType(
            @Parameter(description = "模型ID") @PathVariable Long modelId) {
        Map<String, Long> stats = metaGraphService.countRelationsByType(modelId);
        return Result.success(stats);
    }
}
