package com.bank.itarch.controller;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.model.entity.OssFlowRole;
import com.bank.itarch.service.OssFlowRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/flow-role")
@RequiredArgsConstructor
@Tag(name = "流程角色管理")
public class OssFlowRoleController {

    private final OssFlowRoleService flowRoleService;

    @GetMapping
    @Operation(summary = "流程角色列表")
    public Result<PageResult<OssFlowRole>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String flowRoleName,
            @RequestParam(required = false) String flowRoleDesc) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        return Result.success(flowRoleService.pageQuery(query, flowRoleName, flowRoleDesc));
    }

    @GetMapping("/all")
    @Operation(summary = "流程角色全部列表")
    public Result<List<OssFlowRole>> listAll() {
        return Result.success(flowRoleService.listAll());
    }

    @GetMapping("/{flowRoleId}")
    @Operation(summary = "流程角色详情")
    public Result<OssFlowRole> getById(@PathVariable String flowRoleId) {
        return Result.success(flowRoleService.detail(flowRoleId));
    }

    @PostMapping
    @Operation(summary = "新增流程角色")
    public Result<OssFlowRole> create(@RequestBody OssFlowRole entity) {
        return Result.success("创建成功", flowRoleService.create(entity));
    }

    @PutMapping("/{flowRoleId}")
    @Operation(summary = "编辑流程角色")
    public Result<OssFlowRole> update(@PathVariable String flowRoleId, @RequestBody OssFlowRole entity) {
        return Result.success("更新成功", flowRoleService.update(flowRoleId, entity));
    }

    @DeleteMapping("/{flowRoleId}")
    @Operation(summary = "删除流程角色")
    public Result<Void> delete(@PathVariable String flowRoleId) {
        flowRoleService.delete(flowRoleId);
        return Result.success("删除成功", null);
    }
}