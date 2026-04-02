package com.bank.itarch.engine.node;

import com.bank.itarch.engine.WfEngineContext;
import com.bank.itarch.engine.execution.WfToken;
import com.bank.itarch.model.entity.WfDefinitionLine;
import com.bank.itarch.model.entity.WfDefinitionNode;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

/**
 * 节点处理器抽象类
 */
@Slf4j
public abstract class AbstractNodeHandler implements WfNodeHandler {

    @Override
    public void enter(WfEngineContext context, WfToken token) {
        log.debug("进入节点: {}", token.getCurrentNodeCode());
        token.setCurrentNodeId(token.getCurrentNodeId());
    }

    @Override
    public void complete(WfEngineContext context, WfToken token) {
        log.info("AbstractNodeHandler.complete: nodeCode={}, currentNodeId={}", token.getCurrentNodeCode(), token.getCurrentNodeId());
        leave(context, token);
    }

    @Override
    public void leave(WfEngineContext context, WfToken token) {
        log.info("AbstractNodeHandler.leave: token currentNodeId={}", token.getCurrentNodeId());

        WfDefinitionNode currentNode = context.getNodeById(token.getCurrentNodeId());
        if (currentNode == null) {
            log.error("未找到节点: {}", token.getCurrentNodeId());
            return;
        }

        // 获取出口连线并流转
        List<WfDefinitionLine> outgoingLines = context.getOutgoingLines(currentNode.getId());
        log.info("节点 {} 有 {} 条出口连线", currentNode.getNodeCode(), outgoingLines.size());

        if (outgoingLines.isEmpty()) {
            log.warn("节点 {} 没有出口连线", currentNode.getNodeCode());
            return;
        }

        // 流转到下一个节点
        WfDefinitionLine nextLine = selectNextLine(context, token, outgoingLines);
        log.info("选择的下一条连线: targetNodeId={}", nextLine != null ? nextLine.getTargetNodeId() : "null");

        if (nextLine != null) {
            WfDefinitionNode nextNode = context.getNodeById(nextLine.getTargetNodeId());
            log.info("下一节点: id={}, code={}, category={}", nextNode != null ? nextNode.getId() : "null", nextNode != null ? nextNode.getNodeCode() : "null", nextNode != null ? nextNode.getNodeCategory() : "null");

            if (nextNode != null) {
                token.setCurrentNodeId(nextNode.getId());
                token.setCurrentNodeCode(nextNode.getNodeCode());
                token.addHistory(nextNode.getId());
                log.info("准备进入下一节点: {} -> {}", currentNode.getNodeCode(), nextNode.getNodeCode());

                // 获取下一节点的处理器并调用其enter
                WfNodeHandler nextHandler = context.getNodeHandlers().get(nextNode.getNodeCategory());
                if (nextHandler != null) {
                    nextHandler.enter(context, token);
                } else {
                    log.error("未找到节点处理器: {}", nextNode.getNodeCategory());
                }
            }
        }
    }

    /**
     * 选择下一条连线（被子类重写实现条件分支等逻辑）
     */
    protected WfDefinitionLine selectNextLine(WfEngineContext context, WfToken token,
                                               List<WfDefinitionLine> outgoingLines) {
        if (outgoingLines.size() == 1) {
            return outgoingLines.get(0);
        }
        // 默认返回第一条连线，子类需要重写实现条件选择
        return outgoingLines.get(0);
    }
}
