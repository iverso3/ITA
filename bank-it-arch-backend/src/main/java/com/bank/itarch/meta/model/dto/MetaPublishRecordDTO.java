package com.bank.itarch.meta.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发布记录DTO
 */
@Data
public class MetaPublishRecordDTO {

    private Long id;
    private String recordCode;
    private Long modelId;
    private String modelCode;
    private String modelName;
    private Integer versionFrom;
    private Integer versionTo;
    private String actionType;
    private String status;
    private String executeConfig;
    private String executeResult;
    private String errorMessage;
    private String operator;
    private LocalDateTime operateTime;
    private LocalDateTime createTime;
}
