package com.bank.itarch.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.io.Serializable;

/**
 * 流程节点DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WfNodeDTO implements Serializable {
    // 使用Object以便同时接受前端字符串ID和数据库Long ID
    private Object id;
    private Long definitionId;
    private String nodeCode;
    private String nodeName;
    private Integer nodeOrder;
    private String nodeType;

    // 节点类别: START/END/APPROVAL/CONDITION/PARALLEL_BRANCH/PARALLEL_JOIN
    private String nodeCategory;

    // 审批方式: SINGLE/GRAB/MULTI_COUNTER/MULTI_OR
    private String approvalType;

    // 审批角色ID
    private Long assignedRoleId;

    // 审批角色名称
    private String assignedRoleName;

    // 流程角色ID(用于oss_flow_role_user_rel表)
    private String flowRoleId;

    // 流程角色名称
    private String flowRoleName;

    // 超时时长(分钟)
    private Integer timeoutDuration;

    // 超时动作: TRANSFER/SKIP/AUTO_APPROVE
    private String timeoutAction;

    // 超时转办人
    private String timeoutTransferUser;

    // 表单配置JSON
    private String formSchema;

    // 通知配置JSON
    private String noticeConfig;

    // 扩展属性JSON
    private String extraAttrs;

    // 条件表达式
    private String conditionRule;

    // Vue Flow位置
    private Double positionX;
    private Double positionY;
}
