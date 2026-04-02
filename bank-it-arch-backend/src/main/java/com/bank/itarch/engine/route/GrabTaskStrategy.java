package com.bank.itarch.engine.route;

import com.bank.itarch.engine.WfEngineContext;
import com.bank.itarch.model.entity.WfDefinitionNode;
import com.bank.itarch.model.entity.WfTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 抢任务策略
 * 多个候选人，谁先抢到谁审批
 */
@Slf4j
@Component
public class GrabTaskStrategy implements WfRouteStrategy {

    @Override
    public void handleCandidates(WfEngineContext context, WfTask task, WfDefinitionNode node) {
        // 抢任务模式：任务不指定具体审批人
        // 状态设为PENDING，由候选人主动签收
        task.setAssigneeId(null);
        task.setAssigneeName(null);
        task.setStatus("PENDING");
        log.info("抢任务模式-任务创建，等待签收: taskId={}", task.getId());
    }

    @Override
    public boolean checkAndProceed(WfEngineContext context, WfTask task) {
        // 抢任务模式需要检查任务是否已被签收
        // 如果未签收，不继续流转
        return task.getAssigneeId() != null;
    }
}
