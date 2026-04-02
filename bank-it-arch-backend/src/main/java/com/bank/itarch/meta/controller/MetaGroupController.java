package com.bank.itarch.meta.controller;

import com.bank.itarch.common.Result;
import com.bank.itarch.meta.model.dto.MetaGroupDTO;
import com.bank.itarch.meta.model.entity.MetaGroup;
import com.bank.itarch.meta.service.MetaGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字段分组管理控制器
 */
@Tag(name = "字段分组管理")
@RestController
@RequestMapping("/v1/meta/groups")
@RequiredArgsConstructor
public class MetaGroupController {

    private final MetaGroupService metaGroupService;

    @Operation(summary = "创建分组")
    @PostMapping("/{modelId}")
    public Result<MetaGroup> createGroup(
            @Parameter(description = "模型ID") @PathVariable Long modelId,
            @RequestBody MetaGroupDTO dto) {
        MetaGroup group = metaGroupService.createGroup(modelId, dto);
        return Result.success("创建成功", group);
    }

    @Operation(summary = "更新分组")
    @PutMapping("/{modelId}/{groupId}")
    public Result<MetaGroup> updateGroup(
            @Parameter(description = "模型ID") @PathVariable Long modelId,
            @Parameter(description = "分组ID") @PathVariable Long groupId,
            @RequestBody MetaGroupDTO dto) {
        MetaGroup group = metaGroupService.updateGroup(modelId, groupId, dto);
        return Result.success("更新成功", group);
    }

    @Operation(summary = "删除分组")
    @DeleteMapping("/{modelId}/{groupId}")
    public Result<Void> deleteGroup(
            @Parameter(description = "模型ID") @PathVariable Long modelId,
            @Parameter(description = "分组ID") @PathVariable Long groupId) {
        metaGroupService.deleteGroup(modelId, groupId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取模型的所有分组")
    @GetMapping("/model/{modelId}")
    public Result<List<MetaGroup>> getGroupsByModel(
            @Parameter(description = "模型ID") @PathVariable Long modelId) {
        List<MetaGroup> groups = metaGroupService.getGroupsByModel(modelId);
        return Result.success(groups);
    }

    @Operation(summary = "获取模型的分组（含字段）")
    @GetMapping("/model/{modelId}/with-fields")
    public Result<List<MetaGroupDTO>> getGroupsWithFields(
            @Parameter(description = "模型ID") @PathVariable Long modelId) {
        List<MetaGroupDTO> groups = metaGroupService.getGroupsWithFields(modelId);
        return Result.success(groups);
    }
}
