package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 流程定义DTO
 */
@Data
public class WfDefinitionDTO implements Serializable {
    private Long id;
    private String definitionCode;
    private String definitionName;
    private String processType;
    private String description;
    private Integer version;
    private Integer isActive;
    private String config;
    private String remark;
    private String status;
}
