package com.bank.itarch.meta.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 元实体DTO
 */
@Data
public class MetaEntityDTO {

    private Long id;
    private String entityCode;
    private String entityName;
    private Long modelId;
    private String modelCode;
    private Long modelVersionId;
    private Long businessId;
    private String status;
    private String lockReason;
    private LocalDateTime lockedAt;
    private String lockedBy;
    private String ownerId;
    private String ownerType;
    private Long deptId;
    private String deptPath;
    private Integer dataVersion;
    private String description;
    private Long creatorDeptId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String creator;
    private String modifier;

    // ========== 关联数据 ==========
    private Map<String, Object> fieldValues;
    private List<MetaRelationDTO> relations;
    private List<String> tags;
}
