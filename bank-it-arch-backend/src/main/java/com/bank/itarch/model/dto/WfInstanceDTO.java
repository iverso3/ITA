package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 流程实例DTO
 */
@Data
public class WfInstanceDTO implements Serializable {
    private Long id;
    private String instanceCode;
    private Long definitionId;
    private String definitionName;
    private String processType;
    private String businessType;
    private Long businessId;
    private String businessKey;
    private String title;
    private Long applicantId;
    private String applicantName;
    private Long applicantDeptId;
    private String applicantDeptName;
    private Long currentNodeId;
    private String currentNodeName;
    private String status;
    private String priority;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private String result;
    private String businessData;
    private String remark;

    // 流程变量
    private Map<String, Object> variables;
}
