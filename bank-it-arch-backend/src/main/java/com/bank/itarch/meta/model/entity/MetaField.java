package com.bank.itarch.meta.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bank.itarch.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字段定义表
 * 定义模型的所有字段，包括字段类型、存储模式、校验规则、脱敏配置等
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meta_field")
public class MetaField extends BaseEntity {

    /** 字段编码（英文） */
    private String fieldCode;

    /** 字段名称（中文） */
    private String fieldName;

    /** 字段类型（STRING/INT/BIGINT/DECIMAL/BOOLEAN/DATE/DATETIME/TEXT/JSON/FILE/ENUM） */
    private String fieldType;

    /** 存储模式（COLUMN/JSON/TAG） */
    private String storageMode;

    /** JSON路径（storage_mode=JSON时使用） */
    private String jsonPath;

    /** 物理列名（storage_mode=COLUMN时使用） */
    private String columnName;

    /** 物理列类型 */
    private String columnType;

    /** 列大小 */
    private Integer columnSize;

    /** 字典编码（枚举类型使用） */
    private String dictCode;

    /** 校验规则（正则表达式） */
    private String validationRule;

    /** 默认值 */
    private String defaultValue;

    /** 是否必填 */
    private Boolean isRequired;

    /** 是否唯一 */
    private Boolean isUnique;

    /** 是否索引 */
    private Boolean isIndexed;

    /** 是否可查询 */
    private Boolean isQueryable;

    /** 是否敏感（脱敏） */
    private Boolean isSensitive;

    /** 敏感类型（PHONE/ID_CARD/EMAIL/BANK_CARD） */
    private String sensitiveType;

    /** 所属分组ID */
    private Long groupId;

    /** 所属模型ID */
    private Long modelId;

    /** 字段级别（NORMAL/SYSTEM/HIDDEN） */
    private String fieldLevel;

    /** 前端显示类型（INPUT/SELECT/DATE/DATERANGE/IMAGE/FILE/EDITOR/SWITCH） */
    private String displayType;

    /** 选项配置（下拉/单选/多选等） */
    private String optionConfig;

    /** 排序 */
    private Integer sortOrder;

    /** 描述 */
    private String description;

    /** 帮助文本 */
    private String helpText;

    /** 是否启用 */
    private Boolean isActive;
}
