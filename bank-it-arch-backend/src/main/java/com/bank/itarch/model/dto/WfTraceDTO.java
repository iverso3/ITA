package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程轨迹图DTO
 */
@Data
public class WfTraceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 流程定义信息 */
    private Long definitionId;
    private String definitionName;

    /** 当前实例信息 */
    private Long instanceId;
    private String instanceCode;
    private String title;
    private String status;

    /** 流程节点列表（按顺序） */
    private List<NodeTrace> nodes;

    /** 流程连线列表 */
    private List<LineTrace> lines;

    /** 当前所在的节点ID */
    private Long currentNodeId;

    @Data
    public static class NodeTrace implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long nodeId;
        private String nodeCode;
        private String nodeName;
        private String nodeCategory;  // START/APPROVAL/END/CONDITION/PARALLEL_BRANCH/PARALLEL_JOIN
        private String approvalType;  // SINGLE/GRAB/MULTI_COUNTER/MULTI_OR
        private Integer nodeOrder;
        private Double positionX;
        private Double positionY;

        /** 节点访问序号，用于区分同一节点被多次访问（如驳回后重新到达） */
        private Integer visitIndex;

        /** 该节点是否已执行 */
        private boolean executed;
        /** 该节点是否当前节点（等待处理中） */
        private boolean current;
        /** 审批人信息（如果已分配） */
        private Long assigneeId;
        private String assigneeName;
        /** 审批结果（如果已执行） */
        private String action;        // START/APPROVE/REJECT/TRANSFER/DELEGATE/CLAIM
        private String opinion;
        private LocalDateTime actionTime;
        /** 操作人 */
        private String operatorName;
        /** 关联的任务ID（用于驳回后新任务的场景） */
        private Long taskId;
    }

    @Data
    public static class LineTrace implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long lineId;
        private Long sourceNodeId;
        private Long targetNodeId;
        private String lineName;
        private String lineCode;
        private String conditionExpr;
        private Integer lineOrder;

        /** 该连线是否已流转过 */
        private boolean traversed;
    }
}
