package com.bank.itarch.controller.wf;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.engine.WfProcessEngine;
import com.bank.itarch.mapper.*;
import com.bank.itarch.model.dto.*;
import com.bank.itarch.model.entity.*;
import com.bank.itarch.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 流程实例控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/wf/instances")
@RequiredArgsConstructor
@Tag(name = "流程实例")
public class WfInstanceController {

    private final WfProcessEngine processEngine;
    private final WfInstanceMapper instanceMapper;
    private final WfInstanceVariableMapper variableMapper;
    private final WfHistoryMapper historyMapper;
    private final WfTaskMapper taskMapper;
    private final WfDefinitionNodeMapper nodeMapper;
    private final WfDefinitionLineMapper lineMapper;

    @PostMapping
    @Operation(summary = "发起流程")
    public Result<WfInstance> startProcess(@RequestBody WfStartProcessDTO dto) {
        UserContext.UserInfo currentUser = UserContext.getUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        WfInstance instance = processEngine.startProcess(dto, String.valueOf(currentUser.getUserId()), currentUser.getUsername());
        return Result.success("发起成功", instance);
    }

    @GetMapping
    @Operation(summary = "流程实例列表")
    public Result<PageResult<WfInstance>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String status) {
        Page<WfInstance> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<WfInstance> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(WfInstance::getStatus, status);
        }
        wrapper.orderByDesc(WfInstance::getStartTime);
        IPage<WfInstance> result = instanceMapper.selectPage(p, wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @GetMapping("/my-applications")
    @Operation(summary = "我的申请列表（我发起的流程）")
    public Result<PageResult<WfInstance>> myApplications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String businessType) {
        UserContext.UserInfo currentUser = UserContext.getUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        Page<WfInstance> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<WfInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfInstance::getApplicantId, currentUser.getUserId());
        if (businessType != null && !businessType.isEmpty()) {
            wrapper.eq(WfInstance::getBusinessType, businessType);
        }
        wrapper.orderByDesc(WfInstance::getStartTime);
        IPage<WfInstance> result = instanceMapper.selectPage(p, wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @GetMapping("/my-done")
    @Operation(summary = "我的已办列表（我提交的和我审批过的）")
    public Result<PageResult<WfMyDoneDTO>> myDoneList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String applicantName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDateFrom,
            @RequestParam(required = false) String startDateTo) {
        UserContext.UserInfo currentUser = UserContext.getUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }

        // 查找当前用户参与过的实例ID列表
        // 1. 用户作为申请人提交的实例
        LambdaQueryWrapper<WfInstance> applicantWrapper = new LambdaQueryWrapper<>();
        applicantWrapper.eq(WfInstance::getApplicantId, currentUser.getUserId());
        List<WfInstance> applicantInstances = instanceMapper.selectList(applicantWrapper);

        // 2. 用户作为审批人处理过的实例（通过任务表关联）
        LambdaQueryWrapper<WfTask> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(WfTask::getAssigneeId, currentUser.getUserId());
        taskWrapper.in(WfTask::getStatus, "APPROVED", "REJECTED", "TRANSFERRED");
        List<WfTask> processedTasks = taskMapper.selectList(taskWrapper);
        List<Long> processedInstanceIds = processedTasks.stream()
            .map(WfTask::getInstanceId)
            .distinct()
            .toList();

        // 合并实例ID（去重）
        Set<Long> instanceIds = new HashSet<>();
        for (WfInstance inst : applicantInstances) {
            instanceIds.add(inst.getId());
        }
        for (Long instId : processedInstanceIds) {
            instanceIds.add(instId);
        }

        if (instanceIds.isEmpty()) {
            return Result.success(PageResult.of(Collections.emptyList(), 0L, page, pageSize));
        }

        // 分页查询实例
        Page<WfInstance> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<WfInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WfInstance::getId, instanceIds);

        // 应用过滤条件
        if (businessType != null && !businessType.isEmpty()) {
            wrapper.eq(WfInstance::getBusinessType, businessType);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(WfInstance::getTitle, keyword);
        }
        if (applicantName != null && !applicantName.isEmpty()) {
            wrapper.like(WfInstance::getApplicantName, applicantName);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(WfInstance::getStatus, status);
        }
        if (startDateFrom != null && !startDateFrom.isEmpty()) {
            wrapper.ge(WfInstance::getStartTime, startDateFrom);
        }
        if (startDateTo != null && !startDateTo.isEmpty()) {
            wrapper.le(WfInstance::getStartTime, startDateTo);
        }

        wrapper.orderByDesc(WfInstance::getStartTime);
        IPage<WfInstance> result = instanceMapper.selectPage(p, wrapper);

        // 转换为DTO并添加当前审批人信息
        List<WfMyDoneDTO> dtoList = result.getRecords().stream().map(inst -> {
            WfMyDoneDTO dto = new WfMyDoneDTO();
            dto.setId(inst.getId());
            dto.setInstanceCode(inst.getInstanceCode());
            dto.setDefinitionId(inst.getDefinitionId());
            dto.setDefinitionName(inst.getDefinitionName());
            dto.setBusinessType(inst.getBusinessType());
            dto.setBusinessKey(inst.getBusinessKey());
            dto.setTitle(inst.getTitle());
            dto.setApplicantId(inst.getApplicantId());
            dto.setApplicantName(inst.getApplicantName());
            dto.setApplicantDeptName(inst.getApplicantDeptName());
            dto.setCurrentNodeId(inst.getCurrentNodeId());
            dto.setCurrentNodeName(inst.getCurrentNodeName());
            dto.setStatus(inst.getStatus());
            dto.setResult(inst.getResult());
            dto.setStartTime(inst.getStartTime());
            dto.setEndTime(inst.getEndTime());

            // 判断当前用户在此实例中的角色
            if (currentUser.getUserId().equals(inst.getApplicantId())) {
                dto.setMyRole("APPLICANT");
            } else {
                dto.setMyRole("APPROVER");
            }

            // 获取当前待审批任务（如果有）
            List<WfTask> pending = taskMapper.selectList(
                new LambdaQueryWrapper<WfTask>()
                    .eq(WfTask::getInstanceId, inst.getId())
                    .eq(WfTask::getStatus, "PENDING")
                    .last("LIMIT 1"));
            if (!pending.isEmpty()) {
                dto.setCurrentApproverId(pending.get(0).getAssigneeId());
                dto.setCurrentApproverName(pending.get(0).getAssigneeName());
            }

            return dto;
        }).toList();

        return Result.success(PageResult.of(dtoList, result.getTotal(), page, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "流程实例详情")
    public Result<WfInstanceDTO> getInstance(@PathVariable Long id) {
        WfInstance instance = instanceMapper.selectById(id);
        if (instance == null) {
            return Result.error("流程实例不存在");
        }

        WfInstanceDTO dto = new WfInstanceDTO();
        dto.setId(instance.getId());
        dto.setInstanceCode(instance.getInstanceCode());
        dto.setDefinitionId(instance.getDefinitionId());
        dto.setDefinitionName(instance.getDefinitionName());
        dto.setBusinessType(instance.getBusinessType());
        dto.setBusinessId(instance.getBusinessId());
        dto.setTitle(instance.getTitle());
        dto.setApplicantId(instance.getApplicantId());
        dto.setApplicantName(instance.getApplicantName());
        dto.setCurrentNodeId(instance.getCurrentNodeId());
        dto.setCurrentNodeName(instance.getCurrentNodeName());
        dto.setStatus(instance.getStatus());
        dto.setStartTime(instance.getStartTime());
        dto.setEndTime(instance.getEndTime());
        dto.setResult(instance.getResult());

        // 获取变量
        List<WfInstanceVariable> variables = variableMapper.selectList(
            new LambdaQueryWrapper<WfInstanceVariable>()
                .eq(WfInstanceVariable::getInstanceId, id));
        Map<String, Object> varMap = new HashMap<>();
        for (WfInstanceVariable v : variables) {
            varMap.put(v.getVariableKey(), v.getVariableValue());
        }
        dto.setVariables(varMap);

        return Result.success(dto);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "流程历史")
    public Result<List<WfHistory>> history(@PathVariable Long id) {
        List<WfHistory> histories = historyMapper.selectList(
            new LambdaQueryWrapper<WfHistory>()
                .eq(WfHistory::getInstanceId, id)
                .orderByAsc(WfHistory::getCreateTime));
        return Result.success(histories);
    }

    @GetMapping("/{id}/trace")
    @Operation(summary = "流程轨迹图")
    public Result<WfTraceDTO> getInstanceTrace(@PathVariable Long id) {
        WfInstance instance = instanceMapper.selectById(id);
        if (instance == null) {
            return Result.error("流程实例不存在");
        }

        WfTraceDTO trace = new WfTraceDTO();
        trace.setDefinitionId(instance.getDefinitionId());
        trace.setDefinitionName(instance.getDefinitionName());
        trace.setInstanceId(instance.getId());
        trace.setInstanceCode(instance.getInstanceCode());
        trace.setTitle(instance.getTitle());
        trace.setStatus(instance.getStatus());
        trace.setCurrentNodeId(instance.getCurrentNodeId());

        // 获取流程定义节点
        List<WfDefinitionNode> allNodes = nodeMapper.selectList(
            new LambdaQueryWrapper<WfDefinitionNode>()
                .eq(WfDefinitionNode::getDefinitionId, instance.getDefinitionId())
                .orderByAsc(WfDefinitionNode::getNodeOrder));

        // 获取流程定义连线
        List<WfDefinitionLine> allLines = lineMapper.selectList(
            new LambdaQueryWrapper<WfDefinitionLine>()
                .eq(WfDefinitionLine::getDefinitionId, instance.getDefinitionId()));

        // 获取历史记录（按时间正序）
        List<WfHistory> histories = historyMapper.selectList(
            new LambdaQueryWrapper<WfHistory>()
                .eq(WfHistory::getInstanceId, id)
                .orderByAsc(WfHistory::getCreateTime));

        // 获取当前待处理任务
        List<WfTask> pendingTasks = taskMapper.selectList(
            new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getInstanceId, id)
                .eq(WfTask::getStatus, "PENDING"));

        // 建立nodeId -> node 的映射
        Map<Long, WfDefinitionNode> nodeMap = new HashMap<>();
        for (WfDefinitionNode node : allNodes) {
            nodeMap.put(node.getId(), node);
        }

        // 建立 nodeId -> pending task 的映射（用于匹配特定visit的pending状态）
        // key: nodeId, value: visitIdx of the pending task
        Map<Long, Integer> pendingTaskVisitIdx = new HashMap<>();
        Map<Long, WfTask> pendingTaskByNode = new HashMap<>();
        for (WfTask task : pendingTasks) {
            // 统计该节点已有多少条历史记录（visit次数）
            int existingVisitCount = (int) histories.stream()
                .filter(h -> h.getNodeId() != null && h.getNodeId().equals(task.getNodeId()))
                .count();
            // pending任务的visitIdx = 历史记录数 + 1（下一个要发生的visit）
            int visitIdx = existingVisitCount + 1;
            pendingTaskVisitIdx.put(task.getNodeId(), visitIdx);
            pendingTaskByNode.put(task.getNodeId(), task);
        }

        // 构建节点轨迹：按历史顺序，同一节点可多次出现
        List<WfTraceDTO.NodeTrace> nodeTraces = new ArrayList<>();
        Map<Long, Integer> nodeCurrentVisit = new HashMap<>();

        // 5.1 按历史顺序添加所有有记录的节点
        for (WfHistory h : histories) {
            if (h.getNodeId() == null) continue;
            WfDefinitionNode node = nodeMap.get(h.getNodeId());
            if (node == null) continue;

            int visitIdx = nodeCurrentVisit.getOrDefault(h.getNodeId(), 0) + 1;
            nodeCurrentVisit.put(h.getNodeId(), visitIdx);

            WfTraceDTO.NodeTrace nt = new WfTraceDTO.NodeTrace();
            nt.setNodeId(node.getId());
            nt.setNodeCode(node.getNodeCode());
            nt.setNodeName(node.getNodeName());
            nt.setNodeCategory(node.getNodeCategory());
            nt.setApprovalType(node.getApprovalType());
            nt.setNodeOrder(node.getNodeOrder());
            nt.setPositionX(node.getPositionX());
            nt.setPositionY(node.getPositionY());
            nt.setVisitIndex(visitIdx);
            nt.setExecuted(true);
            nt.setAction(h.getAction());
            nt.setOpinion(h.getOpinion());
            nt.setActionTime(h.getCreateTime());
            nt.setOperatorName(h.getOperatorName());
            nt.setTaskId(h.getTaskId());

            // 判断当前状态：是否有pending任务关联到这个visit
            Integer pendingVisitIdx = pendingTaskVisitIdx.get(node.getId());
            if (pendingVisitIdx != null && pendingVisitIdx == visitIdx) {
                // 这个visit有pending任务，设置为CURRENT
                nt.setCurrent(true);
                WfTask task = pendingTaskByNode.get(node.getId());
                if (task != null) {
                    nt.setAssigneeId(task.getAssigneeId());
                    nt.setAssigneeName(task.getAssigneeName());
                    nt.setTaskId(task.getId());
                }
            } else {
                nt.setCurrent(false);
            }

            nodeTraces.add(nt);
        }

        // 5.2 如果有pending任务但该visit不在历史中（驳回后新建任务），手动追加
        for (Map.Entry<Long, Integer> entry : pendingTaskVisitIdx.entrySet()) {
            Long nodeId = entry.getKey();
            Integer visitIdx = entry.getValue();
            Integer existingVisitCount = nodeCurrentVisit.get(nodeId);
            if (existingVisitCount == null || existingVisitCount < visitIdx) {
                // 这个visit在历史中没有记录，需要手动追加
                WfDefinitionNode node = nodeMap.get(nodeId);
                if (node != null) {
                    WfTask task = pendingTaskByNode.get(nodeId);
                    WfTraceDTO.NodeTrace nt = new WfTraceDTO.NodeTrace();
                    nt.setNodeId(node.getId());
                    nt.setNodeCode(node.getNodeCode());
                    nt.setNodeName(node.getNodeName());
                    nt.setNodeCategory(node.getNodeCategory());
                    nt.setApprovalType(node.getApprovalType());
                    nt.setNodeOrder(node.getNodeOrder());
                    nt.setPositionX(node.getPositionX());
                    nt.setPositionY(node.getPositionY());
                    nt.setVisitIndex(visitIdx);
                    nt.setExecuted(false);
                    nt.setCurrent(true);
                    nt.setAssigneeId(task.getAssigneeId());
                    nt.setAssigneeName(task.getAssigneeName());
                    nt.setTaskId(task.getId());
                    nodeTraces.add(nt);
                    nodeCurrentVisit.put(nodeId, visitIdx);
                }
            }
        }

        // 5.3 添加未到达的节点（每个节点最多出现一次在未访问列表中）
        // 计算每个节点在历史记录中的最大visitIndex
        Map<Long, Integer> maxVisitInHistory = new HashMap<>();
        for (WfTraceDTO.NodeTrace nt : nodeTraces) {
            maxVisitInHistory.merge(nt.getNodeId(), nt.getVisitIndex(), Integer::max);
        }

        List<WfTraceDTO.NodeTrace> unvisitedNodes = new ArrayList<>();
        for (WfDefinitionNode node : allNodes) {
            if ("START".equals(node.getNodeCategory())) continue; // 跳过START节点

            // 检查该节点是否已经有任何 visit 记录
            boolean hasAnyVisit = nodeTraces.stream()
                .anyMatch(nt -> nt.getNodeId().equals(node.getId()));
            if (hasAnyVisit) continue; // 已有访问记录，跳过

            WfTraceDTO.NodeTrace nt = new WfTraceDTO.NodeTrace();
            nt.setNodeId(node.getId());
            nt.setNodeCode(node.getNodeCode());
            nt.setNodeName(node.getNodeName());
            nt.setNodeCategory(node.getNodeCategory());
            nt.setApprovalType(node.getApprovalType());
            nt.setNodeOrder(node.getNodeOrder());
            nt.setPositionX(node.getPositionX());
            nt.setPositionY(node.getPositionY());
            nt.setVisitIndex(1);
            nt.setExecuted(false);
            nt.setCurrent(false);
            unvisitedNodes.add(nt);
        }

        // 未访问节点按 nodeOrder 排序
        unvisitedNodes.sort(Comparator.comparing(
            WfTraceDTO.NodeTrace::getNodeOrder,
            Comparator.nullsLast(Comparator.naturalOrder())
        ));

        // 最终排序：
        // 1. 历史节点按时间顺序（已在nodeTraces中）
        // 2. 当前节点（pending）插入到对应的历史位置之后
        // 3. 未访问节点按nodeOrder排在最后

        List<WfTraceDTO.NodeTrace> finalTraces = new ArrayList<>();

        // 先加入所有非current的历史节点
        for (WfTraceDTO.NodeTrace nt : nodeTraces) {
            if (!nt.isCurrent()) {
                finalTraces.add(nt);
            }
        }

        // 再加入current节点（pending），插入到对应的历史位置之后
        // 找到pending节点应该插入的位置：在与其关联的历史记录之后
        int insertIndex = finalTraces.size();
        for (int i = 0; i < nodeTraces.size(); i++) {
            WfTraceDTO.NodeTrace nt = nodeTraces.get(i);
            if (nt.isCurrent()) {
                // 找到这个current节点在finalTraces中的位置
                // 关联的历史节点应该在finalTraces中的最后
                // 所以插入到finalTraces末尾
                break;
            }
        }
        for (WfTraceDTO.NodeTrace nt : nodeTraces) {
            if (nt.isCurrent()) {
                finalTraces.add(nt);
            }
        }

        // 最后加入未访问节点
        finalTraces.addAll(unvisitedNodes);

        // 6. 处理结束节点状态：如果实例已完成，则结束节点也标记为已完成
        if ("COMPLETED".equals(instance.getStatus()) || "APPROVED".equals(instance.getStatus())) {
            for (WfTraceDTO.NodeTrace nt : finalTraces) {
                WfDefinitionNode node = nodeMap.get(nt.getNodeId());
                if (node != null && "END".equals(node.getNodeCategory())) {
                    nt.setExecuted(true);
                    nt.setAction("COMPLETE");
                }
            }
        }

        trace.setNodes(finalTraces);

        // 6. 构建连线轨迹
        Set<Long> executedNodeIds = histories.stream()
            .map(WfHistory::getNodeId)
            .filter(Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());

        List<WfTraceDTO.LineTrace> lineTraces = allLines.stream().map(line -> {
            WfTraceDTO.LineTrace lt = new WfTraceDTO.LineTrace();
            lt.setLineId(line.getId());
            lt.setSourceNodeId(line.getSourceNodeId());
            lt.setTargetNodeId(line.getTargetNodeId());
            lt.setLineName(line.getLineName());
            lt.setLineCode(line.getLineCode());
            lt.setConditionExpr(line.getConditionExpr());
            lt.setLineOrder(line.getLineOrder());
            lt.setTraversed(executedNodeIds.contains(line.getSourceNodeId()));
            return lt;
        }).toList();
        trace.setLines(lineTraces);

        return Result.success(trace);
    }

    @GetMapping("/{id}/tasks")
    @Operation(summary = "流程实例的任务列表")
    public Result<List<WfTask>> getTasks(@PathVariable Long id) {
        List<WfTask> tasks = taskMapper.selectList(
            new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getInstanceId, id)
                .orderByDesc(WfTask::getCreateTime));
        return Result.success(tasks);
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "撤回流程")
    public Result<Void> withdraw(@PathVariable Long id) {
        UserContext.UserInfo currentUser = UserContext.getUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        WfInstance instance = instanceMapper.selectById(id);
        if (instance == null) {
            return Result.error("流程实例不存在");
        }
        if (!"RUNNING".equals(instance.getStatus())) {
            return Result.error("只能撤回运行中的流程");
        }
        // 只能撤回自己发起的流程
        if (!currentUser.getUserId().equals(instance.getApplicantId())) {
            return Result.error("只能撤回自己发起的流程");
        }
        // 取消所有待处理任务
        List<WfTask> pendingTasks = taskMapper.selectList(
            new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getInstanceId, id)
                .eq(WfTask::getStatus, "PENDING"));
        for (WfTask task : pendingTasks) {
            task.setStatus("WITHDRAWN");
            task.setCompleteTime(LocalDateTime.now());
            task.setOpinion("撤回流程");
            taskMapper.updateById(task);
        }
        // 更新实例状态
        instance.setStatus("WITHDRAWN");
        instance.setEndTime(LocalDateTime.now());
        instance.setResult("WITHDRAWN");
        instanceMapper.updateById(instance);
        return Result.success("撤回成功", null);
    }

    @PostMapping("/{id}/terminate")
    @Operation(summary = "终止流程")
    public Result<Void> terminate(@PathVariable Long id, @RequestBody(required = false) String reason) {
        WfInstance instance = instanceMapper.selectById(id);
        if (instance == null) {
            return Result.error("流程实例不存在");
        }
        instance.setStatus("TERMINATED");
        instance.setEndTime(java.time.LocalDateTime.now());
        instance.setResult("TERMINATED");
        instanceMapper.updateById(instance);
        return Result.success("终止成功", null);
    }
}
