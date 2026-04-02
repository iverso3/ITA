package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批任务DTO
 */
@Data
public class WfTaskDTO implements Serializable {
    private Long id;
    private Long instanceId;
    private String instanceCode;
    private String title;
    private Long nodeId;
    private String nodeName;
    private String taskCode;
    private String taskName;

    // 任务类型: NORMAL/COUNTER_SIGN/SUB_TASK
    private String taskCategory;

    // 会签类型: COUNTER/OR
    private String counterSignType;

    // 会签要求人数
    private Integer counterSignCount;

    // 已审批人数
    private Integer counterSignApproved;

    private Long assigneeId;
    private String assigneeName;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    private LocalDateTime completeTime;
    private String opinion;
    private String remark;

    // 流程定义信息
    private Long definitionId;
    private String definitionName;

    // 申请人信息
    private Long applicantId;
    private String applicantName;
    private String applicantDeptName;

    // 业务信息（用于跳转查看详情）
    private String businessType;
    private String businessKey;
    private Long businessId;

    // 节点类别: START/END/APPROVAL/CONDITION/PARALLEL_BRANCH/PARALLEL_JOIN
    private String nodeCategory;
}
