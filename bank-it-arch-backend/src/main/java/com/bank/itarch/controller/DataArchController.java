package com.bank.itarch.controller;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.model.entity.ArchDataEntity;
import com.bank.itarch.model.entity.ArchDataFlow;
import com.bank.itarch.service.ArchDataEntityService;
import com.bank.itarch.service.ArchDataFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/data")
@RequiredArgsConstructor
@Tag(name = "数据架构管理")
public class DataArchController {

    private final ArchDataEntityService entityService;
    private final ArchDataFlowService flowService;

    @GetMapping("/entities")
    @Operation(summary = "数据实体列表")
    public Result<PageResult<ArchDataEntity>> listEntities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long appId) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        return Result.success(entityService.pageQuery(query, keyword, appId));
    }

    @GetMapping("/entities/{id}")
    @Operation(summary = "数据实体详情")
    public Result<ArchDataEntity> getEntity(@PathVariable Long id) {
        return Result.success(entityService.detail(id));
    }

    @PostMapping("/entities")
    @Operation(summary = "新增数据实体")
    public Result<ArchDataEntity> createEntity(@RequestBody ArchDataEntity entity) {
        return Result.success("创建成功", entityService.create(entity));
    }

    @PutMapping("/entities/{id}")
    @Operation(summary = "编辑数据实体")
    public Result<ArchDataEntity> updateEntity(@PathVariable Long id, @RequestBody ArchDataEntity entity) {
        return Result.success("更新成功", entityService.update(id, entity));
    }

    @DeleteMapping("/entities/{id}")
    @Operation(summary = "删除数据实体")
    public Result<Void> deleteEntity(@PathVariable Long id) {
        entityService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/flows")
    @Operation(summary = "数据流向列表")
    public Result<PageResult<ArchDataFlow>> listFlows(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        return Result.success(flowService.pageQuery(query, keyword));
    }

    @PostMapping("/flows")
    @Operation(summary = "新增数据流向")
    public Result<ArchDataFlow> createFlow(@RequestBody ArchDataFlow entity) {
        return Result.success("创建成功", flowService.create(entity));
    }

    @PutMapping("/flows/{id}")
    @Operation(summary = "编辑数据流向")
    public Result<ArchDataFlow> updateFlow(@PathVariable Long id, @RequestBody ArchDataFlow entity) {
        return Result.success("更新成功", flowService.update(id, entity));
    }

    @DeleteMapping("/flows/{id}")
    @Operation(summary = "删除数据流向")
    public Result<Void> deleteFlow(@PathVariable Long id) {
        flowService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/flows/graph")
    @Operation(summary = "数据流拓扑图")
    public Result<Map<String, Object>> flowGraph() {
        return Result.success(flowService.getFlowGraph());
    }
}
