package com.bank.itarch.meta.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.meta.constant.MetaStatus;
import com.bank.itarch.meta.constant.MetaStorageMode;
import com.bank.itarch.meta.mapper.*;
import com.bank.itarch.meta.model.dto.MetaEntityDTO;
import com.bank.itarch.meta.model.dto.MetaRelationDTO;
import com.bank.itarch.meta.model.entity.*;
import com.bank.itarch.meta.service.MetaAuditService;
import com.bank.itarch.meta.service.MetaEntityService;
import com.bank.itarch.meta.service.MetaFieldService;
import com.bank.itarch.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 实体管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetaEntityServiceImpl implements MetaEntityService {

    private final MetaEntityMapper metaEntityMapper;
    private final MetaValueMapper metaValueMapper;
    private final MetaRelationMapper metaRelationMapper;
    private final MetaFieldMapper metaFieldMapper;
    private final MetaFieldService metaFieldService;
    private final MetaAuditService metaAuditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MetaEntity createEntity(MetaEntityDTO dto) {
        // 检查实体编码唯一性
        LambdaQueryWrapper<MetaEntity> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(MetaEntity::getEntityCode, dto.getEntityCode());
        if (metaEntityMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException(400, "实体编码已存在");
        }

        MetaEntity entity = new MetaEntity();
        entity.setEntityCode(dto.getEntityCode());
        entity.setEntityName(dto.getEntityName());
        entity.setModelId(dto.getModelId());
        entity.setModelCode(dto.getModelCode());
        entity.setModelVersionId(dto.getModelVersionId());
        entity.setBusinessId(dto.getBusinessId());
        entity.setStatus(MetaStatus.ENTITY_ACTIVE);
        entity.setOwnerId(dto.getOwnerId());
        entity.setOwnerType(dto.getOwnerType());
        entity.setDeptId(dto.getDeptId());
        entity.setDeptPath(dto.getDeptPath());
        entity.setDescription(dto.getDescription());
        entity.setCreator(UserContext.getUser().getUsername());
        entity.setDataVersion(1);

        metaEntityMapper.insert(entity);

        // 保存字段值
        if (dto.getFieldValues() != null && !dto.getFieldValues().isEmpty()) {
            saveFieldValues(entity.getId(), dto.getModelId(), dto.getFieldValues());
        }

        // 记录审计日志
        metaAuditService.logOperation(
                MetaStatus.AUDIT_CREATE, "ENTITY", entity.getId(),
                entity.getEntityCode(), entity.getEntityName(), null, entity, null
        );

        log.info("创建实体成功: id={}, code={}", entity.getId(), entity.getEntityCode());
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MetaEntity> batchCreate(Long modelId, List<MetaEntityDTO> entities) {
        return entities.stream()
                .filter(e -> e.getModelId() == null || e.getModelId().equals(modelId))
                .map(e -> {
                    e.setModelId(modelId);
                    return createEntity(e);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MetaEntity updateEntity(Long id, MetaEntityDTO dto) {
        MetaEntity entity = metaEntityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "实体不存在");
        }

        entity.setEntityName(dto.getEntityName());
        entity.setOwnerId(dto.getOwnerId());
        entity.setOwnerType(dto.getOwnerType());
        entity.setDeptId(dto.getDeptId());
        entity.setDeptPath(dto.getDeptPath());
        entity.setDescription(dto.getDescription());
        entity.setModifier(UserContext.getUser().getUsername());
        entity.setDataVersion(entity.getDataVersion() + 1); // 乐观锁

        metaEntityMapper.updateById(entity);

        // 更新字段值
        if (dto.getFieldValues() != null && !dto.getFieldValues().isEmpty()) {
            saveFieldValues(id, entity.getModelId(), dto.getFieldValues());
        }

        // 记录审计日志
        metaAuditService.logOperation(
                MetaStatus.AUDIT_UPDATE, "ENTITY", entity.getId(),
                entity.getEntityCode(), entity.getEntityName(), null, entity, null
        );

        log.info("更新实体成功: id={}", id);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEntity(Long id) {
        MetaEntity entity = metaEntityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "实体不存在");
        }

        // 软删除
        entity.setStatus(MetaStatus.ENTITY_DELETED);
        metaEntityMapper.updateById(entity);

        // 删除关联的字段值
        LambdaQueryWrapper<MetaValue> valueWrapper = new LambdaQueryWrapper<>();
        valueWrapper.eq(MetaValue::getEntityId, id);
        metaValueMapper.delete(valueWrapper);

        // 删除关联的关系
        LambdaQueryWrapper<MetaRelation> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(MetaRelation::getSourceEntityId, id)
                .or()
                .eq(MetaRelation::getTargetEntityId, id);
        metaRelationMapper.delete(relWrapper);

        // 记录审计日志
        metaAuditService.logOperation(
                MetaStatus.AUDIT_DELETE, "ENTITY", entity.getId(),
                entity.getEntityCode(), entity.getEntityName(), entity, null, null
        );

        log.info("删除实体成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(Long modelId, List<Long> ids) {
        ids.forEach(this::deleteEntity);
    }

    @Override
    public PageResult<MetaEntity> pageQuery(PageQuery query, Long modelId, String keyword,
                                             Map<String, Object> filters) {
        Page<MetaEntity> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<MetaEntity> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(MetaEntity::getModelId, modelId)
                .eq(MetaEntity::getStatus, MetaStatus.ENTITY_ACTIVE);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(MetaEntity::getEntityCode, keyword)
                    .or().like(MetaEntity::getEntityName, keyword));
        }

        wrapper.orderByDesc(MetaEntity::getCreateTime);
        IPage<MetaEntity> pageResult = metaEntityMapper.selectPage(page, wrapper);

        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(),
                query.getPage(), query.getPageSize());
    }

    @Override
    public MetaEntityDTO getEntityDetail(Long id) {
        MetaEntity entity = metaEntityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "实体不存在");
        }

        MetaEntityDTO dto = new MetaEntityDTO();
        dto.setId(entity.getId());
        dto.setEntityCode(entity.getEntityCode());
        dto.setEntityName(entity.getEntityName());
        dto.setModelId(entity.getModelId());
        dto.setModelCode(entity.getModelCode());
        dto.setModelVersionId(entity.getModelVersionId());
        dto.setBusinessId(entity.getBusinessId());
        dto.setStatus(entity.getStatus());
        dto.setOwnerId(entity.getOwnerId());
        dto.setOwnerType(entity.getOwnerType());
        dto.setDeptId(entity.getDeptId());
        dto.setDeptPath(entity.getDeptPath());
        dto.setDataVersion(entity.getDataVersion());
        dto.setDescription(entity.getDescription());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        dto.setCreator(entity.getCreator());
        dto.setModifier(entity.getModifier());

        // 加载字段值
        dto.setFieldValues(getAllFieldValues(id));

        // 加载关系
        dto.setRelations(getEntityRelations(id));

        return dto;
    }

    @Override
    public MetaEntity getByBusinessId(Long modelId, Long businessId) {
        LambdaQueryWrapper<MetaEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaEntity::getModelId, modelId)
                .eq(MetaEntity::getBusinessId, businessId);
        return metaEntityMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindRelation(Long sourceEntityId, String relCode, Long targetEntityId) {
        MetaEntity source = metaEntityMapper.selectById(sourceEntityId);
        MetaEntity target = metaEntityMapper.selectById(targetEntityId);
        if (source == null || target == null) {
            throw new BusinessException(404, "实体不存在");
        }

        // 检查关系是否已存在
        LambdaQueryWrapper<MetaRelation> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(MetaRelation::getSourceEntityId, sourceEntityId)
                .eq(MetaRelation::getRelCode, relCode)
                .eq(MetaRelation::getTargetEntityId, targetEntityId)
                .eq(MetaRelation::getIsActive, true);
        if (metaRelationMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException(400, "关系已存在");
        }

        MetaRelation relation = new MetaRelation();
        relation.setRelCode(relCode);
        relation.setSourceEntityId(sourceEntityId);
        relation.setSourceEntityCode(source.getEntityCode());
        relation.setTargetEntityId(targetEntityId);
        relation.setTargetEntityCode(target.getEntityCode());
        relation.setIsActive(true);
        relation.setCreator(UserContext.getUser().getUsername());

        metaRelationMapper.insert(relation);

        log.info("绑定关系成功: source={}, rel={}, target={}", sourceEntityId, relCode, targetEntityId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindRelation(Long sourceEntityId, String relCode, Long targetEntityId) {
        LambdaQueryWrapper<MetaRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaRelation::getSourceEntityId, sourceEntityId)
                .eq(MetaRelation::getRelCode, relCode)
                .eq(MetaRelation::getTargetEntityId, targetEntityId);
        metaRelationMapper.delete(wrapper);

        log.info("解绑关系成功: source={}, rel={}, target={}", sourceEntityId, relCode, targetEntityId);
    }

    @Override
    public List<MetaRelationDTO> getEntityRelations(Long entityId) {
        LambdaQueryWrapper<MetaRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaRelation::getSourceEntityId, entityId)
                .eq(MetaRelation::getIsActive, true);
        List<MetaRelation> relations = metaRelationMapper.selectList(wrapper);

        return relations.stream().map(rel -> {
            MetaRelationDTO dto = new MetaRelationDTO();
            dto.setId(rel.getId());
            dto.setRelId(rel.getRelId());
            dto.setRelCode(rel.getRelCode());
            dto.setRelType(rel.getRelType());
            dto.setSourceEntityId(rel.getSourceEntityId());
            dto.setSourceEntityCode(rel.getSourceEntityCode());
            dto.setTargetEntityId(rel.getTargetEntityId());
            dto.setTargetEntityCode(rel.getTargetEntityCode());
            dto.setRelationLevel(rel.getRelationLevel());
            dto.setDescription(rel.getDescription());
            dto.setIsActive(rel.getIsActive());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MetaEntity> getRelatedEntities(Long entityId, String relCode) {
        // 获取源实体
        MetaEntity source = metaEntityMapper.selectById(entityId);
        if (source == null) {
            return List.of();
        }

        // 查询所有关联实体
        LambdaQueryWrapper<MetaRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaRelation::getSourceEntityId, entityId)
                .eq(MetaRelation::getRelCode, relCode)
                .eq(MetaRelation::getIsActive, true);
        List<MetaRelation> relations = metaRelationMapper.selectList(wrapper);

        List<Long> targetIds = relations.stream()
                .map(MetaRelation::getTargetEntityId)
                .collect(Collectors.toList());

        if (targetIds.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<MetaEntity> entityWrapper = new LambdaQueryWrapper<>();
        entityWrapper.in(MetaEntity::getId, targetIds);
        return metaEntityMapper.selectList(entityWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setFieldValue(Long entityId, String fieldCode, Object value) {
        Map<String, Object> values = new HashMap<>();
        values.put(fieldCode, value);
        saveFieldValues(entityId, null, values);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setFieldValues(Long entityId, Map<String, Object> values) {
        saveFieldValues(entityId, null, values);
    }

    @Override
    public Object getFieldValue(Long entityId, String fieldCode) {
        LambdaQueryWrapper<MetaValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaValue::getEntityId, entityId)
                .eq(MetaValue::getFieldCode, fieldCode)
                .eq(MetaValue::getIsCurrent, true);
        MetaValue metaValue = metaValueMapper.selectOne(wrapper);

        if (metaValue == null) {
            return null;
        }

        return getValueFromMetaValue(metaValue);
    }

    @Override
    public Map<String, Object> getAllFieldValues(Long entityId) {
        LambdaQueryWrapper<MetaValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaValue::getEntityId, entityId)
                .eq(MetaValue::getIsCurrent, true);
        List<MetaValue> values = metaValueMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        for (MetaValue mv : values) {
            result.put(mv.getFieldCode(), getValueFromMetaValue(mv));
        }

        return result;
    }

    // ========== 私有辅助方法 ==========

    private void saveFieldValues(Long entityId, Long modelId, Map<String, Object> fieldValues) {
        for (Map.Entry<String, Object> entry : fieldValues.entrySet()) {
            String fieldCode = entry.getKey();
            Object value = entry.getValue();

            // 获取字段定义
            MetaField field = null;
            String fieldType = null;
            if (modelId != null) {
                field = metaFieldService.getByCode(modelId, fieldCode);
                if (field != null) {
                    fieldType = field.getFieldType();
                }
            }
            if (field == null) {
                LambdaQueryWrapper<MetaValue> vWrapper = new LambdaQueryWrapper<>();
                vWrapper.eq(MetaValue::getEntityId, entityId)
                        .eq(MetaValue::getFieldCode, fieldCode);
                MetaValue existing = metaValueMapper.selectOne(vWrapper);
                if (existing != null) {
                    field = metaFieldMapper.selectById(existing.getFieldId());
                    if (field != null) {
                        fieldType = field.getFieldType();
                    }
                }
            }

            // 查询是否已存在值
            LambdaQueryWrapper<MetaValue> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MetaValue::getEntityId, entityId)
                    .eq(MetaValue::getFieldCode, fieldCode);
            MetaValue existing = metaValueMapper.selectOne(wrapper);

            if (existing != null) {
                // 更新旧值为非当前
                existing.setIsCurrent(false);
                metaValueMapper.updateById(existing);
            }

            // 创建新值
            MetaValue metaValue = new MetaValue();
            metaValue.setEntityId(entityId);
            metaValue.setFieldId(field != null ? field.getId() : null);
            metaValue.setFieldCode(fieldCode);
            metaValue.setStorageMode(field != null ? field.getStorageMode() : MetaStorageMode.COLUMN);
            metaValue.setIsCurrent(true);
            metaValue.setVersion(1);
            metaValue.setCreator(UserContext.getUser().getUsername());

            // 根据类型设置值
            setValueToMetaValue(metaValue, value, fieldType);

            metaValueMapper.insert(metaValue);
        }
    }

    private void setValueToMetaValue(MetaValue metaValue, Object value, String fieldType) {
        if (value == null) return;

        // 根据字段类型处理值
        if (com.bank.itarch.meta.constant.MetaFieldType.DATE.equals(fieldType)) {
            metaValue.setValueText(String.valueOf(value));
            try {
                metaValue.setValueDate(java.time.LocalDate.parse(String.valueOf(value)));
            } catch (Exception e) {
                log.warn("日期格式解析失败: {}", value);
            }
        } else if (com.bank.itarch.meta.constant.MetaFieldType.DATETIME.equals(fieldType)) {
            metaValue.setValueText(String.valueOf(value));
            try {
                metaValue.setValueDatetime(java.time.LocalDateTime.parse(String.valueOf(value)));
            } catch (Exception e) {
                log.warn("日期时间格式解析失败: {}", value);
            }
        } else if (com.bank.itarch.meta.constant.MetaFieldType.JSON.equals(fieldType)) {
            String jsonStr = String.valueOf(value);
            metaValue.setValueText(jsonStr);
            metaValue.setValueJson(jsonStr);
        } else if (com.bank.itarch.meta.constant.MetaFieldType.BOOLEAN.equals(fieldType)) {
            metaValue.setValueBoolean(Boolean.parseBoolean(String.valueOf(value)));
            metaValue.setValueText(String.valueOf(value));
        } else if (com.bank.itarch.meta.constant.MetaFieldType.INT.equals(fieldType) || com.bank.itarch.meta.constant.MetaFieldType.BIGINT.equals(fieldType)) {
            try {
                metaValue.setValueInt(Long.parseLong(String.valueOf(value)));
                metaValue.setValueText(String.valueOf(value));
            } catch (NumberFormatException e) {
                metaValue.setValueText(String.valueOf(value));
            }
        } else if (com.bank.itarch.meta.constant.MetaFieldType.DECIMAL.equals(fieldType)) {
            try {
                metaValue.setValueDecimal(new java.math.BigDecimal(String.valueOf(value)));
                metaValue.setValueText(String.valueOf(value));
            } catch (NumberFormatException e) {
                metaValue.setValueText(String.valueOf(value));
            }
        } else {
            // 默认作为文本处理
            metaValue.setValueText(String.valueOf(value));
        }
    }

    private Object getValueFromMetaValue(MetaValue metaValue) {
        if (metaValue == null) return null;

        if (metaValue.getValueBoolean() != null) {
            return metaValue.getValueBoolean();
        }
        if (metaValue.getValueInt() != null) {
            return metaValue.getValueInt();
        }
        if (metaValue.getValueDecimal() != null) {
            return metaValue.getValueDecimal();
        }
        if (metaValue.getValueDate() != null) {
            return metaValue.getValueDate();
        }
        if (metaValue.getValueDatetime() != null) {
            return metaValue.getValueDatetime();
        }
        if (metaValue.getValueJson() != null) {
            return metaValue.getValueJson();
        }

        return metaValue.getValueText();
    }
}
