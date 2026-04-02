package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * 发起流程DTO
 */
@Data
public class WfStartProcessDTO implements Serializable {
    private Long definitionId;
    private Integer version;
    private String businessType;
    private Long businessId;
    private String businessKey;
    private String title;
    private Map<String, Object> variables;
}
