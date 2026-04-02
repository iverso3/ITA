package com.bank.itarch.controller;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.model.entity.OssFlowRoleUserRel;
import com.bank.itarch.service.OssFlowRoleUserRelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/flow-role-user-rel")
@RequiredArgsConstructor
@Tag(name = "用户流程角色管理")
public class OssFlowRoleUserRelController {

    private final OssFlowRoleUserRelService userRelService;

    @GetMapping
    @Operation(summary = "用户流程角色列表")
    public Result<PageResult<OssFlowRoleUserRel>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String flowRoleName,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String orgId,
            @RequestParam(required = false) String updateUserId) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        return Result.success(userRelService.pageQuery(query, userName, flowRoleName, branchId, teamId, orgId, updateUserId));
    }

    @GetMapping("/{relId}")
    @Operation(summary = "用户流程角色详情")
    public Result<OssFlowRoleUserRel> getById(@PathVariable String relId) {
        return Result.success(userRelService.detail(relId));
    }

    @PostMapping
    @Operation(summary = "新增用户流程角色")
    public Result<OssFlowRoleUserRel> create(@RequestBody OssFlowRoleUserRel entity) {
        return Result.success("创建成功", userRelService.create(entity));
    }

    @GetMapping("/check")
    @Operation(summary = "校验用户角色是否已存在")
    public Result<Boolean> checkExists(@RequestParam String userId, @RequestParam String flowRoleId) {
        boolean exists = userRelService.existsByUserIdAndFlowRoleId(userId, flowRoleId);
        return Result.success(exists);
    }

    @PutMapping("/{relId}")
    @Operation(summary = "编辑用户流程角色")
    public Result<OssFlowRoleUserRel> update(@PathVariable String relId, @RequestBody OssFlowRoleUserRel entity) {
        return Result.success("更新成功", userRelService.update(relId, entity));
    }

    @DeleteMapping("/{relId}")
    @Operation(summary = "删除用户流程角色")
    public Result<Void> delete(@PathVariable String relId) {
        userRelService.delete(relId);
        return Result.success("删除成功", null);
    }
}