package com.bank.itarch.meta.controller;

import com.bank.itarch.common.Result;
import com.bank.itarch.meta.model.dto.MetaModelVersionDTO;
import com.bank.itarch.meta.model.dto.MetaPublishRecordDTO;
import com.bank.itarch.meta.model.entity.MetaModelVersion;
import com.bank.itarch.meta.service.MetaPublishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 发布管理控制器
 */
@Tag(name = "发布管理")
@RestController
@RequestMapping("/v1/meta/publish")
@RequiredArgsConstructor
public class MetaPublishController {

    private final MetaPublishService metaPublishService;

    @Operation(summary = "提交发布")
    @PostMapping("/submit/{modelId}")
    public Result<MetaModelVersion> submitPublish(
            @Parameter(description = "模型ID") @PathVariable Long modelId) {
        MetaModelVersion version = metaPublishService.submitPublish(modelId);
        return Result.success("提交发布成功", version);
    }

    @Operation(summary = "审核发布")
    @PostMapping("/approve/{versionId}")
    public Result<Void> approvePublish(
            @Parameter(description = "版本ID") @PathVariable Long versionId,
            @RequestParam(required = false) String approveComment) {
        metaPublishService.approvePublish(versionId, approveComment);
        return Result.success("审核通过", null);
    }

    @Operation(summary = "执行发布")
    @PostMapping("/execute/{versionId}")
    public Result<MetaPublishRecordDTO> executePublish(
            @Parameter(description = "版本ID") @PathVariable Long versionId) {
        MetaPublishRecordDTO record = metaPublishService.executePublish(versionId);
        return Result.success("发布成功", record);
    }

    @Operation(summary = "回滚发布")
    @PostMapping("/rollback/{recordId}")
    public Result<Void> rollbackPublish(
            @Parameter(description = "发布记录ID") @PathVariable Long recordId) {
        metaPublishService.rollbackPublish(recordId);
        return Result.success("回滚成功", null);
    }

    @Operation(summary = "归档版本")
    @PostMapping("/archive/{versionId}")
    public Result<Void> archiveVersion(
            @Parameter(description = "版本ID") @PathVariable Long versionId) {
        metaPublishService.archiveVersion(versionId);
        return Result.success("归档成功", null);
    }

    @Operation(summary = "回滚到指定版本")
    @PostMapping("/rollback/{modelId}/{targetVersion}")
    public Result<MetaPublishRecordDTO> rollbackToVersion(
            @Parameter(description = "模型ID") @PathVariable Long modelId,
            @Parameter(description = "目标版本") @PathVariable Integer targetVersion) {
        MetaPublishRecordDTO record = metaPublishService.rollbackToVersion(modelId, targetVersion);
        return Result.success("回滚成功", record);
    }

    @Operation(summary = "获取版本历史")
    @GetMapping("/history/{modelId}")
    public Result<List<MetaModelVersion>> getVersionHistory(
            @Parameter(description = "模型ID") @PathVariable Long modelId) {
        List<MetaModelVersion> history = metaPublishService.getVersionHistory(modelId);
        return Result.success(history);
    }

    @Operation(summary = "获取当前生效版本")
    @GetMapping("/active/{modelId}")
    public Result<MetaModelVersion> getActiveVersion(
            @Parameter(description = "模型ID") @PathVariable Long modelId) {
        MetaModelVersion version = metaPublishService.getActiveVersion(modelId);
        return Result.success(version);
    }

    @Operation(summary = "比较两个版本")
    @GetMapping("/compare/{versionId1}/{versionId2}")
    public Result<Object> compareVersions(
            @Parameter(description = "版本ID1") @PathVariable Long versionId1,
            @Parameter(description = "版本ID2") @PathVariable Long versionId2) {
        Object diff = metaPublishService.compareVersions(versionId1, versionId2);
        return Result.success(diff);
    }
}
