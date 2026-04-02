package com.bank.itarch.engine.node;

import com.bank.itarch.engine.WfEngineContext;
import com.bank.itarch.engine.execution.WfToken;
import com.bank.itarch.model.entity.*;
import com.bank.itarch.mapper.WfParallelJoinMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 并行分支节点处理器
 * 创建多个并行执行的分支
 */
@Slf4j
@Component
public class ParallelBranchHandler extends AbstractNodeHandler {

    @Override
    public String getNodeType() {
        return "PARALLEL_BRANCH";
    }

    @Override
    public void enter(WfEngineContext context, WfToken token) {
        log.info("进入并行分支节点: nodeId={}, instanceId={}",
                token.getCurrentNodeId(), token.getInstance().getId());

        WfDefinitionNode node = context.getNodeById(token.getCurrentNodeId());
        if (node == null) {
            log.error("未找到并行分支节点");
            return;
        }

        // 重入保护：如果令牌已经在并行分支节点且是WAITING状态，说明是子令牌流转回来，不需要再次创建分支
        if (WfToken.STATUS_WAITING.equals(token.getTokenStatus())) {
            log.info("并行分支节点已在处理中，跳过重复进入");
            return;
        }

        // 先更新父令牌的当前节点和状态
        token.setCurrentNodeId(node.getId());
        token.setCurrentNodeCode(node.getNodeCode());
        token.setTokenStatus(WfToken.STATUS_WAITING);
        // 同步更新数据库中的令牌状态（需要转换为实体类型）
        com.bank.itarch.model.entity.WfToken tokenEntity = new com.bank.itarch.model.entity.WfToken();
        tokenEntity.setId(token.getId());
        tokenEntity.setInstanceId(token.getInstance().getId());
        tokenEntity.setCurrentNodeId(node.getId());
        tokenEntity.setTokenStatus(WfToken.STATUS_WAITING);
        context.getTokenMapper().updateById(tokenEntity);

        super.enter(context, token);

        // 获取所有出口连线（并行分支）
        List<WfDefinitionLine> outgoingLines = context.getOutgoingLines(node.getId());
        if (outgoingLines.isEmpty()) {
            log.warn("并行分支节点没有出口连线");
            return;
        }

        // 创建并行分支记录
        Long instanceId = token.getInstance().getId();
        for (WfDefinitionLine line : outgoingLines) {
            WfParallelJoin join = new WfParallelJoin();
            join.setInstanceId(instanceId);
            join.setParallelNodeId(node.getId()); // 记录并行组ID
            join.setBranchNodeId(line.getTargetNodeId());
            join.setBranchStatus("RUNNING");
            context.getParallelJoinMapper().insert(join);
        }

        // 为每个分支创建子令牌
        for (WfDefinitionLine line : outgoingLines) {
            WfDefinitionNode targetNode = context.getNodeById(line.getTargetNodeId());
            if (targetNode != null) {
                WfToken childToken = new WfToken(token.getInstance(), targetNode.getId(), targetNode.getNodeCode());
                childToken.setParentToken(token);
                token.addChildToken(childToken);

                // 设置子令牌状态为ACTIVE
                childToken.setTokenStatus(WfToken.STATUS_ACTIVE);
                // 将子令牌信息写入数据库，以便getTokenByInstance能查到
                com.bank.itarch.model.entity.WfToken childTokenEntity = new com.bank.itarch.model.entity.WfToken();
                childTokenEntity.setInstanceId(token.getInstance().getId());
                childTokenEntity.setCurrentNodeId(targetNode.getId());
                childTokenEntity.setTokenStatus(WfToken.STATUS_ACTIVE);
                context.getTokenMapper().insert(childTokenEntity);
                // 同步childToken的id
                childToken.setId(childTokenEntity.getId());

                childToken.addHistory(targetNode.getId());

                // 调用分支节点的enter
                WfNodeHandler handler = context.getNodeHandlers().get(targetNode.getNodeCategory());
                if (handler != null) {
                    handler.enter(context, childToken);
                }
            }
        }

        log.info("创建 {} 个并行分支", outgoingLines.size());
    }

    @Override
    public void leave(WfEngineContext context, WfToken token) {
        // 并行分支节点的leave由汇聚节点触发
    }
}
