package com.bank.itarch.meta.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 字段分组DTO
 */
@Data
public class MetaGroupDTO {

    private Long id;
    private String groupCode;
    private String groupName;
    private String groupType;
    private Long modelId;
    private Integer sortOrder;
    private String description;
    private Boolean isActive;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String creator;
    private String modifier;

    // ========== 关联数据 ==========
    private List<MetaFieldDTO> fields;
}
