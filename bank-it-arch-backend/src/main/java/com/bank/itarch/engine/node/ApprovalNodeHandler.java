package com.bank.itarch.engine.node;

import com.bank.itarch.engine.WfEngineContext;
import com.bank.itarch.engine.execution.WfToken;
import com.bank.itarch.engine.route.WfRouteStrategy;
import com.bank.itarch.engine.route.SingleApproveStrategy;
import com.bank.itarch.engine.route.GrabTaskStrategy;
import com.bank.itarch.engine.route.CounterSignStrategy;
import com.bank.itarch.engine.route.OrSignStrategy;
import com.bank.itarch.model.entity.*;
import com.bank.itarch.service.OssFlowRoleUserRelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 审批节点处理器
 */
@Slf4j
@Component
public class ApprovalNodeHandler extends AbstractNodeHandler {

    private final SingleApproveStrategy singleApproveStrategy;
    private final GrabTaskStrategy grabTaskStrategy;
    private final CounterSignStrategy counterSignStrategy;
    private final OrSignStrategy orSignStrategy;
    private final OssFlowRoleUserRelService flowRoleUserRelService;

    public ApprovalNodeHandler(
            SingleApproveStrategy singleApproveStrategy,
            GrabTaskStrategy grabTaskStrategy,
            CounterSignStrategy counterSignStrategy,
            OrSignStrategy orSignStrategy,
            OssFlowRoleUserRelService flowRoleUserRelService) {
        this.singleApproveStrategy = singleApproveStrategy;
        this.grabTaskStrategy = grabTaskStrategy;
        this.counterSignStrategy = counterSignStrategy;
        this.orSignStrategy = orSignStrategy;
        this.flowRoleUserRelService = flowRoleUserRelService;
    }

    @Override
    public String getNodeType() {
        return "APPROVAL";
    }

    @Override
    public void enter(WfEngineContext context, WfToken token) {
        log.info("进入审批节点: nodeId={}, instanceId={}",
                token.getCurrentNodeId(), token.getInstance().getId());
        super.enter(context, token);

        WfDefinitionNode node = context.getNodeById(token.getCurrentNodeId());
        if (node == null) {
            log.error("未找到审批节点: {}", token.getCurrentNodeId());
            return;
        }

        // 更新实例当前节点
        WfInstance instance = token.getInstance();
        instance.setCurrentNodeId(node.getId());
        instance.setCurrentNodeName(node.getNodeName());
        context.getInstanceMapper().updateById(instance);

        // 更新令牌状态为等待
        var tokenEntity = context.getTokenMapper().selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.bank.itarch.model.entity.WfToken>()
                .eq(com.bank.itarch.model.entity.WfToken::getInstanceId, instance.getId())
                .eq(com.bank.itarch.model.entity.WfToken::getTokenStatus, "ACTIVE"));
        if (tokenEntity != null) {
            tokenEntity.setTokenStatus(WfToken.STATUS_WAITING);
            tokenEntity.setCurrentNodeId(node.getId());
            context.getTokenMapper().updateById(tokenEntity);
        }

        // 如果配置了flowRoleId，则为每个关联用户创建任务
        if (node.getFlowRoleId() != null && !node.getFlowRoleId().isEmpty()) {
            List<OssFlowRoleUserRel> users = flowRoleUserRelService.getUsersByFlowRoleId(node.getFlowRoleId());
            if (users == null || users.isEmpty()) {
                throw new RuntimeException("流程角色[flowRoleId=" + node.getFlowRoleId() + "]没有关联的审批人，请先为该角色分配用户");
            }
            // 按userId去重，避免同一用户被分配到同一角色多次时创建重复任务
            Set<String> processedUserIds = new HashSet<>();
            for (OssFlowRoleUserRel user : users) {
                if (!processedUserIds.add(user.getUserId())) {
                    log.warn("跳过重复用户: userId={}, userName={}, flowRoleId={}",
                            user.getUserId(), user.getUserName(), node.getFlowRoleId());
                    continue;
                }
                WfTask task = createTask(context, node, instance);
                task.setAssigneeId(Long.parseLong(user.getUserId()));
                task.setAssigneeName(user.getUserName());
                context.getTaskMapper().updateById(task);
                log.info("审批节点任务已创建(flowRoleId): taskId={}, assigneeId={}, assigneeName={}",
                        task.getId(), user.getUserId(), user.getUserName());
            }
            log.info("审批节点任务已批量创建(flowRoleId), 共{}个任务", processedUserIds.size());
        } else {
            // 创建审批任务
            WfTask task = createTask(context, node, instance);

            // 根据策略处理候选人
            WfRouteStrategy routeStrategy = getRouteStrategy(node.getApprovalType());
            routeStrategy.handleCandidates(context, task, node);

            // 更新任务的审批人信息到数据库
            context.getTaskMapper().updateById(task);

            // 审批节点进入后不自动流转，等待任务完成
            log.info("审批节点任务已创建，等待审批: taskId={}", task.getId());
        }
    }

    private WfTask createTask(WfEngineContext context, WfDefinitionNode node, WfInstance instance) {
        WfTask task = new WfTask();
        task.setInstanceId(instance.getId());
        task.setNodeId(node.getId());
        task.setTaskCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        task.setTaskName(node.getNodeName());
        task.setTaskCategory("NORMAL");
        task.setStatus("PENDING");
        task.setCreateTime(LocalDateTime.now());

        // 设置超时
        if (node.getTimeoutDuration() != null && node.getTimeoutDuration() > 0) {
            task.setExpireTime(LocalDateTime.now().plusMinutes(node.getTimeoutDuration()));
        }

        context.getTaskMapper().insert(task);
        log.info("创建审批任务: taskId={}, nodeName={}", task.getId(), node.getNodeName());

        return task;
    }

    @Override
    public void leave(WfEngineContext context, WfToken token) {
        // 审批完成后，调用父类的leave方法来流转到下一节点
        log.info("ApprovalNodeHandler.leave: nodeId={}, nodeCode={}", token.getCurrentNodeId(), token.getCurrentNodeCode());
        super.leave(context, token);
    }

    private WfRouteStrategy getRouteStrategy(String approvalType) {
        if (approvalType == null) {
            return singleApproveStrategy;
        }
        return switch (approvalType) {
            case "SINGLE" -> singleApproveStrategy;
            case "GRAB" -> grabTaskStrategy;
            case "MULTI_COUNTER" -> counterSignStrategy;
            case "MULTI_OR" -> orSignStrategy;
            default -> singleApproveStrategy;
        };
    }
}
