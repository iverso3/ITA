package com.bank.itarch.engine.route;

import com.bank.itarch.engine.WfEngineContext;
import com.bank.itarch.mapper.WfTaskCounterSignMapper;
import com.bank.itarch.mapper.WfTaskMapper;
import com.bank.itarch.model.entity.WfDefinitionNode;
import com.bank.itarch.model.entity.WfTask;
import com.bank.itarch.model.entity.WfTaskCounterSign;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 或签策略
 * 多人审批，任一人通过即可流转，其他任务自动取消
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrSignStrategy implements WfRouteStrategy {

    private final WfTaskCounterSignMapper counterSignMapper;
    private final WfTaskMapper taskMapper;

    @Override
    public void handleCandidates(WfEngineContext context, WfTask task, WfDefinitionNode node) {
        // 或签模式：创建或签任务
        task.setTaskCategory("COUNTER_SIGN");
        task.setCounterSignType("OR");
        task.setCounterSignCount(node.getCounterSignCount() != null ? node.getCounterSignCount() : 3);
        task.setCounterSignApproved(0);
        task.setStatus("PENDING");

        log.info("或签模式-创建或签任务: taskId={}, requiredCount={}",
                task.getId(), task.getCounterSignCount());
    }

    @Override
    public boolean checkAndProceed(WfEngineContext context, WfTask task) {
        // 获取会签记录
        var counterSigns = counterSignMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WfTaskCounterSign>()
                .eq(WfTaskCounterSign::getTaskId, task.getId()));

        if (counterSigns == null || counterSigns.isEmpty()) {
            return false;
        }

        // 检查是否有人同意
        boolean anyApproved = counterSigns.stream()
                .anyMatch(cs -> cs.getApproved() != null && cs.getApproved() == 1);
        if (anyApproved) {
            log.info("或签任务已通过，其他任务将取消: taskId={}", task.getId());

            // 取消其他待审批任务
            cancelOtherTasks(context, task);

            return true;
        }

        // 检查是否有人拒绝
        boolean anyRejected = counterSigns.stream()
                .anyMatch(cs -> cs.getApproved() != null && cs.getApproved() == 0);
        if (anyRejected) {
            log.info("或签任务被拒绝: taskId={}", task.getId());
            return false;
        }

        return false;
    }

    private void cancelOtherTasks(WfEngineContext context, WfTask approvedTask) {
        // 取消同一节点的其他任务
        taskMapper.update(null,
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<WfTask>()
                .eq(WfTask::getInstanceId, approvedTask.getInstanceId())
                .eq(WfTask::getNodeId, approvedTask.getNodeId())
                .ne(WfTask::getId, approvedTask.getId())
                .eq(WfTask::getStatus, "PENDING")
                .set(WfTask::getStatus, "CANCELLED"));
    }
}
