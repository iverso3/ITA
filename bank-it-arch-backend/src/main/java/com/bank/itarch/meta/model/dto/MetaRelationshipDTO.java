package com.bank.itarch.meta.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关系定义DTO
 */
@Data
public class MetaRelationshipDTO {

    private Long id;
    private String relCode;
    private String relName;
    private String relType;
    private Long sourceModelId;
    private Long sourceFieldId;
    private Long targetModelId;
    private Long targetFieldId;
    private Boolean isCascadeDelete;
    private Boolean isSelfRel;
    private String relConfig;
    private String description;
    private Boolean isActive;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String creator;
    private String modifier;

    // ========== 冗余显示 ==========
    private String sourceModelName;
    private String targetModelName;
}
