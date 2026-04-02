package com.bank.itarch.engine.node;

import com.bank.itarch.engine.WfEngineContext;
import com.bank.itarch.engine.execution.WfToken;

/**
 * 节点处理器接口
 */
public interface WfNodeHandler {

    /**
     * 获取节点类型
     */
    String getNodeType();

    /**
     * 进入节点
     * @param context 引擎上下文
     * @param token 流程令牌
     */
    void enter(WfEngineContext context, WfToken token);

    /**
     * 节点任务完成
     * @param context 引擎上下文
     * @param token 流程令牌
     */
    void complete(WfEngineContext context, WfToken token);

    /**
     * 离开节点
     * @param context 引擎上下文
     * @param token 流程令牌
     */
    void leave(WfEngineContext context, WfToken token);
}
