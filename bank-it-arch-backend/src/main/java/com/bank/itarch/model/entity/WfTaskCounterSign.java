package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 会签/或签记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_task_counter_sign")
public class WfTaskCounterSign extends BaseEntity {
    private Long taskId;
    private String userId;
    private Integer approved;
    private String comment;
    private LocalDateTime signTime;
}
