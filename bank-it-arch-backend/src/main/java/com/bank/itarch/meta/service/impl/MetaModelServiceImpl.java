package com.bank.itarch.meta.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.meta.constant.MetaStatus;
import com.bank.itarch.meta.mapper.*;
import com.bank.itarch.meta.model.dto.MetaFieldDTO;
import com.bank.itarch.meta.model.dto.MetaGroupDTO;
import com.bank.itarch.meta.model.dto.MetaModelDTO;
import com.bank.itarch.meta.model.entity.MetaField;
import com.bank.itarch.meta.model.entity.MetaGroup;
import com.bank.itarch.meta.model.entity.MetaModel;
import com.bank.itarch.meta.model.entity.MetaRelationship;
import com.bank.itarch.meta.service.MetaAuditService;
import com.bank.itarch.meta.service.MetaFieldService;
import com.bank.itarch.meta.service.MetaGroupService;
import com.bank.itarch.meta.service.MetaModelService;
import com.bank.itarch.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 元模型管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetaModelServiceImpl implements MetaModelService {

    private final MetaModelMapper metaModelMapper;
    private final MetaGroupMapper metaGroupMapper;
    private final MetaFieldMapper metaFieldMapper;
    private final MetaRelationshipMapper metaRelationshipMapper;
    private final MetaFieldService metaFieldService;
    private final MetaGroupService metaGroupService;
    private final MetaAuditService metaAuditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MetaModel createModel(MetaModelDTO dto) {
        // 检查编码唯一性
        LambdaQueryWrapper<MetaModel> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(MetaModel::getModelCode, dto.getModelCode());
        if (metaModelMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException(400, "模型编码已存在");
        }

        MetaModel model = new MetaModel();
        model.setModelCode(dto.getModelCode());
        model.setModelName(dto.getModelName());
        model.setModelType(dto.getModelType());
        model.setTableName(dto.getTableName());
        model.setTableAlias(dto.getTableAlias());
        model.setParentModelId(dto.getParentModelId());
        model.setVersion(1);
        model.setStatus(MetaStatus.MODEL_DRAFT);
        model.setIsGeneratable(dto.getIsGeneratable() != null ? dto.getIsGeneratable() : true);
        model.setIsActive(true);
        model.setDescription(dto.getDescription());
        model.setExtraConfig(dto.getExtraConfig());
        model.setCreator(UserContext.getUser().getUsername());

        metaModelMapper.insert(model);

        // 记录审计日志
        metaAuditService.logOperation(
                MetaStatus.AUDIT_CREATE,
                "MODEL",
                model.getId(),
                model.getModelCode(),
                model.getModelName(),
                null,
                model,
                null
        );

        log.info("创建元模型成功: id={}, code={}", model.getId(), model.getModelCode());
        return model;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MetaModel updateModel(Long id, MetaModelDTO dto) {
        MetaModel model = metaModelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException(404, "元模型不存在");
        }

        // 仅草稿状态可修改
        if (!MetaStatus.MODEL_DRAFT.equals(model.getStatus())) {
            throw new BusinessException(400, "仅草稿状态可修改");
        }

        MetaModel before = cloneModel(model);

        model.setModelName(dto.getModelName());
        model.setModelType(dto.getModelType());
        model.setTableName(dto.getTableName());
        model.setTableAlias(dto.getTableAlias());
        model.setParentModelId(dto.getParentModelId());
        model.setIsGeneratable(dto.getIsGeneratable());
        model.setIsActive(dto.getIsActive());
        model.setDescription(dto.getDescription());
        model.setExtraConfig(dto.getExtraConfig());
        model.setModifier(UserContext.getUser().getUsername());

        metaModelMapper.updateById(model);

        // 记录审计日志
        metaAuditService.logOperation(
                MetaStatus.AUDIT_UPDATE,
                "MODEL",
                model.getId(),
                model.getModelCode(),
                model.getModelName(),
                before,
                model,
                getChangedFields(before, model)
        );

        log.info("更新元模型成功: id={}, code={}", model.getId(), model.getModelCode());
        return model;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(Long id) {
        MetaModel model = metaModelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException(404, "元模型不存在");
        }

        // 仅草稿状态可删除
        if (!MetaStatus.MODEL_DRAFT.equals(model.getStatus())) {
            throw new BusinessException(400, "仅草稿状态可删除");
        }

        // 删除关联的字段和分组
        LambdaQueryWrapper<MetaField> fieldWrapper = new LambdaQueryWrapper<>();
        fieldWrapper.eq(MetaField::getModelId, id);
        metaFieldMapper.delete(fieldWrapper);

        LambdaQueryWrapper<MetaGroup> groupWrapper = new LambdaQueryWrapper<>();
        groupWrapper.eq(MetaGroup::getModelId, id);
        metaGroupMapper.delete(groupWrapper);

        metaModelMapper.deleteById(id);

        // 记录审计日志
        metaAuditService.logOperation(
                MetaStatus.AUDIT_DELETE,
                "MODEL",
                model.getId(),
                model.getModelCode(),
                model.getModelName(),
                model,
                null,
                null
        );

        log.info("删除元模型成功: id={}, code={}", id, model.getModelCode());
    }

    @Override
    public PageResult<MetaModel> pageQuery(PageQuery query, String keyword, String modelType, String status) {
        Page<MetaModel> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<MetaModel> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(MetaModel::getModelCode, keyword)
                    .or().like(MetaModel::getModelName, keyword));
        }
        if (modelType != null && !modelType.isEmpty()) {
            wrapper.eq(MetaModel::getModelType, modelType);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(MetaModel::getStatus, status);
        }

        wrapper.orderByDesc(MetaModel::getCreateTime);
        IPage<MetaModel> pageResult = metaModelMapper.selectPage(page, wrapper);

        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(), query.getPage(), query.getPageSize());
    }

    @Override
    public MetaModelDTO getModelDetail(Long id) {
        MetaModel model = metaModelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException(404, "元模型不存在");
        }

        MetaModelDTO dto = new MetaModelDTO();
        dto.setId(model.getId());
        dto.setModelCode(model.getModelCode());
        dto.setModelName(model.getModelName());
        dto.setModelType(model.getModelType());
        dto.setTableName(model.getTableName());
        dto.setTableAlias(model.getTableAlias());
        dto.setParentModelId(model.getParentModelId());
        dto.setTreePath(model.getTreePath());
        dto.setVersion(model.getVersion());
        dto.setStatus(model.getStatus());
        dto.setIsGeneratable(model.getIsGeneratable());
        dto.setIsActive(model.getIsActive());
        dto.setDescription(model.getDescription());
        dto.setExtraConfig(model.getExtraConfig());
        dto.setCreateTime(model.getCreateTime());
        dto.setUpdateTime(model.getUpdateTime());
        dto.setCreator(model.getCreator());
        dto.setModifier(model.getModifier());

        // 加载分组（含字段）
        List<MetaGroupDTO> groups = metaGroupService.getGroupsWithFields(id);
        dto.setGroups(groups);

        // 加载字段
        List<MetaField> fields = metaFieldService.getFieldsByModel(id);
        dto.setFields(fields.stream().map(this::convertToFieldDTO).collect(Collectors.toList()));

        // 加载关系 - 转换为DTO
        LambdaQueryWrapper<MetaRelationship> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(MetaRelationship::getSourceModelId, id);
        List<MetaRelationship> relationships = metaRelationshipMapper.selectList(relWrapper);
        List<com.bank.itarch.meta.model.dto.MetaRelationshipDTO> relDtos = relationships.stream()
            .map(this::convertToRelDTO)
            .collect(Collectors.toList());
        dto.setRelationships(relDtos);

        return dto;
    }

    @Override
    public MetaModel getByCode(String modelCode) {
        LambdaQueryWrapper<MetaModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaModel::getModelCode, modelCode);
        return metaModelMapper.selectOne(wrapper);
    }

    @Override
    public List<MetaModel> listAllActive() {
        LambdaQueryWrapper<MetaModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaModel::getIsActive, true);
        wrapper.orderByDesc(MetaModel::getCreateTime);
        return metaModelMapper.selectList(wrapper);
    }

    @Override
    public List<MetaModel> listByType(String modelType) {
        LambdaQueryWrapper<MetaModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaModel::getModelType, modelType);
        wrapper.eq(MetaModel::getIsActive, true);
        return metaModelMapper.selectList(wrapper);
    }

    // ========== 私有辅助方法 ==========

    private MetaFieldDTO convertToFieldDTO(MetaField field) {
        MetaFieldDTO dto = new MetaFieldDTO();
        dto.setId(field.getId());
        dto.setFieldCode(field.getFieldCode());
        dto.setFieldName(field.getFieldName());
        dto.setFieldType(field.getFieldType());
        dto.setStorageMode(field.getStorageMode());
        dto.setJsonPath(field.getJsonPath());
        dto.setColumnName(field.getColumnName());
        dto.setColumnType(field.getColumnType());
        dto.setColumnSize(field.getColumnSize());
        dto.setDictCode(field.getDictCode());
        dto.setValidationRule(field.getValidationRule());
        dto.setDefaultValue(field.getDefaultValue());
        dto.setIsRequired(field.getIsRequired());
        dto.setIsUnique(field.getIsUnique());
        dto.setIsIndexed(field.getIsIndexed());
        dto.setIsQueryable(field.getIsQueryable());
        dto.setIsSensitive(field.getIsSensitive());
        dto.setSensitiveType(field.getSensitiveType());
        dto.setGroupId(field.getGroupId());
        dto.setModelId(field.getModelId());
        dto.setFieldLevel(field.getFieldLevel());
        dto.setDisplayType(field.getDisplayType());
        dto.setOptionConfig(field.getOptionConfig());
        dto.setSortOrder(field.getSortOrder());
        dto.setDescription(field.getDescription());
        dto.setHelpText(field.getHelpText());
        dto.setIsActive(field.getIsActive());
        return dto;
    }

    private com.bank.itarch.meta.model.dto.MetaRelationshipDTO convertToRelDTO(MetaRelationship rel) {
        com.bank.itarch.meta.model.dto.MetaRelationshipDTO dto = new com.bank.itarch.meta.model.dto.MetaRelationshipDTO();
        dto.setId(rel.getId());
        dto.setRelCode(rel.getRelCode());
        dto.setRelName(rel.getRelName());
        dto.setRelType(rel.getRelType());
        dto.setSourceModelId(rel.getSourceModelId());
        dto.setSourceFieldId(rel.getSourceFieldId());
        dto.setTargetModelId(rel.getTargetModelId());
        dto.setTargetFieldId(rel.getTargetFieldId());
        dto.setIsCascadeDelete(rel.getIsCascadeDelete());
        dto.setIsSelfRel(rel.getIsSelfRel());
        dto.setRelConfig(rel.getRelConfig());
        dto.setDescription(rel.getDescription());
        dto.setIsActive(rel.getIsActive());
        return dto;
    }

    private MetaModel cloneModel(MetaModel model) {
        MetaModel clone = new MetaModel();
        clone.setId(model.getId());
        clone.setModelCode(model.getModelCode());
        clone.setModelName(model.getModelName());
        clone.setModelType(model.getModelType());
        clone.setTableName(model.getTableName());
        clone.setTableAlias(model.getTableAlias());
        clone.setParentModelId(model.getParentModelId());
        clone.setTreePath(model.getTreePath());
        clone.setVersion(model.getVersion());
        clone.setStatus(model.getStatus());
        clone.setIsGeneratable(model.getIsGeneratable());
        clone.setIsActive(model.getIsActive());
        clone.setDescription(model.getDescription());
        clone.setExtraConfig(model.getExtraConfig());
        clone.setCreator(model.getCreator());
        clone.setModifier(model.getModifier());
        clone.setCreateTime(model.getCreateTime());
        clone.setUpdateTime(model.getUpdateTime());
        return clone;
    }

    private List<String> getChangedFields(MetaModel before, MetaModel after) {
        List<String> changedFields = new ArrayList<>();
        if (!equals(before.getModelName(), after.getModelName())) {
            changedFields.add("modelName");
        }
        if (!equals(before.getModelType(), after.getModelType())) {
            changedFields.add("modelType");
        }
        if (!equals(before.getTableName(), after.getTableName())) {
            changedFields.add("tableName");
        }
        if (!equals(before.getDescription(), after.getDescription())) {
            changedFields.add("description");
        }
        return changedFields;
    }

    private boolean equals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
