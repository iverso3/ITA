package com.bank.itarch.engine.node;

import com.bank.itarch.engine.WfEngineContext;
import com.bank.itarch.engine.execution.WfToken;
import com.bank.itarch.model.entity.WfDefinitionLine;
import com.bank.itarch.model.entity.WfDefinitionNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 开始节点处理器
 */
@Slf4j
@Component
public class StartNodeHandler extends AbstractNodeHandler {

    @Override
    public String getNodeType() {
        return "START";
    }

    @Override
    public void enter(WfEngineContext context, WfToken token) {
        log.info("进入开始节点: instanceId={}", token.getInstance().getId());
        super.enter(context, token);

        WfDefinitionNode startNode = context.getNodeById(token.getCurrentNodeId());
        if (startNode == null) {
            log.error("未找到开始节点");
            return;
        }

        // 检查出口连线
        List<WfDefinitionLine> outgoingLines = context.getOutgoingLines(startNode.getId());
        log.info("开始节点出口连线数量: {}", outgoingLines.size());
        for (WfDefinitionLine line : outgoingLines) {
            WfDefinitionNode targetNode = context.getNodeById(line.getTargetNodeId());
            log.info("  连线: {} -> {} (targetNodeName={})",
                line.getSourceNodeId(), line.getTargetNodeId(),
                targetNode != null ? targetNode.getNodeName() : "null");
        }

        log.info("开始节点完成，自动流转到下一节点");
        // 开始节点进入后自动流转到下一节点
        flowToNextNode(context, token);
    }

    @Override
    public void complete(WfEngineContext context, WfToken token) {
        log.debug("开始节点任务完成，流转到下一节点");
        // 自动流转到下一节点
        flowToNextNode(context, token);
    }

    @Override
    public void leave(WfEngineContext context, WfToken token) {
        // 开始节点离开时自动流转
        flowToNextNode(context, token);
    }

    /**
     * 流转到下一个节点
     */
    private void flowToNextNode(WfEngineContext context, WfToken token) {
        WfDefinitionNode currentNode = context.getNodeById(token.getCurrentNodeId());
        if (currentNode == null) {
            log.error("flowToNextNode: 未找到当前节点: {}", token.getCurrentNodeId());
            return;
        }
        log.info("flowToNextNode(), currentNode={}, currentNodeId={}", currentNode.getNodeCode(), currentNode.getId());

        List<WfDefinitionLine> outgoingLines = context.getOutgoingLines(currentNode.getId());
        log.info("flowToNextNode: 出口连线数量={}", outgoingLines.size());
        if (outgoingLines.isEmpty()) {
            log.warn("flowToNextNode: 节点 {} 没有出口连线", currentNode.getNodeCode());
            return;
        }

        WfDefinitionLine nextLine = selectNextLine(context, token, outgoingLines);
        log.info("flowToNextNode: 选择的连线: {}", nextLine != null ? nextLine.getLineCode() : "null");
        if (nextLine != null) {
            WfDefinitionNode nextNode = context.getNodeById(nextLine.getTargetNodeId());
            log.info("flowToNextNode: 下一节点: {}", nextNode != null ? nextNode.getNodeName() : "null");
            if (nextNode != null) {
                log.info("流转到下一节点: {} -> {}", currentNode.getNodeCode(), nextNode.getNodeCode());

                // 更新令牌
                token.setCurrentNodeId(nextNode.getId());
                token.setCurrentNodeCode(nextNode.getNodeCode());
                token.addHistory(nextNode.getId());

                // 更新数据库中的令牌
                var tokenEntity = context.getTokenMapper().selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.bank.itarch.model.entity.WfToken>()
                        .eq(com.bank.itarch.model.entity.WfToken::getInstanceId, token.getInstance().getId())
                        .eq(com.bank.itarch.model.entity.WfToken::getDeleted, 0));
                if (tokenEntity != null) {
                    tokenEntity.setCurrentNodeId(nextNode.getId());
                    tokenEntity.setTokenStatus(WfToken.STATUS_ACTIVE);
                    context.getTokenMapper().updateById(tokenEntity);
                }

                // 调用下一个节点的enter
                WfNodeHandler handler = getNodeHandler(context, nextNode.getNodeCategory());
                log.info("获取到下一节点处理器: {} -> category={}, handler={}", nextNode.getNodeCode(), nextNode.getNodeCategory(), handler);
                if (handler != null) {
                    handler.enter(context, token);
                } else {
                    log.error("未找到处理器: {}", nextNode.getNodeCategory());
                }
            }
        }
    }

    /**
     * 根据节点类型获取处理器
     */
    private WfNodeHandler getNodeHandler(WfEngineContext context, String nodeCategory) {
        WfNodeHandler handler = context.getNodeHandlers().get(nodeCategory);
        if (handler == null) {
            log.error("未找到节点处理器: {}", nodeCategory);
        }
        return handler;
    }

    protected WfDefinitionLine selectNextLine(WfEngineContext context, WfToken token,
                                               List<WfDefinitionLine> outgoingLines) {
        if (outgoingLines.size() == 1) {
            return outgoingLines.get(0);
        }
        return outgoingLines.get(0);
    }
}
