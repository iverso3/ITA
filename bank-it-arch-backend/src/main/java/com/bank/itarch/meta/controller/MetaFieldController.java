package com.bank.itarch.meta.controller;

import com.bank.itarch.common.Result;
import com.bank.itarch.meta.model.dto.MetaFieldDTO;
import com.bank.itarch.meta.model.entity.MetaField;
import com.bank.itarch.meta.service.MetaFieldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字段定义管理控制器
 */
@Tag(name = "字段定义管理")
@RestController
@RequestMapping("/v1/meta/fields")
@RequiredArgsConstructor
public class MetaFieldController {

    private final MetaFieldService metaFieldService;

    @Operation(summary = "添加字段")
    @PostMapping("/{modelId}")
    public Result<MetaField> addField(
            @Parameter(description = "模型ID") @PathVariable Long modelId,
            @RequestBody MetaFieldDTO dto) {
        MetaField field = metaFieldService.addField(modelId, dto);
        return Result.success("添加成功", field);
    }

    @Operation(summary = "更新字段")
    @PutMapping("/{modelId}/{fieldId}")
    public Result<MetaField> updateField(
            @Parameter(description = "模型ID") @PathVariable Long modelId,
            @Parameter(description = "字段ID") @PathVariable Long fieldId,
            @RequestBody MetaFieldDTO dto) {
        MetaField field = metaFieldService.updateField(modelId, fieldId, dto);
        return Result.success("更新成功", field);
    }

    @Operation(summary = "删除字段")
    @DeleteMapping("/{modelId}/{fieldId}")
    public Result<Void> deleteField(
            @Parameter(description = "模型ID") @PathVariable Long modelId,
            @Parameter(description = "字段ID") @PathVariable Long fieldId) {
        metaFieldService.deleteField(modelId, fieldId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "批量添加字段")
    @PostMapping("/{modelId}/batch")
    public Result<List<MetaField>> batchAddFields(
            @Parameter(description = "模型ID") @PathVariable Long modelId,
            @RequestBody List<MetaFieldDTO> fields) {
        List<MetaField> result = metaFieldService.batchAddFields(modelId, fields);
        return Result.success("批量添加成功", result);
    }

    @Operation(summary = "调整字段顺序")
    @PutMapping("/{modelId}/reorder")
    public Result<Void> reorderFields(
            @Parameter(description = "模型ID") @PathVariable Long modelId,
            @RequestBody List<Long> fieldIds) {
        metaFieldService.reorderFields(modelId, fieldIds);
        return Result.success("排序成功", null);
    }

    @Operation(summary = "获取模型的所有字段")
    @GetMapping("/model/{modelId}")
    public Result<List<MetaField>> getFieldsByModel(
            @Parameter(description = "模型ID") @PathVariable Long modelId) {
        List<MetaField> fields = metaFieldService.getFieldsByModel(modelId);
        return Result.success(fields);
    }

    @Operation(summary = "获取模型的可查询字段")
    @GetMapping("/model/{modelId}/queryable")
    public Result<List<MetaField>> getQueryableFields(
            @Parameter(description = "模型ID") @PathVariable Long modelId) {
        List<MetaField> fields = metaFieldService.getQueryableFields(modelId);
        return Result.success(fields);
    }

    @Operation(summary = "获取模型的敏感字段")
    @GetMapping("/model/{modelId}/sensitive")
    public Result<List<MetaField>> getSensitiveFields(
            @Parameter(description = "模型ID") @PathVariable Long modelId) {
        List<MetaField> fields = metaFieldService.getSensitiveFields(modelId);
        return Result.success(fields);
    }
}
