package com.bank.itarch.engine;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bank.itarch.engine.execution.WfToken;
import com.bank.itarch.engine.node.*;
import com.bank.itarch.engine.route.WfRouteStrategy;
import com.bank.itarch.engine.route.SingleApproveStrategy;
import com.bank.itarch.engine.route.CounterSignStrategy;
import com.bank.itarch.engine.route.OrSignStrategy;
import com.bank.itarch.engine.route.GrabTaskStrategy;
import com.bank.itarch.mapper.*;
import com.bank.itarch.model.dto.WfStartProcessDTO;
import com.bank.itarch.model.entity.*;
import com.bank.itarch.model.dto.WfApprovalDTO;
import com.bank.itarch.service.OssImplApplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 流程引擎核心类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WfProcessEngine {

    private final WfDefinitionMapper definitionMapper;
    private final WfDefinitionNodeMapper nodeMapper;
    private final WfDefinitionLineMapper lineMapper;
    private final WfInstanceMapper instanceMapper;
    private final WfInstanceVariableMapper variableMapper;
    private final WfTaskMapper taskMapper;
    private final WfTaskCandidateMapper candidateMapper;
    private final WfHistoryMapper historyMapper;
    private final WfTokenMapper tokenMapper;
    private final WfParallelJoinMapper parallelJoinMapper;
    private final WfTaskCounterSignMapper counterSignMapper;
    private final WfTaskTimeoutRuleMapper timeoutRuleMapper;

    private final OssImplApplyService ossImplApplyService;

    private final StartNodeHandler startNodeHandler;
    private final EndNodeHandler endNodeHandler;
    private final ApprovalNodeHandler approvalNodeHandler;
    private final ConditionNodeHandler conditionNodeHandler;
    private final ParallelBranchHandler parallelBranchHandler;
    private final ParallelJoinHandler parallelJoinHandler;
    private final SingleApproveStrategy singleApproveStrategy;
    private final CounterSignStrategy counterSignStrategy;
    private final OrSignStrategy orSignStrategy;
    private final GrabTaskStrategy grabTaskStrategy;

    /**
     * 发起流程
     */
    @Transactional
    public WfInstance startProcess(WfStartProcessDTO dto, String operatorId, String operatorName) {
        log.info("发起流程: definitionId={}, businessId={}, operator={}",
                dto.getDefinitionId(), dto.getBusinessId(), operatorName);

        // 0. 校验操作人ID
        if (operatorId == null || operatorId.trim().isEmpty()) {
            throw new RuntimeException("操作人ID不能为空，请检查当前用户是否登录");
        }

        // 1. 获取流程定义
        WfDefinition definition = definitionMapper.selectById(dto.getDefinitionId());
        if (definition == null) {
            throw new RuntimeException("流程定义不存在: " + dto.getDefinitionId());
        }

        // 2. 获取流程模型
        List<WfDefinitionNode> nodes = nodeMapper.selectList(
            new LambdaQueryWrapper<WfDefinitionNode>()
                .eq(WfDefinitionNode::getDefinitionId, dto.getDefinitionId())
                .orderByAsc(WfDefinitionNode::getNodeOrder));
        List<WfDefinitionLine> lines = lineMapper.selectList(
            new LambdaQueryWrapper<WfDefinitionLine>()
                .eq(WfDefinitionLine::getDefinitionId, dto.getDefinitionId()));

        if (nodes.isEmpty()) {
            throw new RuntimeException("流程定义没有节点");
        }

        // 3. 找到开始节点
        WfDefinitionNode startNode = nodes.stream()
                .filter(n -> "START".equals(n.getNodeCategory()))
                .findFirst()
                .orElse(nodes.get(0));

        // 4. 创建流程实例
        WfInstance instance = new WfInstance();
        instance.setInstanceCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        instance.setDefinitionId(definition.getId());
        instance.setDefinitionName(definition.getDefinitionName());
        instance.setProcessType(definition.getProcessType());
        instance.setBusinessType(dto.getBusinessType());
        instance.setBusinessId(dto.getBusinessId());
        instance.setBusinessKey(dto.getBusinessKey());
        instance.setTitle(dto.getTitle());
        instance.setApplicantId(Long.parseLong(operatorId));
        instance.setApplicantName(operatorName);
        instance.setCurrentNodeId(startNode.getId());
        instance.setCurrentNodeName(startNode.getNodeName());
        instance.setStatus("RUNNING");
        instance.setStartTime(LocalDateTime.now());
        instanceMapper.insert(instance);

        // 5. 保存流程变量
        if (dto.getVariables() != null && !dto.getVariables().isEmpty()) {
            for (Map.Entry<String, Object> entry : dto.getVariables().entrySet()) {
                WfInstanceVariable variable = new WfInstanceVariable();
                variable.setInstanceId(instance.getId());
                variable.setVariableKey(entry.getKey());
                variable.setVariableValue(entry.getValue() != null ? entry.getValue().toString() : null);
                variableMapper.insert(variable);
            }
        }

        // 6. 创建令牌
        com.bank.itarch.model.entity.WfToken tokenEntity = new com.bank.itarch.model.entity.WfToken();
        tokenEntity.setInstanceId(instance.getId());
        tokenEntity.setCurrentNodeId(startNode.getId());
        tokenEntity.setTokenStatus("ACTIVE");
        tokenMapper.insert(tokenEntity);

        WfToken token = new WfToken(instance, startNode.getId(), startNode.getNodeCode());

        // 7. 创建引擎上下文并执行
        WfEngineContext context = createContext(definition, nodes, lines, instance, dto.getVariables());
        context.setOperatorId(operatorId);
        context.setOperatorName(operatorName);

        // 8. 记录历史
        saveHistory(instance, startNode, operatorId, operatorName, "START", "发起流程", null);

        // 9. 执行开始节点
        log.info("开始执行startNodeHandler.enter(), token currentNodeId={}", token.getCurrentNodeId());
        try {
            startNodeHandler.enter(context, token);
            log.info("startNodeHandler.enter() 执行完成");
        } catch (Exception e) {
            log.error("startNodeHandler.enter() 异常: ", e);
            throw e;
        }

        log.info("流程发起成功: instanceId={}", instance.getId());
        return instance;
    }

    /**
     * 审批任务
     */
    @Transactional
    public void completeTask(Long taskId, WfApprovalDTO approvalDTO, String operatorId, String operatorName) {
        log.info("审批任务: taskId={}, action={}, operator={}", taskId, approvalDTO.getAction(), operatorName);

        // 1. 获取任务
        WfTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        if (!"PENDING".equals(task.getStatus())) {
            throw new RuntimeException("任务状态不是待处理: " + task.getStatus());
        }

        // 2. 获取实例和上下文
        WfInstance instance = instanceMapper.selectById(task.getInstanceId());
        WfDefinition definition = definitionMapper.selectById(instance.getDefinitionId());
        List<WfDefinitionNode> nodes = nodeMapper.selectList(
            new LambdaQueryWrapper<WfDefinitionNode>()
                .eq(WfDefinitionNode::getDefinitionId, instance.getDefinitionId()));
        List<WfDefinitionLine> lines = lineMapper.selectList(
            new LambdaQueryWrapper<WfDefinitionLine>()
                .eq(WfDefinitionLine::getDefinitionId, instance.getDefinitionId()));
        Map<String, Object> variables = getInstanceVariables(instance.getId());

        WfEngineContext context = createContext(definition, nodes, lines, instance, variables);
        context.setOperatorId(operatorId);
        context.setOperatorName(operatorName);

        // 3. 根据动作处理
        String action = approvalDTO.getAction();
        switch (action) {
            case "APPROVE" -> handleApprove(task, approvalDTO, context, operatorId, operatorName);
            case "REJECT" -> handleReject(task, approvalDTO, context, operatorId, operatorName);
            case "TRANSFER" -> handleTransfer(task, approvalDTO, context, operatorId, operatorName);
            case "DELEGATE" -> handleDelegate(task, approvalDTO, context, operatorId, operatorName);
            case "CLAIM" -> handleClaim(task, context, operatorId, operatorName);
            default -> throw new RuntimeException("不支持的操作: " + action);
        }

        log.info("任务审批完成: taskId={}, action={}", taskId, action);
    }

    private void handleApprove(WfTask task, WfApprovalDTO approvalDTO, WfEngineContext context,
                                String operatorId, String operatorName) {
        // 更新任务状态
        task.setStatus("APPROVED");
        task.setCompleteTime(LocalDateTime.now());
        task.setOpinion(approvalDTO.getComment());
        task.setTaskAction("APPROVE");
        taskMapper.updateById(task);

        // 获取令牌和节点 - 根据task的nodeId查询对应的令牌
        WfToken token = getTokenByInstanceAndNodeId(task.getInstanceId(), task.getNodeId());
        WfDefinitionNode node = context.getNodeById(task.getNodeId());

        // 如果token为null（并行分支场景下子令牌不在数据库中），直接使用task的nodeId创建令牌对象
        if (token == null) {
            log.info("子令牌不在数据库中，使用task节点创建令牌对象进行流转");
            token = new WfToken(context.getInstance(), task.getNodeId(), node.getNodeCode());
        } else {
            // 在并行分支场景下，即使获取到了令牌，其currentNodeId可能不是task对应的节点
            // 需要将token的currentNodeId更新为task对应的节点，以确保后续流转使用正确的节点
            token.setCurrentNodeId(task.getNodeId());
            token.setCurrentNodeCode(node.getNodeCode());
        }

        // 记录会签
        if ("COUNTER_SIGN".equals(task.getTaskCategory())) {
            WfTaskCounterSign counterSign = new WfTaskCounterSign();
            counterSign.setTaskId(task.getId());
            counterSign.setUserId(operatorId);
            counterSign.setApproved(1);
            counterSign.setComment(approvalDTO.getComment());
            counterSign.setSignTime(LocalDateTime.now());
            counterSignMapper.insert(counterSign);

            task.setCounterSignApproved(task.getCounterSignApproved() != null ? task.getCounterSignApproved() + 1 : 1);
            taskMapper.updateById(task);

            // 检查会签是否完成，未完成则不流转
            WfRouteStrategy routeStrategy = getRouteStrategy(node.getApprovalType());
            boolean canProceed = routeStrategy.checkAndProceed(context, task);
            log.info("会签检查: taskId={}, approvedCount={}, requiredCount={}, canProceed={}",
                    task.getId(), task.getCounterSignApproved(), task.getCounterSignCount(), canProceed);
            if (!canProceed) {
                // 会签未完成，等待其他审批人
                saveHistory(context.getInstance(), node, operatorId, operatorName, "APPROVE", approvalDTO.getComment(), task.getId());
                return;
            }
        }

        // 更新令牌状态为ACTIVE，准备流转 - 根据nodeId更新正确的令牌
        var tokenEntity = tokenMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.bank.itarch.model.entity.WfToken>()
                .eq(com.bank.itarch.model.entity.WfToken::getInstanceId, task.getInstanceId())
                .eq(com.bank.itarch.model.entity.WfToken::getCurrentNodeId, task.getNodeId()));
        if (tokenEntity != null) {
            tokenEntity.setTokenStatus("ACTIVE");
            tokenMapper.updateById(tokenEntity);
        }

        // 记录历史
        saveHistory(context.getInstance(), node, operatorId, operatorName, "APPROVE", approvalDTO.getComment(), task.getId());

        // 继续流转
        continueFlow(context, token, node);

        // 流转完成后，检查是否需要同步数据到软件表
        // 只有 OSS_IMPL_APPLY 类型且流程实例状态为 COMPLETED 时才同步
        if ("OSS_IMPL_APPLY".equals(context.getInstance().getBusinessType())
            && "COMPLETED".equals(context.getInstance().getStatus())) {
            try {
                String businessKey = context.getInstance().getBusinessKey();
                if (businessKey != null && !businessKey.isEmpty()) {
                    // 根据UUID获取申请信息，然后调用同步方法
                    var applyDTO = ossImplApplyService.getById(businessKey);
                    if (applyDTO != null && applyDTO.getImplApplyNo() != null) {
                        log.info("流程完成，同步引入申请数据到软件表: implApplyNo={}", applyDTO.getImplApplyNo());
                        ossImplApplyService.syncToSoftware(applyDTO.getImplApplyNo());
                    }
                }
            } catch (Exception e) {
                log.error("同步引入申请数据失败", e);
                // 不影响流程完成，只记录错误
            }
        }
    }

    private void handleReject(WfTask task, WfApprovalDTO approvalDTO, WfEngineContext context,
                               String operatorId, String operatorName) {
        // 更新任务状态
        task.setStatus("REJECTED");
        task.setCompleteTime(LocalDateTime.now());
        task.setOpinion(approvalDTO.getComment());
        task.setTaskAction("REJECT");
        taskMapper.updateById(task);

        // 记录会签拒绝
        if ("COUNTER_SIGN".equals(task.getTaskCategory())) {
            WfTaskCounterSign counterSign = new WfTaskCounterSign();
            counterSign.setTaskId(task.getId());
            counterSign.setUserId(operatorId);
            counterSign.setApproved(0);
            counterSign.setComment(approvalDTO.getComment());
            counterSign.setSignTime(LocalDateTime.now());
            counterSignMapper.insert(counterSign);
        }

        WfDefinitionNode node = context.getNodeById(task.getNodeId());
        saveHistory(context.getInstance(), node, operatorId, operatorName, "REJECT", approvalDTO.getComment(), task.getId());

        // 驳回到指定节点或流程发起人
        WfDefinitionNode targetNode = null;
        if (approvalDTO.getTargetNodeId() != null) {
            targetNode = context.getNodeById(approvalDTO.getTargetNodeId());
        } else if (approvalDTO.getTargetNodeCode() != null) {
            targetNode = context.getNodeByCode(approvalDTO.getTargetNodeCode());
        }
        if (targetNode != null) {
            // 如果目标是START节点，不自动流转，让发起人修改后重新提交
            if ("START".equals(targetNode.getNodeCategory())) {
                handleRejectToStart(context, task, targetNode);
            } else {
                // 驳回到指定节点（非START）
                WfToken token = getTokenByInstance(task.getInstanceId());
                token.setCurrentNodeId(targetNode.getId());
                token.setCurrentNodeCode(targetNode.getNodeCode());
                token.addHistory(targetNode.getId());

                // 更新令牌状态为ACTIVE
                var tokenEntity = tokenMapper.selectOne(
                    new LambdaQueryWrapper<com.bank.itarch.model.entity.WfToken>()
                        .eq(com.bank.itarch.model.entity.WfToken::getInstanceId, task.getInstanceId())
                        .in(com.bank.itarch.model.entity.WfToken::getTokenStatus, "ACTIVE", "WAITING"));
                if (tokenEntity != null) {
                    tokenEntity.setTokenStatus(WfToken.STATUS_ACTIVE);
                    tokenMapper.updateById(tokenEntity);
                }

                // 根据节点类型调用正确的处理器
                WfNodeHandler targetHandler = context.getNodeHandlers().get(targetNode.getNodeCategory());
                if (targetHandler != null) {
                    targetHandler.enter(context, token);
                    context.setInstance(token.getInstance());
                } else {
                    log.error("未找到目标节点处理器: category={}", targetNode.getNodeCategory());
                }

                // 更新实例状态为 RUNNING（驳回后流程继续运行）
                context.getInstance().setStatus("RUNNING");
                context.getInstance().setCurrentNodeId(targetNode.getId());
                context.getInstance().setCurrentNodeName(targetNode.getNodeName());
                context.getInstanceMapper().updateById(context.getInstance());

                // 创建 ACTIVE 历史记录，标记目标节点有待办任务等待处理
                createActiveHistoryForNewTask(context);
            }
        } else {
            // 没有指定目标节点，默认驳回到发起人
            WfDefinitionNode startNode = context.getStartNode();
            if (startNode == null) {
                throw new RuntimeException("流程定义中没有开始节点");
            }
            handleRejectToStart(context, task, startNode);
        }
    }

    /**
     * 驳回到START节点（发起人），不自动流转
     */
    private void handleRejectToStart(WfEngineContext context, WfTask task, WfDefinitionNode startNode) {
        WfInstance instance = context.getInstance();

        // 更新令牌状态为ACTIVE，流转到开始节点
        var tokenEntity = tokenMapper.selectOne(
            new LambdaQueryWrapper<com.bank.itarch.model.entity.WfToken>()
                .eq(com.bank.itarch.model.entity.WfToken::getInstanceId, task.getInstanceId())
                .in(com.bank.itarch.model.entity.WfToken::getTokenStatus, "ACTIVE", "WAITING"));
        if (tokenEntity != null) {
            tokenEntity.setTokenStatus(WfToken.STATUS_ACTIVE);
            tokenEntity.setCurrentNodeId(startNode.getId());
            tokenMapper.updateById(tokenEntity);
        }

        // 更新实例状态为 RUNNING，当前节点设为开始节点
        instance.setStatus("RUNNING");
        instance.setCurrentNodeId(startNode.getId());
        instance.setCurrentNodeName(startNode.getNodeName());
        context.getInstanceMapper().updateById(instance);

        // 直接创建待办任务给发起人，不自动流转
        // 发起人需要修改表单后主动提交
        WfTask newTask = new WfTask();
        newTask.setInstanceId(instance.getId());
        newTask.setNodeId(startNode.getId());
        newTask.setTaskCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        newTask.setTaskName(startNode.getNodeName());
        newTask.setTaskCategory("NORMAL");
        newTask.setAssigneeId(instance.getApplicantId());
        newTask.setAssigneeName(instance.getApplicantName());
        newTask.setStatus("PENDING");
        newTask.setCreateTime(LocalDateTime.now());
        taskMapper.insert(newTask);

        log.info("驳回到发起人，创建待办任务: taskId={}, assigneeId={}", newTask.getId(), instance.getApplicantId());

        // 注意：不创建 ACTIVE 历史记录，START 的 pending 任务会在审批路径中单独显示
    }

    private void handleTransfer(WfTask task, WfApprovalDTO approvalDTO, WfEngineContext context,
                                 String operatorId, String operatorName) {
        // 转办：将任务转给其他人
        task.setStatus("TRANSFERRED");
        task.setCompleteTime(LocalDateTime.now());
        task.setOpinion("转办至: " + approvalDTO.getAssigneeName());
        task.setTaskAction("TRANSFER");
        taskMapper.updateById(task);

        // 创建新的转办任务
        WfTask newTask = new WfTask();
        newTask.setInstanceId(task.getInstanceId());
        newTask.setNodeId(task.getNodeId());
        newTask.setTaskCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        newTask.setTaskName(task.getTaskName());
        newTask.setTaskCategory("NORMAL");
        newTask.setAssigneeId(Long.parseLong(approvalDTO.getAssigneeId()));
        newTask.setAssigneeName(approvalDTO.getAssigneeName());
        newTask.setStatus("PENDING");
        newTask.setCreateTime(LocalDateTime.now());
        newTask.setDelegateFrom(Long.parseLong(operatorId));
        taskMapper.insert(newTask);

        WfDefinitionNode node = context.getNodeById(task.getNodeId());
        saveHistory(context.getInstance(), node, operatorId, operatorName, "TRANSFER",
                "转办至: " + approvalDTO.getAssigneeName(), task.getId());
    }

    private void handleDelegate(WfTask task, WfApprovalDTO approvalDTO, WfEngineContext context,
                                  String operatorId, String operatorName) {
        // 加签：知会其他人，不影响原流程
        WfTask delegateTask = new WfTask();
        delegateTask.setInstanceId(task.getInstanceId());
        delegateTask.setNodeId(task.getNodeId());
        delegateTask.setTaskCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        delegateTask.setTaskName(task.getTaskName() + "[知会]");
        delegateTask.setTaskCategory("SUB_TASK");
        delegateTask.setParentTaskId(task.getId());
        delegateTask.setAssigneeId(Long.parseLong(approvalDTO.getDelegateUserId()));
        delegateTask.setAssigneeName(approvalDTO.getDelegateUserName());
        delegateTask.setStatus("PENDING");
        delegateTask.setCreateTime(LocalDateTime.now());
        taskMapper.insert(delegateTask);

        WfDefinitionNode node = context.getNodeById(task.getNodeId());
        saveHistory(context.getInstance(), node, operatorId, operatorName, "DELEGATE",
                "加签知会: " + approvalDTO.getDelegateUserName(), task.getId());
    }

    private void handleClaim(WfTask task, WfEngineContext context, String operatorId, String operatorName) {
        // 签收任务（抢任务）
        task.setAssigneeId(Long.parseLong(operatorId));
        task.setAssigneeName(operatorName);
        task.setClaimTime(LocalDateTime.now());
        taskMapper.updateById(task);

        WfDefinitionNode node = context.getNodeById(task.getNodeId());
        saveHistory(context.getInstance(), node, operatorId, operatorName, "CLAIM", "签收任务", task.getId());
    }

    private void continueFlow(WfEngineContext context, WfToken token, WfDefinitionNode node) {
        // 根据节点类型继续流转
        String nodeCategory = node.getNodeCategory();
        WfNodeHandler handler = getNodeHandler(nodeCategory);

        if (handler != null) {
            handler.complete(context, token);
        }
    }

    private WfNodeHandler getNodeHandler(String nodeCategory) {
        if (nodeCategory == null) {
            return approvalNodeHandler;
        }
        return switch (nodeCategory) {
            case "START" -> startNodeHandler;
            case "END" -> endNodeHandler;
            case "APPROVAL" -> approvalNodeHandler;
            case "CONDITION" -> conditionNodeHandler;
            case "PARALLEL_BRANCH" -> parallelBranchHandler;
            case "PARALLEL_JOIN" -> parallelJoinHandler;
            default -> approvalNodeHandler;
        };
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

    private WfEngineContext createContext(WfDefinition definition, List<WfDefinitionNode> nodes,
                                          List<WfDefinitionLine> lines, WfInstance instance,
                                          Map<String, Object> variables) {
        WfEngineContext context = new WfEngineContext();
        context.setDefinition(definition);
        context.setNodes(nodes);
        context.setLines(lines);
        context.setInstance(instance);
        context.setVariables(variables != null ? variables : new HashMap<>());

        // 注入节点处理器
        Map<String, WfNodeHandler> handlers = new HashMap<>();
        handlers.put("START", startNodeHandler);
        handlers.put("END", endNodeHandler);
        handlers.put("APPROVAL", approvalNodeHandler);
        handlers.put("CONDITION", conditionNodeHandler);
        handlers.put("PARALLEL_BRANCH", parallelBranchHandler);
        handlers.put("PARALLEL_JOIN", parallelJoinHandler);
        context.setNodeHandlers(handlers);

        // 注入Mapper
        context.setDefinitionMapper(definitionMapper);
        context.setNodeMapper(nodeMapper);
        context.setLineMapper(lineMapper);
        context.setInstanceMapper(instanceMapper);
        context.setVariableMapper(variableMapper);
        context.setTaskMapper(taskMapper);
        context.setCandidateMapper(candidateMapper);
        context.setHistoryMapper(historyMapper);
        context.setTokenMapper(tokenMapper);
        context.setParallelJoinMapper(parallelJoinMapper);
        context.setCounterSignMapper(counterSignMapper);
        context.setTimeoutRuleMapper(timeoutRuleMapper);

        return context;
    }

    private WfToken getTokenByInstance(Long instanceId) {
        com.bank.itarch.model.entity.WfToken tokenEntity = tokenMapper.selectOne(
            new LambdaQueryWrapper<com.bank.itarch.model.entity.WfToken>()
                .eq(com.bank.itarch.model.entity.WfToken::getInstanceId, instanceId)
                .in(com.bank.itarch.model.entity.WfToken::getTokenStatus, "ACTIVE", "WAITING"));
        if (tokenEntity == null) {
            return null;
        }
        // Fetch node code from definition node
        WfDefinitionNode node = nodeMapper.selectById(tokenEntity.getCurrentNodeId());
        String nodeCode = node != null ? node.getNodeCode() : null;
        WfInstance instance = instanceMapper.selectById(instanceId);
        return new WfToken(instance, tokenEntity.getCurrentNodeId(), nodeCode);
    }

    private WfToken getTokenByInstanceAndNodeId(Long instanceId, Long nodeId) {
        com.bank.itarch.model.entity.WfToken tokenEntity = tokenMapper.selectOne(
            new LambdaQueryWrapper<com.bank.itarch.model.entity.WfToken>()
                .eq(com.bank.itarch.model.entity.WfToken::getInstanceId, instanceId)
                .eq(com.bank.itarch.model.entity.WfToken::getCurrentNodeId, nodeId)
                .in(com.bank.itarch.model.entity.WfToken::getTokenStatus, "ACTIVE", "WAITING"));
        if (tokenEntity == null) {
            // 在并行分支场景下，子令牌可能不在数据库中，此时不应该回退到获取主令牌
            // 否则会导致审批处理器使用主令牌流转，错误地重新进入下一节点
            log.warn("未找到对应节点的令牌: instanceId={}, nodeId={}", instanceId, nodeId);
            return null;
        }
        WfDefinitionNode node = nodeMapper.selectById(tokenEntity.getCurrentNodeId());
        String nodeCode = node != null ? node.getNodeCode() : null;
        WfInstance instance = instanceMapper.selectById(instanceId);
        return new WfToken(instance, tokenEntity.getCurrentNodeId(), nodeCode);
    }

    private Map<String, Object> getInstanceVariables(Long instanceId) {
        List<WfInstanceVariable> variables = variableMapper.selectList(
            new LambdaQueryWrapper<WfInstanceVariable>()
                .eq(WfInstanceVariable::getInstanceId, instanceId));

        Map<String, Object> result = new HashMap<>();
        for (WfInstanceVariable v : variables) {
            result.put(v.getVariableKey(), v.getVariableValue());
        }
        return result;
    }

    private void saveHistory(WfInstance instance, WfDefinitionNode node, String operatorId,
                              String operatorName, String action, String opinion, Long taskId) {
        WfHistory history = new WfHistory();
        history.setInstanceId(instance.getId());
        history.setNodeId(node != null ? node.getId() : null);
        history.setNodeName(node != null ? node.getNodeName() : null);
        if (operatorId != null && !operatorId.trim().isEmpty()) {
            history.setOperatorId(Long.parseLong(operatorId));
        }
        history.setOperatorName(operatorName);
        history.setAction(action);
        history.setOpinion(opinion);
        history.setTaskId(taskId);
        history.setCreateTime(LocalDateTime.now());
        historyMapper.insert(history);
    }

    /**
     * 驳回后，为新创建的待办任务节点创建ACTIVE历史记录
     * 用于在审批路径中展示节点处于"待处理"状态
     */
    private void createActiveHistoryForNewTask(WfEngineContext context) {
        // 查询最近创建的待办任务（状态为PENDING）
        WfTask newTask = taskMapper.selectOne(
            new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getInstanceId, context.getInstance().getId())
                .eq(WfTask::getStatus, "PENDING")
                .orderByDesc(WfTask::getCreateTime)
                .last("LIMIT 1"));

        if (newTask != null) {
            WfDefinitionNode currentNode = context.getNodeById(newTask.getNodeId());
            if (currentNode != null) {
                saveHistory(context.getInstance(), currentNode,
                    String.valueOf(newTask.getAssigneeId()),
                    newTask.getAssigneeName(),
                    "ACTIVE", "等待处理", newTask.getId());
            }
        }
    }
}
