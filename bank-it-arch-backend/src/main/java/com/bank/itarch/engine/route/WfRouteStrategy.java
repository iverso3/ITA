package com.bank.itarch.engine.route;

import com.bank.itarch.engine.WfEngineContext;
import com.bank.itarch.model.entity.WfDefinitionNode;
import com.bank.itarch.model.entity.WfTask;

/**
 * 路由策略接口
 */
public interface WfRouteStrategy {

    /**
     * 处理候选人
     */
    void handleCandidates(WfEngineContext context, WfTask task, WfDefinitionNode node);

    /**
     * 完成任务后检查是否继续流转
     * @return true=继续流转, false=等待其他条件
     */
    boolean checkAndProceed(WfEngineContext context, WfTask task);
}
