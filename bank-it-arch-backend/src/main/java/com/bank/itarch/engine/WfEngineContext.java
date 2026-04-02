package com.bank.itarch.engine;

import com.bank.itarch.engine.node.WfNodeHandler;
import com.bank.itarch.mapper.*;
import com.bank.itarch.model.entity.*;
import com.bank.itarch.model.dto.WfNodeDTO;
import com.bank.itarch.model.dto.WfLineDTO;
import lombok.Data;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程引擎上下文
 */
@Data
public class WfEngineContext implements Serializable {
    private static final long serialVersionUID = 1L;

    // 流程定义
    private WfDefinition definition;
    private List<WfDefinitionNode> nodes;
    private List<WfDefinitionLine> lines;

    // 流程实例
    private WfInstance instance;

    // 流程变量
    private Map<String, Object> variables = new HashMap<>();

    // Mapper依赖
    private transient WfDefinitionMapper definitionMapper;
    private transient WfDefinitionNodeMapper nodeMapper;
    private transient WfDefinitionLineMapper lineMapper;
    private transient WfInstanceMapper instanceMapper;
    private transient WfInstanceVariableMapper variableMapper;
    private transient WfTaskMapper taskMapper;
    private transient WfTaskCandidateMapper candidateMapper;
    private transient WfHistoryMapper historyMapper;
    private transient WfTokenMapper tokenMapper;
    private transient WfParallelJoinMapper parallelJoinMapper;
    private transient WfTaskCounterSignMapper counterSignMapper;
    private transient WfTaskTimeoutRuleMapper timeoutRuleMapper;

    // 节点处理器映射
    private transient Map<String, WfNodeHandler> nodeHandlers = new HashMap<>();

    // 当前操作用户
    private String operatorId;
    private String operatorName;

    /**
     * 根据节点ID获取节点
     */
    public WfDefinitionNode getNodeById(Long nodeId) {
        if (nodes == null || nodeId == null) return null;
        return nodes.stream()
                .filter(n -> n.getId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据节点编码获取节点
     */
    public WfDefinitionNode getNodeByCode(String nodeCode) {
        if (nodes == null || nodeCode == null) return null;
        return nodes.stream()
                .filter(n -> nodeCode.equals(n.getNodeCode()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取节点的所有出口连线
     */
    public List<WfDefinitionLine> getOutgoingLines(Long nodeId) {
        if (lines == null || nodeId == null) return List.of();
        return lines.stream()
                .filter(l -> l.getSourceNodeId().equals(nodeId))
                .toList();
    }

    /**
     * 获取节点的所有入口连线
     */
    public List<WfDefinitionLine> getIncomingLines(Long nodeId) {
        if (lines == null || nodeId == null) return List.of();
        return lines.stream()
                .filter(l -> l.getTargetNodeId().equals(nodeId))
                .toList();
    }

    /**
     * 获取开始节点
     */
    public WfDefinitionNode getStartNode() {
        if (nodes == null) return null;
        return nodes.stream()
                .filter(n -> "START".equals(n.getNodeCategory()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取结束节点
     */
    public WfDefinitionNode getEndNode() {
        if (nodes == null) return null;
        return nodes.stream()
                .filter(n -> "END".equals(n.getNodeCategory()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取变量值
     */
    public Object getVariable(String key) {
        return variables.get(key);
    }

    /**
     * 设置变量值
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }
}
