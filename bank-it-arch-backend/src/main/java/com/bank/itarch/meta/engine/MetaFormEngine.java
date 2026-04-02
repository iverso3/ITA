package com.bank.itarch.meta.engine;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.meta.mapper.*;
import com.bank.itarch.meta.model.entity.*;
import com.bank.itarch.meta.model.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 动态表单引擎
 * 根据元模型定义生成前端表单配置
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetaFormEngine {

    private final MetaModelMapper metaModelMapper;
    private final MetaGroupMapper metaGroupMapper;
    private final MetaFieldMapper metaFieldMapper;
    private final MetaDataDictExtMapper dictExtMapper;

    /**
     * 获取表单配置（包含所有字段的渲染配置）
     */
    public MetaFormConfigVO buildFormConfig(Long modelId, String formType) {
        MetaModel model = metaModelMapper.selectById(modelId);
        if (model == null) {
            throw new BusinessException(404, "模型不存在: " + modelId);
        }

        MetaFormConfigVO config = new MetaFormConfigVO();
        config.setModelId(modelId);
        config.setModelCode(model.getModelCode());
        config.setModelName(model.getModelName());

        // 获取分组
        List<MetaFormGroupVO> groups = buildGroupFormConfig(modelId);
        config.setGroups(groups);

        return config;
    }

    /**
     * 获取分组表单配置
     */
    public List<MetaFormGroupVO> buildGroupFormConfig(Long modelId) {
        // 获取所有分组
        LambdaQueryWrapper<MetaGroup> groupWrapper = new LambdaQueryWrapper<>();
        groupWrapper.eq(MetaGroup::getModelId, modelId)
                .eq(MetaGroup::getIsActive, true)
                .orderByAsc(MetaGroup::getSortOrder);
        List<MetaGroup> groups = metaGroupMapper.selectList(groupWrapper);

        // 获取所有字段
        LambdaQueryWrapper<MetaField> fieldWrapper = new LambdaQueryWrapper<>();
        fieldWrapper.eq(MetaField::getModelId, modelId)
                .eq(MetaField::getIsActive, true)
                .orderByAsc(MetaField::getSortOrder);
        List<MetaField> allFields = metaFieldMapper.selectList(fieldWrapper);

        // 按分组组织字段
        Map<Long, List<MetaField>> fieldsByGroup = allFields.stream()
                .filter(f -> f.getGroupId() != null)
                .collect(Collectors.groupingBy(MetaField::getGroupId));

        // 未分组的字段
        List<MetaField> ungroupedFields = allFields.stream()
                .filter(f -> f.getGroupId() == null)
                .collect(Collectors.toList());

        List<MetaFormGroupVO> result = new ArrayList<>();

        // 处理有分组的字段
        for (MetaGroup group : groups) {
            List<MetaField> groupFields = fieldsByGroup.getOrDefault(group.getId(), List.of());

            MetaFormGroupVO groupVO = new MetaFormGroupVO();
            groupVO.setGroupId(group.getId());
            groupVO.setGroupCode(group.getGroupCode());
            groupVO.setGroupName(group.getGroupName());
            groupVO.setGroupType(group.getGroupType());
            groupVO.setSortOrder(group.getSortOrder());
            groupVO.setFields(buildFieldConfigs(groupFields));
            result.add(groupVO);
        }

        // 处理未分组的字段（放入"其他"分组）
        if (!ungroupedFields.isEmpty()) {
            MetaFormGroupVO ungrouped = new MetaFormGroupVO();
            ungrouped.setGroupCode("other");
            ungrouped.setGroupName("其他信息");
            ungrouped.setGroupType("CUSTOM");
            ungrouped.setFields(buildFieldConfigs(ungroupedFields));
            result.add(ungrouped);
        }

        return result;
    }

    /**
     * 构建字段配置列表
     */
    private List<MetaFormFieldVO> buildFieldConfigs(List<MetaField> fields) {
        return fields.stream()
                .map(this::buildFieldConfig)
                .collect(Collectors.toList());
    }

    /**
     * 构建单个字段配置
     */
    public MetaFormFieldVO buildFieldConfig(Long modelId, String fieldCode) {
        LambdaQueryWrapper<MetaField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaField::getModelId, modelId)
                .eq(MetaField::getFieldCode, fieldCode);
        MetaField field = metaFieldMapper.selectOne(wrapper);

        if (field == null) {
            return null;
        }

        return buildFieldConfig(field);
    }

    /**
     * 构建单个字段配置
     */
    private MetaFormFieldVO buildFieldConfig(MetaField field) {
        MetaFormFieldVO vo = new MetaFormFieldVO();
        vo.setFieldId(field.getId());
        vo.setFieldCode(field.getFieldCode());
        vo.setFieldName(field.getFieldName());
        vo.setFieldType(field.getFieldType());
        vo.setDisplayType(field.getDisplayType() != null ? field.getDisplayType() : "INPUT");
        vo.setIsRequired(field.getIsRequired() != null ? field.getIsRequired() : false);
        vo.setDefaultValue(field.getDefaultValue());
        vo.setPlaceholder("请输入" + field.getFieldName());
        vo.setHelpText(field.getHelpText());
        vo.setDescription(field.getDescription());

        // 处理选项配置
        if (field.getOptionConfig() != null && !field.getOptionConfig().isEmpty()) {
            try {
                vo.setOptions(JSON.parseArray(field.getOptionConfig(), MetaOptionVO.class));
            } catch (Exception e) {
                log.warn("解析选项配置失败: {}", field.getOptionConfig());
            }
        }

        // 枚举类型从字典获取选项
        if ("ENUM".equals(field.getFieldType()) && field.getDictCode() != null) {
            List<MetaOptionVO> options = getDictItems(field.getDictCode());
            vo.setOptions(options);
        }

        // 校验规则
        vo.setValidations(buildValidationRules(field));

        // 权限配置（暂不设置，由后端控制）
        vo.setReadonly(false);
        vo.setHidden("HIDDEN".equals(field.getFieldLevel()));

        return vo;
    }

    /**
     * 构建校验规则
     */
    private List<MetaValidationVO> buildValidationRules(MetaField field) {
        List<MetaValidationVO> rules = new ArrayList<>();

        // 必填校验
        if (Boolean.TRUE.equals(field.getIsRequired())) {
            rules.add(new MetaValidationVO("required", field.getFieldName() + "不能为空", null));
        }

        // 自定义正则校验
        if (field.getValidationRule() != null && !field.getValidationRule().isEmpty()) {
            rules.add(new MetaValidationVO("pattern", "格式不正确", field.getValidationRule()));
        }

        // 类型校验
        if ("INT".equals(field.getFieldType()) || "BIGINT".equals(field.getFieldType())) {
            rules.add(new MetaValidationVO("type", "请输入整数", null));
        }

        return rules;
    }

    /**
     * 获取字典项
     */
    public List<MetaOptionVO> getDictItems(String dictCode) {
        LambdaQueryWrapper<MetaDataDictExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaDataDictExt::getDictCode, dictCode)
                .eq(MetaDataDictExt::getIsActive, true)
                .orderByAsc(MetaDataDictExt::getSortOrder);
        List<MetaDataDictExt> items = dictExtMapper.selectList(wrapper);

        return items.stream()
                .map(item -> {
                    MetaOptionVO vo = new MetaOptionVO();
                    vo.setValue(item.getItemCode());
                    vo.setLabel(item.getItemName());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取联动配置（字段间联动关系）
     */
    public List<MetaFieldLinkageVO> getFieldLinkages(Long modelId) {
        // 暂时返回空列表，联动配置可以通过extra_config扩展
        return List.of();
    }

    /**
     * 获取校验规则（用于前端同步校验）
     */
    public Map<String, List<MetaValidationVO>> getValidationRules(Long modelId) {
        LambdaQueryWrapper<MetaField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaField::getModelId, modelId)
                .eq(MetaField::getIsActive, true);
        List<MetaField> fields = metaFieldMapper.selectList(wrapper);

        Map<String, List<MetaValidationVO>> rulesMap = new HashMap<>();
        for (MetaField field : fields) {
            rulesMap.put(field.getFieldCode(), buildValidationRules(field));
        }

        return rulesMap;
    }
}
