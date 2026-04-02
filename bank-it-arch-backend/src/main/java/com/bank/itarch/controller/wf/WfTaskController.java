package com.bank.itarch.controller.wf;

import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.engine.WfProcessEngine;
import com.bank.itarch.mapper.WfDefinitionNodeMapper;
import com.bank.itarch.mapper.WfHistoryMapper;
import com.bank.itarch.mapper.WfInstanceMapper;
import com.bank.itarch.mapper.WfTaskMapper;
import com.bank.itarch.model.entity.WfDefinitionNode;
import com.bank.itarch.model.entity.WfHistory;
import com.bank.itarch.model.dto.WfApprovalDTO;
import com.bank.itarch.model.dto.WfTaskDTO;
import com.bank.itarch.model.entity.WfInstance;
import com.bank.itarch.model.entity.WfTask;
import com.bank.itarch.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流任务控制器
 */
@RestController
@RequestMapping("/v1/wf/tasks")
@RequiredArgsConstructor
@Tag(name = "工作流任务")
public class WfTaskController {

    private final WfProcessEngine processEngine;
    private final WfTaskMapper taskMapper;
    private final WfInstanceMapper instanceMapper;
    private final WfDefinitionNodeMapper nodeMapper;
    private final WfHistoryMapper historyMapper;

    @GetMapping("/todo")
    @Operation(summary = "待办任务列表")
    public Result<PageResult<WfTaskDTO>> todoList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        UserContext.UserInfo currentUser = UserContext.getUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        Page<WfTask> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<WfTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTask::getStatus, "PENDING");
        wrapper.eq(WfTask::getAssigneeId, currentUser.getUserId());
        wrapper.orderByDesc(WfTask::getCreateTime);
        IPage<WfTask> result = taskMapper.selectPage(p, wrapper);

        // 转换为DTO并关联实例信息
        List<WfTaskDTO> dtoList = result.getRecords().stream().map(task -> {
            WfTaskDTO dto = convertToDto(task);
            // 查询关联的实例信息
            WfInstance instance = instanceMapper.selectById(task.getInstanceId());
            if (instance != null) {
                dto.setInstanceCode(instance.getInstanceCode());
                dto.setTitle(instance.getTitle());
                dto.setApplicantId(instance.getApplicantId());
                dto.setApplicantName(instance.getApplicantName());
                dto.setApplicantDeptName(instance.getApplicantDeptName());
                dto.setDefinitionId(instance.getDefinitionId());
                dto.setDefinitionName(instance.getDefinitionName());
                dto.setBusinessType(instance.getBusinessType());
                dto.setBusinessKey(instance.getBusinessKey());
                dto.setBusinessId(instance.getBusinessId());
            }
            return dto;
        }).toList();

        return Result.success(PageResult.of(dtoList, result.getTotal(), page, pageSize));
    }

    @GetMapping("/done")
    @Operation(summary = "已办任务列表")
    public Result<PageResult<WfTaskDTO>> doneList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        UserContext.UserInfo currentUser = UserContext.getUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        Page<WfTask> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<WfTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WfTask::getStatus, "APPROVED", "REJECTED", "TRANSFERRED");
        wrapper.eq(WfTask::getAssigneeId, currentUser.getUserId());
        wrapper.orderByDesc(WfTask::getCompleteTime);
        IPage<WfTask> result = taskMapper.selectPage(p, wrapper);

        // 转换为DTO并关联实例信息
        List<WfTaskDTO> dtoList = result.getRecords().stream().map(task -> {
            WfTaskDTO dto = convertToDto(task);
            WfInstance instance = instanceMapper.selectById(task.getInstanceId());
            if (instance != null) {
                dto.setInstanceCode(instance.getInstanceCode());
                dto.setTitle(instance.getTitle());
                dto.setApplicantId(instance.getApplicantId());
                dto.setApplicantName(instance.getApplicantName());
                dto.setApplicantDeptName(instance.getApplicantDeptName());
                dto.setDefinitionId(instance.getDefinitionId());
                dto.setDefinitionName(instance.getDefinitionName());
                dto.setBusinessType(instance.getBusinessType());
                dto.setBusinessKey(instance.getBusinessKey());
                dto.setBusinessId(instance.getBusinessId());
            }
            return dto;
        }).toList();

        return Result.success(PageResult.of(dtoList, result.getTotal(), page, pageSize));
    }

    private WfTaskDTO convertToDto(WfTask task) {
        WfTaskDTO dto = new WfTaskDTO();
        dto.setId(task.getId());
        dto.setInstanceId(task.getInstanceId());
        dto.setNodeId(task.getNodeId());
        dto.setTaskCode(task.getTaskCode());
        dto.setTaskName(task.getTaskName());
        dto.setTaskCategory(task.getTaskCategory());
        dto.setCounterSignType(task.getCounterSignType());
        dto.setCounterSignCount(task.getCounterSignCount());
        dto.setCounterSignApproved(task.getCounterSignApproved());
        dto.setAssigneeId(task.getAssigneeId());
        dto.setAssigneeName(task.getAssigneeName());
        dto.setStatus(task.getStatus());
        dto.setCreateTime(task.getCreateTime());
        dto.setExpireTime(task.getExpireTime());
        dto.setCompleteTime(task.getCompleteTime());
        dto.setOpinion(task.getOpinion());
        return dto;
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "审批通过")
    public Result<Void> approve(@PathVariable Long id, @RequestBody WfApprovalDTO dto) {
        UserContext.UserInfo currentUser = UserContext.getUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        dto.setAction("APPROVE");
        processEngine.completeTask(id, dto, String.valueOf(currentUser.getUserId()), currentUser.getUsername());
        return Result.success("审批通过", null);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "审批拒绝")
    public Result<Void> reject(@PathVariable Long id, @RequestBody WfApprovalDTO dto) {
        UserContext.UserInfo currentUser = UserContext.getUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        dto.setAction("REJECT");
        processEngine.completeTask(id, dto, String.valueOf(currentUser.getUserId()), currentUser.getUsername());
        return Result.success("审批拒绝", null);
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "驳回到指定节点")
    public Result<Void> returnTask(@PathVariable Long id, @RequestBody WfApprovalDTO dto) {
        UserContext.UserInfo currentUser = UserContext.getUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        dto.setAction("REJECT");
        processEngine.completeTask(id, dto, String.valueOf(currentUser.getUserId()), currentUser.getUsername());
        return Result.success("驳回成功", null);
    }

    @PostMapping("/{id}/transfer")
    @Operation(summary = "转办任务")
    public Result<Void> transfer(@PathVariable Long id, @RequestBody WfApprovalDTO dto) {
        UserContext.UserInfo currentUser = UserContext.getUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        dto.setAction("TRANSFER");
        processEngine.completeTask(id, dto, String.valueOf(currentUser.getUserId()), currentUser.getUsername());
        return Result.success("转办成功", null);
    }

    @PostMapping("/{id}/delegate")
    @Operation(summary = "加签任务")
    public Result<Void> delegate(@PathVariable Long id, @RequestBody WfApprovalDTO dto) {
        UserContext.UserInfo currentUser = UserContext.getUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        dto.setAction("DELEGATE");
        processEngine.completeTask(id, dto, String.valueOf(currentUser.getUserId()), currentUser.getUsername());
        return Result.success("加签成功", null);
    }

    @PostMapping("/{id}/claim")
    @Operation(summary = "签收任务(抢任务)")
    public Result<Void> claim(@PathVariable Long id, @RequestBody WfApprovalDTO dto) {
        UserContext.UserInfo currentUser = UserContext.getUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        dto.setAction("CLAIM");
        processEngine.completeTask(id, dto, String.valueOf(currentUser.getUserId()), currentUser.getUsername());
        return Result.success("签收成功", null);
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "获取任务详情（含业务信息）")
    public Result<WfTaskDTO> getTaskDetail(@PathVariable Long id) {
        WfTask task = taskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        WfTaskDTO dto = convertToDto(task);
        WfInstance instance = instanceMapper.selectById(task.getInstanceId());
        if (instance != null) {
            dto.setInstanceCode(instance.getInstanceCode());
            dto.setTitle(instance.getTitle());
            dto.setApplicantId(instance.getApplicantId());
            dto.setApplicantName(instance.getApplicantName());
            dto.setApplicantDeptName(instance.getApplicantDeptName());
            dto.setDefinitionId(instance.getDefinitionId());
            dto.setDefinitionName(instance.getDefinitionName());
            dto.setBusinessType(instance.getBusinessType());
            dto.setBusinessKey(instance.getBusinessKey());
            dto.setBusinessId(instance.getBusinessId());
        }
        // 查询节点类别
        if (task.getNodeId() != null) {
            WfDefinitionNode node = nodeMapper.selectById(task.getNodeId());
            if (node != null) {
                dto.setNodeCategory(node.getNodeCategory());
            }
        }
        return Result.success(dto);
    }

    @GetMapping("/{id}/reject-nodes")
    @Operation(summary = "获取可驳回到的节点列表")
    public Result<List<Map<String, Object>>> getRejectNodes(@PathVariable Long id) {
        WfTask task = taskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        WfInstance instance = instanceMapper.selectById(task.getInstanceId());
        if (instance == null) {
            return Result.error("流程实例不存在");
        }
        // 获取流程的所有节点
        List<WfDefinitionNode> allNodes = nodeMapper.selectList(
            new LambdaQueryWrapper<WfDefinitionNode>()
                .eq(WfDefinitionNode::getDefinitionId, instance.getDefinitionId())
                .orderByAsc(WfDefinitionNode::getNodeOrder));

        // 获取当前节点在流程中的位置
        WfDefinitionNode currentNode = allNodes.stream()
                .filter(n -> n.getId().equals(task.getNodeId()))
                .findFirst()
                .orElse(null);

        if (currentNode == null) {
            return Result.error("未找到当前节点");
        }

        // 获取历史记录，用于查找每个节点的审批人
        List<WfHistory> histories = historyMapper.selectList(
            new LambdaQueryWrapper<WfHistory>()
                .eq(WfHistory::getInstanceId, instance.getId())
                .orderByAsc(WfHistory::getCreateTime));

        // 找出所有在当前节点之前的节点（可驳回到的节点）
        // 排除END节点，包含START节点（驳回到发起人）
        List<Map<String, Object>> rejectableNodes = new java.util.ArrayList<>();
        for (WfDefinitionNode node : allNodes) {
            // 遇到当前节点则停止（只取之前的节点）
            if (node.getId().equals(task.getNodeId())) {
                break;
            }
            // 排除END节点，START节点可以驳回
            if ("END".equals(node.getNodeCategory())) {
                continue;
            }
            Map<String, Object> nodeInfo = new java.util.HashMap<>();
            nodeInfo.put("nodeId", node.getId());
            nodeInfo.put("nodeCode", node.getNodeCode());
            nodeInfo.put("nodeName", node.getNodeName());
            nodeInfo.put("nodeOrder", node.getNodeOrder());
            nodeInfo.put("nodeCategory", node.getNodeCategory());

            // 查找该节点的最新审批记录，获取审批人信息
            WfHistory lastHistory = histories.stream()
                .filter(h -> h.getNodeId() != null && h.getNodeId().equals(node.getId()))
                .reduce((first, second) -> second)
                .orElse(null);
            if (lastHistory != null) {
                nodeInfo.put("operatorId", lastHistory.getOperatorId());
                nodeInfo.put("operatorName", lastHistory.getOperatorName());
                nodeInfo.put("action", lastHistory.getAction());
            }
            rejectableNodes.add(nodeInfo);
        }
        return Result.success(rejectableNodes);
    }
}
