package com.bank.itarch.engine.node;

import com.bank.itarch.engine.WfEngineContext;
import com.bank.itarch.engine.execution.WfToken;
import com.bank.itarch.model.entity.WfInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * 结束节点处理器
 */
@Slf4j
@Component
public class EndNodeHandler extends AbstractNodeHandler {

    @Override
    public String getNodeType() {
        return "END";
    }

    @Override
    public void enter(WfEngineContext context, WfToken token) {
        log.info("流程结束: instanceId={}", token.getInstance().getId());
        super.enter(context, token);

        // 标记流程实例为完成
        WfInstance instance = token.getInstance();
        instance.setStatus("COMPLETED");
        instance.setEndTime(LocalDateTime.now());
        instance.setResult("APPROVED");

        // 计算流程时长
        if (instance.getStartTime() != null) {
            long duration = java.time.Duration.between(
                instance.getStartTime(), instance.getEndTime()).toMillis();
            instance.setDuration(duration);
        }

        // 更新实例状态
        context.getInstanceMapper().updateById(instance);

        // 标记令牌为完成
        token.setTokenStatus(WfToken.STATUS_COMPLETED);
    }

    @Override
    public void leave(WfEngineContext context, WfToken token) {
        // 结束节点不需要再流转
        log.info("流程实例完成: id={}, result={}", token.getInstance().getId(), token.getInstance().getResult());
    }
}
