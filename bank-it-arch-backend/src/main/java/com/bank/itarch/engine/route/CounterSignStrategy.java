package com.bank.itarch.engine.route;

import com.bank.itarch.engine.WfEngineContext;
import com.bank.itarch.mapper.WfTaskCounterSignMapper;
import com.bank.itarch.model.entity.WfDefinitionNode;
import com.bank.itarch.model.entity.WfTask;
import com.bank.itarch.model.entity.WfTaskCounterSign;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 会签策略
 * 多人审批，必须全部通过才能流转
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CounterSignStrategy implements WfRouteStrategy {

    private final WfTaskCounterSignMapper counterSignMapper;

    @Override
    public void handleCandidates(WfEngineContext context, WfTask task, WfDefinitionNode node) {
        // 会签模式：创建会签任务
        task.setTaskCategory("COUNTER_SIGN");
        task.setCounterSignType("COUNTER");
        task.setCounterSignCount(node.getCounterSignCount() != null ? node.getCounterSignCount() : 3);
        task.setCounterSignApproved(0);
        task.setStatus("PENDING");

        log.info("会签模式-创建会签任务: taskId={}, requiredCount={}",
                task.getId(), task.getCounterSignCount());
    }

    @Override
    public boolean checkAndProceed(WfEngineContext context, WfTask task) {
        // 检查是否有人拒绝
        var counterSigns = counterSignMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WfTaskCounterSign>()
                .eq(WfTaskCounterSign::getTaskId, task.getId()));

        if (counterSigns == null || counterSigns.isEmpty()) {
            return false;
        }

        // 检查是否有人拒绝
        boolean anyRejected = counterSigns.stream()
                .anyMatch(cs -> cs.getApproved() != null && cs.getApproved() == 0);
        if (anyRejected) {
            log.info("会签任务被拒绝: taskId={}", task.getId());
            return false;
        }

        // 检查是否所有人都已审批
        long approvedCount = counterSigns.stream()
                .filter(cs -> cs.getApproved() != null && cs.getApproved() == 1)
                .count();

        task.setCounterSignApproved((int) approvedCount);

        // 全部审批通过
        return approvedCount >= task.getCounterSignCount();
    }
}
