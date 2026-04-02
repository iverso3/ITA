package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 流程实例变量
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_instance_variable")
public class WfInstanceVariable extends BaseEntity {
    private Long instanceId;
    private String variableKey;
    private String variableValue;
}
