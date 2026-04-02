package com.bank.itarch.meta.controller;

import com.bank.itarch.common.Result;
import com.bank.itarch.meta.model.dto.MetaRelationshipDTO;
import com.bank.itarch.meta.model.entity.MetaRelationship;
import com.bank.itarch.meta.service.MetaModelService;
import com.bank.itarch.meta.mapper.MetaRelationshipMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 关系定义管理控制器
 */
@Tag(name = "关系定义管理")
@RestController
@RequestMapping("/v1/meta/relationships")
@RequiredArgsConstructor
public class MetaRelationshipController {

    private final MetaRelationshipMapper metaRelationshipMapper;
    private final MetaModelService metaModelService;

    @Operation(summary = "创建关系定义")
    @PostMapping
    public Result<MetaRelationship> createRelationship(@RequestBody MetaRelationshipDTO dto) {
        MetaRelationship relationship = new MetaRelationship();
        relationship.setRelCode(dto.getRelCode());
        relationship.setRelName(dto.getRelName());
        relationship.setRelType(dto.getRelType());
        relationship.setSourceModelId(dto.getSourceModelId());
        relationship.setSourceFieldId(dto.getSourceFieldId());
        relationship.setTargetModelId(dto.getTargetModelId());
        relationship.setTargetFieldId(dto.getTargetFieldId());
        relationship.setIsCascadeDelete(dto.getIsCascadeDelete());
        relationship.setIsSelfRel(dto.getIsSelfRel());
        relationship.setRelConfig(dto.getRelConfig());
        relationship.setDescription(dto.getDescription());
        relationship.setIsActive(true);

        metaRelationshipMapper.insert(relationship);
        return Result.success("创建成功", relationship);
    }

    @Operation(summary = "更新关系定义")
    @PutMapping("/{id}")
    public Result<MetaRelationship> updateRelationship(
            @Parameter(description = "关系ID") @PathVariable Long id,
            @RequestBody MetaRelationshipDTO dto) {
        MetaRelationship relationship = metaRelationshipMapper.selectById(id);
        if (relationship == null) {
            return Result.error(404, "关系不存在");
        }

        relationship.setRelName(dto.getRelName());
        relationship.setRelType(dto.getRelType());
        relationship.setSourceModelId(dto.getSourceModelId());
        relationship.setSourceFieldId(dto.getSourceFieldId());
        relationship.setTargetModelId(dto.getTargetModelId());
        relationship.setTargetFieldId(dto.getTargetFieldId());
        relationship.setIsCascadeDelete(dto.getIsCascadeDelete());
        relationship.setRelConfig(dto.getRelConfig());
        relationship.setDescription(dto.getDescription());

        metaRelationshipMapper.updateById(relationship);
        return Result.success("更新成功", relationship);
    }

    @Operation(summary = "删除关系定义")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRelationship(
            @Parameter(description = "关系ID") @PathVariable Long id) {
        metaRelationshipMapper.deleteById(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取模型的所有关系定义")
    @GetMapping("/model/{modelId}")
    public Result<List<MetaRelationship>> getRelationshipsByModel(
            @Parameter(description = "模型ID") @PathVariable Long modelId) {
        LambdaQueryWrapper<MetaRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaRelationship::getSourceModelId, modelId)
                .or().eq(MetaRelationship::getTargetModelId, modelId);
        List<MetaRelationship> relationships = metaRelationshipMapper.selectList(wrapper);
        return Result.success(relationships);
    }

    @Operation(summary = "通过编码获取关系定义")
    @GetMapping("/code/{relCode}")
    public Result<MetaRelationship> getByCode(
            @Parameter(description = "关系编码") @PathVariable String relCode) {
        LambdaQueryWrapper<MetaRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaRelationship::getRelCode, relCode);
        MetaRelationship relationship = metaRelationshipMapper.selectOne(wrapper);
        return Result.success(relationship);
    }
}
