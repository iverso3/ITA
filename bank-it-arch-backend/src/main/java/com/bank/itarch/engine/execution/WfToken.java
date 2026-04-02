package com.bank.itarch.engine.execution;

import com.bank.itarch.model.entity.WfInstance;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 流程令牌 - 代表流程实例的执行点
 */
@Data
public class WfToken {
    private Long id;
    private WfInstance instance;
    private Long currentNodeId;
    private String currentNodeCode;
    private String tokenStatus;

    // 令牌状态常量
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_WAITING = "WAITING";
    public static final String STATUS_COMPLETED = "COMPLETED";

    // 分支信息（用于并行节点）
    private List<WfToken> childTokens = new ArrayList<>();
    private WfToken parentToken;

    // 历史路径（用于驳回）
    private List<Long> historyNodeIds = new ArrayList<>();

    public WfToken(WfInstance instance, Long nodeId, String nodeCode) {
        this.instance = instance;
        this.currentNodeId = nodeId;
        this.currentNodeCode = nodeCode;
        this.tokenStatus = STATUS_ACTIVE;
    }

    public void addHistory(Long nodeId) {
        this.historyNodeIds.add(nodeId);
    }

    public void addChildToken(WfToken childToken) {
        this.childTokens.add(childToken);
        childToken.setParentToken(this);
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(tokenStatus);
    }

    public boolean isWaiting() {
        return STATUS_WAITING.equals(tokenStatus);
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equals(tokenStatus);
    }
}
