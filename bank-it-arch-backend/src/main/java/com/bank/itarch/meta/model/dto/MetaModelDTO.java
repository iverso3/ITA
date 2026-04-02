package com.bank.itarch.meta.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 元模型DTO
 */
@Data
public class MetaModelDTO {

    private Long id;
    private String modelCode;
    private String modelName;
    private String modelType;
    private String tableName;
    private String tableAlias;
    private Long parentModelId;
    private String treePath;
    private Integer version;
    private String status;
    private Boolean isGeneratable;
    private Boolean isActive;
    private String description;
    private String extraConfig;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String creator;
    private String modifier;

    // ========== 关联数据 ==========
    private List<MetaFieldDTO> fields;
    private List<MetaGroupDTO> groups;
    private List<MetaRelationshipDTO> relationships;
}
