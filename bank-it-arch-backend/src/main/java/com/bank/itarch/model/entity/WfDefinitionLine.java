package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 流程连线定义
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_definition_line")
public class WfDefinitionLine extends BaseEntity {
    private Long definitionId;
    private Long sourceNodeId;
    private Long targetNodeId;
    private String lineCode;
    private String lineName;
    private String conditionExpr;
    private Integer lineOrder;
}
