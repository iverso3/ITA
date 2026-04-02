package com.bank.itarch.meta.model.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 关系实例DTO
 */
@Data
public class MetaRelationDTO {

    private Long id;
    private Long relId;
    private String relCode;
    private String relType;
    private Long sourceEntityId;
    private String sourceEntityCode;
    private Long targetEntityId;
    private String targetEntityCode;
    private String relationLevel;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private LocalDateTime createTime;
    private String creator;

    // ========== 冗余显示 ==========
    private String sourceEntityName;
    private String targetEntityName;
    private String relName;
}
