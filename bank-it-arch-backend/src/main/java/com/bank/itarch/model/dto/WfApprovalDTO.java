package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * 审批操作DTO
 */
@Data
public class WfApprovalDTO implements Serializable {
    private Long taskId;
    private String action;
    private String comment;
    private Map<String, Object> variables;

    // 驳回相关
    private String targetNodeCode;
    private Long targetNodeId;

    // 转办相关
    private String assigneeId;
    private String assigneeName;

    // 加签相关
    private String delegateUserId;
    private String delegateUserName;
}
