package com.bank.itarch.meta.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.meta.constant.MetaStatus;
import com.bank.itarch.meta.mapper.MetaFieldMapper;
import com.bank.itarch.meta.mapper.MetaGroupMapper;
import com.bank.itarch.meta.model.dto.MetaFieldDTO;
import com.bank.itarch.meta.model.dto.MetaGroupDTO;
import com.bank.itarch.meta.model.entity.MetaField;
import com.bank.itarch.meta.model.entity.MetaGroup;
import com.bank.itarch.meta.service.MetaAuditService;
import com.bank.itarch.meta.service.MetaFieldService;
import com.bank.itarch.meta.service.MetaGroupService;
import com.bank.itarch.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字段分组管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetaGroupServiceImpl implements MetaGroupService {

    private final MetaGroupMapper metaGroupMapper;
    private final MetaFieldMapper metaFieldMapper;
    private final MetaFieldService metaFieldService;
    private final MetaAuditService metaAuditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MetaGroup createGroup(Long modelId, MetaGroupDTO dto) {
        // 检查分组编码唯一性
        LambdaQueryWrapper<MetaGroup> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(MetaGroup::getModelId, modelId)
                .eq(MetaGroup::getGroupCode, dto.getGroupCode());
        if (metaGroupMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException(400, "分组编码已存在");
        }

        MetaGroup group = new MetaGroup();
        group.setGroupCode(dto.getGroupCode());
        group.setGroupName(dto.getGroupName());
        group.setGroupType(dto.getGroupType() != null ? dto.getGroupType() : "CUSTOM");
        group.setModelId(modelId);
        group.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        group.setDescription(dto.getDescription());
        group.setIsActive(true);
        group.setCreator(UserContext.getUser().getUsername());

        metaGroupMapper.insert(group);

        // 记录审计日志
        metaAuditService.logOperation(
                MetaStatus.AUDIT_CREATE, "GROUP", group.getId(),
                group.getGroupCode(), group.getGroupName(), null, group, null
        );

        log.info("创建分组成功: modelId={}, groupCode={}", modelId, group.getGroupCode());
        return group;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MetaGroup updateGroup(Long modelId, Long groupId, MetaGroupDTO dto) {
        MetaGroup group = metaGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(404, "分组不存在");
        }
        if (!group.getModelId().equals(modelId)) {
            throw new BusinessException(400, "分组不属于该模型");
        }

        // 检查分组编码唯一性（如果编码变更）
        if (dto.getGroupCode() != null && !dto.getGroupCode().equals(group.getGroupCode())) {
            LambdaQueryWrapper<MetaGroup> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(MetaGroup::getModelId, modelId)
                    .eq(MetaGroup::getGroupCode, dto.getGroupCode());
            if (metaGroupMapper.selectCount(checkWrapper) > 0) {
                throw new BusinessException(400, "分组编码已存在");
            }
            group.setGroupCode(dto.getGroupCode());
        }

        group.setGroupName(dto.getGroupName());
        group.setGroupType(dto.getGroupType());
        group.setSortOrder(dto.getSortOrder());
        group.setDescription(dto.getDescription());
        group.setModifier(UserContext.getUser().getUsername());

        metaGroupMapper.updateById(group);

        // 记录审计日志
        metaAuditService.logOperation(
                MetaStatus.AUDIT_UPDATE, "GROUP", group.getId(),
                group.getGroupCode(), group.getGroupName(), null, group, null
        );

        log.info("更新分组成功: groupId={}", groupId);
        return group;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long modelId, Long groupId) {
        MetaGroup group = metaGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(404, "分组不存在");
        }
        if (!group.getModelId().equals(modelId)) {
            throw new BusinessException(400, "分组不属于该模型");
        }

        // 将该分组下的字段移到默认分组
        LambdaQueryWrapper<MetaField> fieldWrapper = new LambdaQueryWrapper<>();
        fieldWrapper.eq(MetaField::getModelId, modelId)
                .eq(MetaField::getGroupId, groupId);
        List<MetaField> fields = metaFieldMapper.selectList(fieldWrapper);
        for (MetaField field : fields) {
            field.setGroupId(null); // 移除分组
            metaFieldMapper.updateById(field);
        }

        metaGroupMapper.deleteById(groupId);

        // 记录审计日志
        metaAuditService.logOperation(
                MetaStatus.AUDIT_DELETE, "GROUP", group.getId(),
                group.getGroupCode(), group.getGroupName(), group, null, null
        );

        log.info("删除分组成功: groupId={}", groupId);
    }

    @Override
    public List<MetaGroup> getGroupsByModel(Long modelId) {
        LambdaQueryWrapper<MetaGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaGroup::getModelId, modelId)
                .eq(MetaGroup::getIsActive, true)
                .orderByAsc(MetaGroup::getSortOrder);
        return metaGroupMapper.selectList(wrapper);
    }

    @Override
    public List<MetaGroupDTO> getGroupsWithFields(Long modelId) {
        List<MetaGroup> groups = getGroupsByModel(modelId);
        List<MetaField> allFields = metaFieldService.getFieldsByModel(modelId);

        return groups.stream().map(group -> {
            MetaGroupDTO dto = new MetaGroupDTO();
            dto.setId(group.getId());
            dto.setGroupCode(group.getGroupCode());
            dto.setGroupName(group.getGroupName());
            dto.setGroupType(group.getGroupType());
            dto.setModelId(group.getModelId());
            dto.setSortOrder(group.getSortOrder());
            dto.setDescription(group.getDescription());
            dto.setIsActive(group.getIsActive());

            // 加载该分组下的字段
            List<MetaFieldDTO> fieldDtos = allFields.stream()
                    .filter(f -> f.getGroupId() != null && f.getGroupId().equals(group.getId()))
                    .map(this::convertToFieldDTO)
                    .collect(Collectors.toList());
            dto.setFields(fieldDtos);

            return dto;
        }).collect(Collectors.toList());
    }

    // ========== 私有辅助方法 ==========

    private MetaFieldDTO convertToFieldDTO(MetaField field) {
        MetaFieldDTO dto = new MetaFieldDTO();
        dto.setId(field.getId());
        dto.setFieldCode(field.getFieldCode());
        dto.setFieldName(field.getFieldName());
        dto.setFieldType(field.getFieldType());
        dto.setStorageMode(field.getStorageMode());
        dto.setColumnName(field.getColumnName());
        dto.setColumnType(field.getColumnType());
        dto.setDictCode(field.getDictCode());
        dto.setIsRequired(field.getIsRequired());
        dto.setIsSensitive(field.getIsSensitive());
        dto.setSensitiveType(field.getSensitiveType());
        dto.setGroupId(field.getGroupId());
        dto.setModelId(field.getModelId());
        dto.setDisplayType(field.getDisplayType());
        dto.setOptionConfig(field.getOptionConfig());
        dto.setSortOrder(field.getSortOrder());
        dto.setDescription(field.getDescription());
        dto.setHelpText(field.getHelpText());
        dto.setIsActive(field.getIsActive());
        return dto;
    }
}
