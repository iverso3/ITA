package com.bank.itarch.meta.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字段定义DTO
 */
@Data
public class MetaFieldDTO {

    private Long id;
    private String fieldCode;
    private String fieldName;
    private String fieldType;
    private String storageMode;
    private String jsonPath;
    private String columnName;
    private String columnType;
    private Integer columnSize;
    private String dictCode;
    private String validationRule;
    private String defaultValue;
    private Boolean isRequired;
    private Boolean isUnique;
    private Boolean isIndexed;
    private Boolean isQueryable;
    private Boolean isSensitive;
    private String sensitiveType;
    private Long groupId;
    private Long modelId;
    private String fieldLevel;
    private String displayType;
    private String optionConfig;
    private Integer sortOrder;
    private String description;
    private String helpText;
    private Boolean isActive;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String creator;
    private String modifier;
}
