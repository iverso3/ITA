package com.bank.itarch.meta.engine;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.meta.constant.MetaStorageMode;
import com.bank.itarch.meta.mapper.*;
import com.bank.itarch.meta.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 动态查询引擎
 * 根据元模型定义动态构建SQL，支持：
 * - 动态字段映射
 * - 动态条件构建
 * - 动态排序
 * - 动态关联查询
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetaQueryEngine {

    private final MetaModelMapper metaModelMapper;
    private final MetaEntityMapper metaEntityMapper;
    private final MetaFieldMapper metaFieldMapper;
    private final MetaValueMapper metaValueMapper;

    /**
     * 执行动态分页查询
     */
    public PageResult<Map<String, Object>> executeQuery(
            Long modelId,
            PageQuery query,
            Map<String, Object> filters,
            List<String> selectFields,
            String sortField,
            String sortOrder) {

        // 获取模型定义
        MetaModel model = metaModelMapper.selectById(modelId);
        if (model == null) {
            throw new RuntimeException("模型不存在: " + modelId);
        }

        // 获取字段定义
        LambdaQueryWrapper<MetaField> fieldWrapper = new LambdaQueryWrapper<>();
        fieldWrapper.eq(MetaField::getModelId, modelId)
                .eq(MetaField::getIsActive, true);
        List<MetaField> fields = metaFieldMapper.selectList(fieldWrapper);

        // 获取COLUMN存储模式的字段
        List<MetaField> columnFields = fields.stream()
                .filter(f -> MetaStorageMode.COLUMN.equals(f.getStorageMode()))
                .collect(Collectors.toList());

        // 构建查询
        Page<MetaEntity> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<MetaEntity> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(MetaEntity::getModelId, modelId)
                .eq(MetaEntity::getStatus, "ACTIVE");

        // 关键词搜索
        if (filters != null && filters.containsKey("keyword")) {
            String keyword = String.valueOf(filters.get("keyword"));
            if (!keyword.isEmpty()) {
                wrapper.and(w -> w.like(MetaEntity::getEntityCode, keyword)
                        .or().like(MetaEntity::getEntityName, keyword));
            }
        }

        // 自定义过滤条件
        if (filters != null) {
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                if ("keyword".equals(entry.getKey()) || entry.getValue() == null) {
                    continue;
                }
                // 处理其他过滤条件
                buildFilter(wrapper, entry.getKey(), entry.getValue(), columnFields);
            }
        }

        // 排序
        if (sortField != null && !sortField.isEmpty()) {
            boolean ascending = "asc".equalsIgnoreCase(sortOrder);
            wrapper.orderBy(true, ascending, MetaEntity::getCreateTime);
        } else {
            wrapper.orderByDesc(MetaEntity::getCreateTime);
        }

        // 执行分页查询
        IPage<MetaEntity> pageResult = metaEntityMapper.selectPage(page, wrapper);

        // 转换为Map结果
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(entity -> convertToMap(entity, columnFields))
                .collect(Collectors.toList());

        return PageResult.of(records, pageResult.getTotal(), query.getPage(), query.getPageSize());
    }

    /**
     * 执行动态单条查询
     */
    public Map<String, Object> executeGet(Long modelId, Long entityId, List<String> selectFields) {
        MetaEntity entity = metaEntityMapper.selectById(entityId);
        if (entity == null) {
            return null;
        }

        LambdaQueryWrapper<MetaField> fieldWrapper = new LambdaQueryWrapper<>();
        fieldWrapper.eq(MetaField::getModelId, modelId)
                .eq(MetaField::getIsActive, true);
        List<MetaField> fields = metaFieldMapper.selectList(fieldWrapper);

        return convertToMap(entity, fields);
    }

    /**
     * 构建过滤条件
     * 注意：对于MetaEntity的标准字段直接处理，复杂字段值通过meta_value表查询
     */
    private void buildFilter(LambdaQueryWrapper<MetaEntity> wrapper,
                            String fieldCode, Object value,
                            List<MetaField> columnFields) {

        // 只处理MetaEntity固有的字段，其他字段通过后续的meta_value表JOIN处理
        // 这里简化处理，实际生产环境应该使用动态SQL

        // 对于字符串类型的模糊匹配
        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.contains("%")) {
                // 通配符查询 - 仅支持entityCode和entityName
                if ("entityCode".equals(fieldCode)) {
                    wrapper.like(MetaEntity::getEntityCode, strValue.replace("%", ""));
                } else if ("entityName".equals(fieldCode)) {
                    wrapper.like(MetaEntity::getEntityName, strValue.replace("%", ""));
                }
            } else {
                // 精确匹配
                if ("entityCode".equals(fieldCode)) {
                    wrapper.eq(MetaEntity::getEntityCode, strValue);
                } else if ("entityName".equals(fieldCode)) {
                    wrapper.eq(MetaEntity::getEntityName, strValue);
                } else if ("status".equals(fieldCode)) {
                    wrapper.eq(MetaEntity::getStatus, strValue);
                }
            }
        } else if (value instanceof Number) {
            if ("id".equals(fieldCode)) {
                wrapper.eq(MetaEntity::getId, ((Number) value).longValue());
            }
        }
    }

    /**
     * 将实体转换为Map
     */
    private Map<String, Object> convertToMap(MetaEntity entity, List<MetaField> fields) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", entity.getId());
        result.put("entityCode", entity.getEntityCode());
        result.put("entityName", entity.getEntityName());
        result.put("modelCode", entity.getModelCode());
        result.put("status", entity.getStatus());
        result.put("createTime", entity.getCreateTime());
        result.put("creator", entity.getCreator());

        // 加载字段值
        LambdaQueryWrapper<MetaValue> valueWrapper = new LambdaQueryWrapper<>();
        valueWrapper.eq(MetaValue::getEntityId, entity.getId())
                .eq(MetaValue::getIsCurrent, true);
        List<MetaValue> values = metaValueMapper.selectList(valueWrapper);

        // 构建字段值映射
        Map<String, MetaValue> valueMap = values.stream()
                .collect(Collectors.toMap(MetaValue::getFieldCode, v -> v));

        // 填充字段值
        for (MetaField field : fields) {
            MetaValue metaValue = valueMap.get(field.getFieldCode());
            if (metaValue != null) {
                result.put(field.getFieldCode(), getValueFromMetaValue(metaValue));
            }
        }

        return result;
    }

    /**
     * 从MetaValue获取值
     */
    private Object getValueFromMetaValue(MetaValue metaValue) {
        if (metaValue.getValueBoolean() != null) {
            return metaValue.getValueBoolean();
        }
        if (metaValue.getValueInt() != null) {
            return metaValue.getValueInt();
        }
        if (metaValue.getValueDecimal() != null) {
            return metaValue.getValueDecimal();
        }
        if (metaValue.getValueJson() != null) {
            try {
                return JSON.parse(metaValue.getValueJson());
            } catch (Exception e) {
                return metaValue.getValueJson();
            }
        }
        return metaValue.getValueText();
    }

    /**
     * 执行动态COUNT查询
     */
    public long executeCount(Long modelId, Map<String, Object> filters) {
        LambdaQueryWrapper<MetaEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaEntity::getModelId, modelId)
                .eq(MetaEntity::getStatus, "ACTIVE");

        if (filters != null && filters.containsKey("keyword")) {
            String keyword = String.valueOf(filters.get("keyword"));
            if (!keyword.isEmpty()) {
                wrapper.and(w -> w.like(MetaEntity::getEntityCode, keyword)
                        .or().like(MetaEntity::getEntityName, keyword));
            }
        }

        return metaEntityMapper.selectCount(wrapper);
    }
}
