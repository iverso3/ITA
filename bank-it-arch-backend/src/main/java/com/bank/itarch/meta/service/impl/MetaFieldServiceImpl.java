package com.bank.itarch.meta.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.meta.constant.MetaStorageMode;
import com.bank.itarch.meta.constant.MetaStatus;
import com.bank.itarch.meta.mapper.MetaFieldMapper;
import com.bank.itarch.meta.mapper.MetaGroupMapper;
import com.bank.itarch.meta.model.dto.MetaFieldDTO;
import com.bank.itarch.meta.model.entity.MetaField;
import com.bank.itarch.meta.model.entity.MetaGroup;
import com.bank.itarch.meta.service.MetaAuditService;
import com.bank.itarch.meta.service.MetaFieldService;
import com.bank.itarch.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字段定义管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetaFieldServiceImpl implements MetaFieldService {

    private final MetaFieldMapper metaFieldMapper;
    private final MetaGroupMapper metaGroupMapper;
    private final MetaAuditService metaAuditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MetaField addField(Long modelId, MetaFieldDTO dto) {
        // 检查字段编码唯一性
        LambdaQueryWrapper<MetaField> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(MetaField::getModelId, modelId)
                .eq(MetaField::getFieldCode, dto.getFieldCode());
        if (metaFieldMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException(400, "字段编码已存在");
        }

        MetaField field = new MetaField();
        field.setFieldCode(dto.getFieldCode());
        field.setFieldName(dto.getFieldName());
        field.setFieldType(dto.getFieldType() != null ? dto.getFieldType() : "STRING");
        field.setStorageMode(dto.getStorageMode() != null ? dto.getStorageMode() : MetaStorageMode.COLUMN);
        field.setColumnName(dto.getColumnName() != null ? dto.getColumnName() : dto.getFieldCode());
        field.setColumnType(getColumnType(dto.getFieldType()));
        field.setColumnSize(dto.getColumnSize());
        field.setDictCode(dto.getDictCode());
        field.setValidationRule(dto.getValidationRule());
        field.setDefaultValue(dto.getDefaultValue());
        field.setIsRequired(dto.getIsRequired() != null ? dto.getIsRequired() : false);
        field.setIsUnique(dto.getIsUnique() != null ? dto.getIsUnique() : false);
        field.setIsIndexed(dto.getIsIndexed() != null ? dto.getIsIndexed() : false);
        field.setIsQueryable(dto.getIsQueryable() != null ? dto.getIsQueryable() : true);
        field.setIsSensitive(dto.getIsSensitive() != null ? dto.getIsSensitive() : false);
        field.setSensitiveType(dto.getSensitiveType());
        field.setGroupId(dto.getGroupId());
        field.setModelId(modelId);
        field.setFieldLevel(dto.getFieldLevel() != null ? dto.getFieldLevel() : "NORMAL");
        field.setDisplayType(dto.getDisplayType() != null ? dto.getDisplayType() : "INPUT");
        field.setOptionConfig(dto.getOptionConfig());
        field.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        field.setDescription(dto.getDescription());
        field.setHelpText(dto.getHelpText());
        field.setIsActive(true);
        field.setCreator(UserContext.getUser().getUsername());

        metaFieldMapper.insert(field);

        // 记录审计日志
        metaAuditService.logOperation(
                MetaStatus.AUDIT_CREATE, "FIELD", field.getId(),
                field.getFieldCode(), field.getFieldName(), null, field, null
        );

        log.info("添加字段成功: modelId={}, fieldCode={}", modelId, field.getFieldCode());
        return field;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MetaField updateField(Long modelId, Long fieldId, MetaFieldDTO dto) {
        MetaField field = metaFieldMapper.selectById(fieldId);
        if (field == null) {
            throw new BusinessException(404, "字段不存在");
        }
        if (!field.getModelId().equals(modelId)) {
            throw new BusinessException(400, "字段不属于该模型");
        }

        // 检查字段编码唯一性（如果编码变更）
        if (dto.getFieldCode() != null && !dto.getFieldCode().equals(field.getFieldCode())) {
            LambdaQueryWrapper<MetaField> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(MetaField::getModelId, modelId)
                    .eq(MetaField::getFieldCode, dto.getFieldCode());
            if (metaFieldMapper.selectCount(checkWrapper) > 0) {
                throw new BusinessException(400, "字段编码已存在");
            }
            field.setFieldCode(dto.getFieldCode());
        }

        MetaField before = cloneField(field);

        field.setFieldName(dto.getFieldName());
        field.setFieldType(dto.getFieldType());
        field.setStorageMode(dto.getStorageMode());
        field.setColumnName(dto.getColumnName());
        field.setColumnType(getColumnType(dto.getFieldType()));
        field.setColumnSize(dto.getColumnSize());
        field.setDictCode(dto.getDictCode());
        field.setValidationRule(dto.getValidationRule());
        field.setDefaultValue(dto.getDefaultValue());
        field.setIsRequired(dto.getIsRequired());
        field.setIsUnique(dto.getIsUnique());
        field.setIsIndexed(dto.getIsIndexed());
        field.setIsQueryable(dto.getIsQueryable());
        field.setIsSensitive(dto.getIsSensitive());
        field.setSensitiveType(dto.getSensitiveType());
        field.setGroupId(dto.getGroupId());
        field.setFieldLevel(dto.getFieldLevel());
        field.setDisplayType(dto.getDisplayType());
        field.setOptionConfig(dto.getOptionConfig());
        field.setSortOrder(dto.getSortOrder());
        field.setDescription(dto.getDescription());
        field.setHelpText(dto.getHelpText());
        field.setModifier(UserContext.getUser().getUsername());

        metaFieldMapper.updateById(field);

        // 记录审计日志
        metaAuditService.logOperation(
                MetaStatus.AUDIT_UPDATE, "FIELD", field.getId(),
                field.getFieldCode(), field.getFieldName(), before, field,
                getChangedFields(before, field)
        );

        log.info("更新字段成功: fieldId={}", fieldId);
        return field;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteField(Long modelId, Long fieldId) {
        MetaField field = metaFieldMapper.selectById(fieldId);
        if (field == null) {
            throw new BusinessException(404, "字段不存在");
        }
        if (!field.getModelId().equals(modelId)) {
            throw new BusinessException(400, "字段不属于该模型");
        }

        metaFieldMapper.deleteById(fieldId);

        // 记录审计日志
        metaAuditService.logOperation(
                MetaStatus.AUDIT_DELETE, "FIELD", field.getId(),
                field.getFieldCode(), field.getFieldName(), field, null, null
        );

        log.info("删除字段成功: fieldId={}", fieldId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MetaField> batchAddFields(Long modelId, List<MetaFieldDTO> fields) {
        return fields.stream()
                .map(dto -> addField(modelId, dto))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reorderFields(Long modelId, List<Long> fieldIds) {
        for (int i = 0; i < fieldIds.size(); i++) {
            MetaField field = metaFieldMapper.selectById(fieldIds.get(i));
            if (field != null && field.getModelId().equals(modelId)) {
                field.setSortOrder(i);
                metaFieldMapper.updateById(field);
            }
        }
    }

    @Override
    public List<MetaField> getFieldsByModel(Long modelId) {
        LambdaQueryWrapper<MetaField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaField::getModelId, modelId)
                .eq(MetaField::getIsActive, true)
                .orderByAsc(MetaField::getSortOrder);
        return metaFieldMapper.selectList(wrapper);
    }

    @Override
    public List<MetaField> getQueryableFields(Long modelId) {
        LambdaQueryWrapper<MetaField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaField::getModelId, modelId)
                .eq(MetaField::getIsQueryable, true)
                .eq(MetaField::getIsActive, true)
                .orderByAsc(MetaField::getSortOrder);
        return metaFieldMapper.selectList(wrapper);
    }

    @Override
    public List<MetaField> getSensitiveFields(Long modelId) {
        LambdaQueryWrapper<MetaField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaField::getModelId, modelId)
                .eq(MetaField::getIsSensitive, true)
                .eq(MetaField::getIsActive, true);
        return metaFieldMapper.selectList(wrapper);
    }

    @Override
    public MetaField getByCode(Long modelId, String fieldCode) {
        LambdaQueryWrapper<MetaField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaField::getModelId, modelId)
                .eq(MetaField::getFieldCode, fieldCode);
        return metaFieldMapper.selectOne(wrapper);
    }

    // ========== 私有辅助方法 ==========

    private String getColumnType(String fieldType) {
        if (fieldType == null) return "VARCHAR(255)";
        switch (fieldType) {
            case "INT": return "INT";
            case "BIGINT": return "BIGINT";
            case "DECIMAL": return "DECIMAL(20,6)";
            case "BOOLEAN": return "TINYINT(1)";
            case "DATE": return "DATE";
            case "DATETIME": return "DATETIME";
            case "TEXT": return "TEXT";
            case "JSON": return "JSON";
            default: return "VARCHAR(255)";
        }
    }

    private MetaField cloneField(MetaField field) {
        MetaField clone = new MetaField();
        clone.setId(field.getId());
        clone.setFieldCode(field.getFieldCode());
        clone.setFieldName(field.getFieldName());
        clone.setFieldType(field.getFieldType());
        clone.setStorageMode(field.getStorageMode());
        clone.setColumnName(field.getColumnName());
        clone.setColumnType(field.getColumnType());
        clone.setColumnSize(field.getColumnSize());
        clone.setDictCode(field.getDictCode());
        clone.setValidationRule(field.getValidationRule());
        clone.setDefaultValue(field.getDefaultValue());
        clone.setIsRequired(field.getIsRequired());
        clone.setIsUnique(field.getIsUnique());
        clone.setIsIndexed(field.getIsIndexed());
        clone.setIsQueryable(field.getIsQueryable());
        clone.setIsSensitive(field.getIsSensitive());
        clone.setSensitiveType(field.getSensitiveType());
        clone.setGroupId(field.getGroupId());
        clone.setModelId(field.getModelId());
        clone.setFieldLevel(field.getFieldLevel());
        clone.setDisplayType(field.getDisplayType());
        clone.setOptionConfig(field.getOptionConfig());
        clone.setSortOrder(field.getSortOrder());
        clone.setDescription(field.getDescription());
        clone.setHelpText(field.getHelpText());
        clone.setIsActive(field.getIsActive());
        return clone;
    }

    private List<String> getChangedFields(MetaField before, MetaField after) {
        List<String> changed = new java.util.ArrayList<>();
        if (!equals(before.getFieldName(), after.getFieldName())) changed.add("fieldName");
        if (!equals(before.getFieldType(), after.getFieldType())) changed.add("fieldType");
        if (!equals(before.getStorageMode(), after.getStorageMode())) changed.add("storageMode");
        if (!equals(before.getIsRequired(), after.getIsRequired())) changed.add("isRequired");
        return changed;
    }

    private boolean equals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
