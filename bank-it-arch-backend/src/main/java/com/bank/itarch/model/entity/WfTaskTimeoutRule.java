package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 节点超时规则
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_task_timeout_rule")
public class WfTaskTimeoutRule extends BaseEntity {
    private Long definitionId;
    private Long nodeId;
    private Integer timeoutDuration;
    private String timeoutAction;
    private String timeoutNoticeTimes;
}
