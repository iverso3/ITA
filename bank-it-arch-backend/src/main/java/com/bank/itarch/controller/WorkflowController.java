package com.bank.itarch.controller;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.model.entity.*;
import com.bank.itarch.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/wf")
@RequiredArgsConstructor
@Tag(name = "审批流程管理")
public class WorkflowController {

    private final WfDefinitionService definitionService;
    private final WfInstanceService instanceService;
    private final WfTaskService taskService;
    private final WfHistoryService historyService;

    @GetMapping("/definitions")
    @Operation(summary = "流程定义列表")
    public Result<PageResult<WfDefinition>> listDefinitions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        return Result.success(definitionService.pageQuery(query, keyword));
    }

    @GetMapping("/definitions/{id}")
    @Operation(summary = "流程定义详情")
    public Result<WfDefinition> getDefinition(@PathVariable Long id) {
        return Result.success(definitionService.getById(id));
    }

    @PostMapping("/definitions")
    @Operation(summary = "新建流程定义")
    public Result<WfDefinition> createDefinition(@RequestBody WfDefinition entity) {
        return Result.success("创建成功", definitionService.create(entity));
    }

    @PutMapping("/definitions/{id}")
    @Operation(summary = "编辑流程定义")
    public Result<WfDefinition> updateDefinition(@PathVariable Long id, @RequestBody WfDefinition entity) {
        return Result.success("更新成功", definitionService.update(id, entity));
    }

    @PatchMapping("/definitions/{id}/status")
    @Operation(summary = "启用/停用流程定义")
    public Result<Void> toggleDefinitionStatus(@PathVariable Long id) {
        definitionService.toggleStatus(id);
        return Result.success("操作成功", null);
    }

    @GetMapping("/instances")
    @Operation(summary = "流程实例列表")
    public Result<PageResult<WfInstance>> listInstances(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String status) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        return Result.success(instanceService.pageQuery(query, status));
    }

    @GetMapping("/instances/{id}")
    @Operation(summary = "流程实例详情")
    public Result<WfInstance> getInstance(@PathVariable Long id) {
        return Result.success(instanceService.detail(id));
    }

    @GetMapping("/instances/{id}/history")
    @Operation(summary = "审批历史")
    public Result<List<WfHistory>> getHistory(@PathVariable Long id) {
        return Result.success(historyService.listByInstanceId(id));
    }

    @GetMapping("/tasks/todo")
    @Operation(summary = "待办任务")
    public Result<PageResult<WfTask>> todoList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        return Result.success(taskService.todoList(query));
    }

    @GetMapping("/tasks/done")
    @Operation(summary = "已办任务")
    public Result<PageResult<WfTask>> doneList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        return Result.success(taskService.doneList(query));
    }

    @PostMapping("/tasks/{id}/approve")
    @Operation(summary = "审批通过")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        taskService.approve(id, body.get("opinion"));
        return Result.success("审批通过", null);
    }

    @PostMapping("/tasks/{id}/reject")
    @Operation(summary = "审批拒绝")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        taskService.reject(id, body.get("opinion"));
        return Result.success("审批拒绝", null);
    }
}
