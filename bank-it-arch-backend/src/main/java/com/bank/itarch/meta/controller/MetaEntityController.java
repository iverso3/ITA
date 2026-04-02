package com.bank.itarch.meta.controller;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.meta.model.dto.MetaEntityDTO;
import com.bank.itarch.meta.model.dto.MetaRelationDTO;
import com.bank.itarch.meta.model.entity.MetaEntity;
import com.bank.itarch.meta.service.MetaEntityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 实体管理控制器
 */
@Tag(name = "实体管理")
@RestController
@RequestMapping("/v1/meta/entities")
@RequiredArgsConstructor
public class MetaEntityController {

    private final MetaEntityService metaEntityService;

    @Operation(summary = "创建实体")
    @PostMapping
    public Result<MetaEntity> createEntity(@RequestBody MetaEntityDTO dto) {
        MetaEntity entity = metaEntityService.createEntity(dto);
        return Result.success("创建成功", entity);
    }

    @Operation(summary = "批量创建实体")
    @PostMapping("/batch/{modelId}")
    public Result<List<MetaEntity>> batchCreate(
            @Parameter(description = "模型ID") @PathVariable Long modelId,
            @RequestBody List<MetaEntityDTO> entities) {
        List<MetaEntity> result = metaEntityService.batchCreate(modelId, entities);
        return Result.success("批量创建成功", result);
    }

    @Operation(summary = "更新实体")
    @PutMapping("/{id}")
    public Result<MetaEntity> updateEntity(
            @Parameter(description = "实体ID") @PathVariable Long id,
            @RequestBody MetaEntityDTO dto) {
        MetaEntity entity = metaEntityService.updateEntity(id, dto);
        return Result.success("更新成功", entity);
    }

    @Operation(summary = "删除实体")
    @DeleteMapping("/{id}")
    public Result<Void> deleteEntity(
            @Parameter(description = "实体ID") @PathVariable Long id) {
        metaEntityService.deleteEntity(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "批量删除实体")
    @DeleteMapping("/batch/{modelId}")
    public Result<Void> batchDelete(
            @Parameter(description = "模型ID") @PathVariable Long modelId,
            @RequestBody List<Long> ids) {
        metaEntityService.batchDelete(modelId, ids);
        return Result.success("批量删除成功", null);
    }

    @Operation(summary = "分页查询实体")
    @GetMapping("/page/{modelId}")
    public Result<PageResult<MetaEntity>> pageQuery(
            @Parameter(description = "模型ID") @PathVariable Long modelId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        PageResult<MetaEntity> result = metaEntityService.pageQuery(query, modelId, keyword, null);
        return Result.success(result);
    }

    @Operation(summary = "获取实体详情")
    @GetMapping("/{id}")
    public Result<MetaEntityDTO> getEntityDetail(
            @Parameter(description = "实体ID") @PathVariable Long id) {
        MetaEntityDTO detail = metaEntityService.getEntityDetail(id);
        return Result.success(detail);
    }

    @Operation(summary = "通过业务ID获取实体")
    @GetMapping("/business/{modelId}/{businessId}")
    public Result<MetaEntity> getByBusinessId(
            @Parameter(description = "模型ID") @PathVariable Long modelId,
            @Parameter(description = "业务表主键") @PathVariable Long businessId) {
        MetaEntity entity = metaEntityService.getByBusinessId(modelId, businessId);
        return Result.success(entity);
    }

    // ========== 关系管理 ==========

    @Operation(summary = "绑定关系")
    @PostMapping("/{sourceEntityId}/relations/{relCode}/{targetEntityId}")
    public Result<Void> bindRelation(
            @Parameter(description = "源实体ID") @PathVariable Long sourceEntityId,
            @Parameter(description = "关系编码") @PathVariable String relCode,
            @Parameter(description = "目标实体ID") @PathVariable Long targetEntityId) {
        metaEntityService.bindRelation(sourceEntityId, relCode, targetEntityId);
        return Result.success("绑定成功", null);
    }

    @Operation(summary = "解绑关系")
    @DeleteMapping("/{sourceEntityId}/relations/{relCode}/{targetEntityId}")
    public Result<Void> unbindRelation(
            @Parameter(description = "源实体ID") @PathVariable Long sourceEntityId,
            @Parameter(description = "关系编码") @PathVariable String relCode,
            @Parameter(description = "目标实体ID") @PathVariable Long targetEntityId) {
        metaEntityService.unbindRelation(sourceEntityId, relCode, targetEntityId);
        return Result.success("解绑成功", null);
    }

    @Operation(summary = "获取实体所有关系")
    @GetMapping("/{entityId}/relations")
    public Result<List<MetaRelationDTO>> getEntityRelations(
            @Parameter(description = "实体ID") @PathVariable Long entityId) {
        List<MetaRelationDTO> relations = metaEntityService.getEntityRelations(entityId);
        return Result.success(relations);
    }

    @Operation(summary = "获取实体的关联实体列表")
    @GetMapping("/{entityId}/relations/{relCode}")
    public Result<List<MetaEntity>> getRelatedEntities(
            @Parameter(description = "实体ID") @PathVariable Long entityId,
            @Parameter(description = "关系编码") @PathVariable String relCode) {
        List<MetaEntity> entities = metaEntityService.getRelatedEntities(entityId, relCode);
        return Result.success(entities);
    }

    // ========== 字段值管理 ==========

    @Operation(summary = "设置字段值")
    @PutMapping("/{entityId}/fields/{fieldCode}")
    public Result<Void> setFieldValue(
            @Parameter(description = "实体ID") @PathVariable Long entityId,
            @Parameter(description = "字段编码") @PathVariable String fieldCode,
            @RequestBody Map<String, Object> value) {
        metaEntityService.setFieldValue(entityId, fieldCode, value.get("value"));
        return Result.success("设置成功", null);
    }

    @Operation(summary = "批量设置字段值")
    @PutMapping("/{entityId}/fields")
    public Result<Void> setFieldValues(
            @Parameter(description = "实体ID") @PathVariable Long entityId,
            @RequestBody Map<String, Object> values) {
        metaEntityService.setFieldValues(entityId, values);
        return Result.success("设置成功", null);
    }

    @Operation(summary = "获取所有字段值")
    @GetMapping("/{entityId}/fields")
    public Result<Map<String, Object>> getAllFieldValues(
            @Parameter(description = "实体ID") @PathVariable Long entityId) {
        Map<String, Object> values = metaEntityService.getAllFieldValues(entityId);
        return Result.success(values);
    }
}
