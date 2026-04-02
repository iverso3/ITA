package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批任务
 */
@Data
@TableName("wf_task")
public class WfTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long instanceId;
    private Long nodeId;
    private String taskCode;
    private String taskName;
    private String taskType;

    // ========== 新增字段 ==========
    /** 任务类型: NORMAL/COUNTER_SIGN/SUB_TASK */
    private String taskCategory;

    /** 父任务ID(加签时) */
    private Long parentTaskId;

    /** 会签类型: COUNTER/OR */
    private String counterSignType;

    /** 会签要求人数 */
    private Integer counterSignCount;

    /** 已审批人数 */
    private Integer counterSignApproved;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 是否已超时 */
    private Integer isTimeout;

    /** 任务动作: APPROVE/REJECT/TRANSFER/DELEGATE/RETURN */
    private String taskAction;

    /** 签收时间 */
    private LocalDateTime claimTime;
    // ========== 新增字段 END ==========

    private Long assigneeId;
    private String assigneeName;
    private String status;
    private String approveType;
    private Integer approvedCount;
    private Integer requiredCount;
    private LocalDateTime createTime;
    private LocalDateTime completeTime;
    private Long delegateFrom;
    private Long delegateTo;
    private String opinion;
    private String remark;
}
