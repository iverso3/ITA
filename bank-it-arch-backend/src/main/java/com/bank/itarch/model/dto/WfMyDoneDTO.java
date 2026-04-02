package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 我的已办DTO - 包含实例信息和当前审批人信息
 */
@Data
public class WfMyDoneDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String instanceCode;
    private Long definitionId;
    private String definitionName;
    private String businessType;
    private String businessKey;
    private String title;
    private Long applicantId;
    private String applicantName;
    private String applicantDeptName;
    private Long currentNodeId;
    private String currentNodeName;
    private String status;        // RUNNING/COMPLETED/REJECTED/WITHDRAWN/TERMINATED
    private String result;        // APPROVED/REJECTED/WITHDRAWN/TERMINATED
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /** 当前审批人ID（待审批时） */
    private Long currentApproverId;
    /** 当前审批人名称（待审批时） */
    private String currentApproverName;

    /** 当前用户在此实例中的角色：APPLICANT=申请人，APPROVER=审批人 */
    private String myRole;
}
