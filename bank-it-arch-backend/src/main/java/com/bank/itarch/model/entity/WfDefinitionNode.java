package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程节点定义
 */
@Data
@TableName("wf_definition_node")
public class WfDefinitionNode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long definitionId;
    private String nodeCode;
    private String nodeName;
    private Integer nodeOrder;
    private String nodeType;

    // ========== 新增字段 ==========
    /** 节点类别: START/END/APPROVAL/CONDITION/PARALLEL_BRANCH/PARALLEL_JOIN */
    private String nodeCategory;

    /** 审批方式: SINGLE/GRAB/MULTI_COUNTER/MULTI_OR */
    private String approvalType;

    /** 会签要求人数(仅会签模式使用) */
    private Integer counterSignCount;

    /** 审批角色ID */
    private Long assignedRoleId;

    /** 流程角色ID(用于oss_flow_role_user_rel表) */
    private String flowRoleId;

    /** 超时时长(分钟) */
    private Integer timeoutDuration;

    /** 超时动作: TRANSFER/SKIP/AUTO_APPROVE */
    private String timeoutAction;

    /** 超时转办人 */
    private String timeoutTransferUser;

    /** 表单配置JSON */
    private String formSchema;

    /** 通知配置JSON */
    private String noticeConfig;

    /** 扩展属性JSON */
    private String extraAttrs;

    /** 节点X坐标 */
    private Double positionX;

    /** 节点Y坐标 */
    private Double positionY;
    // ========== 新增字段 END ==========

    private String approverType;
    private Long approverId;
    private String approverName;
    private Integer isRequired;
    private String approveType;
    private Integer timeoutHours;
    private Integer autoPass;
    private String conditionRule;
    private String remark;
}
