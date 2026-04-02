package com.bank.itarch.meta.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型版本DTO
 */
@Data
public class MetaModelVersionDTO {

    private Long id;
    private Long modelId;
    private String modelCode;
    private String modelName;
    private Integer version;
    private String versionName;
    private String status;
    private String changeSummary;
    private String diffContent;
    private LocalDateTime publishedAt;
    private String publishedBy;
    private LocalDateTime archivedAt;
    private String archivedBy;
    private Integer rollbackFromVersion;
    private LocalDateTime createTime;
    private String creator;
}
