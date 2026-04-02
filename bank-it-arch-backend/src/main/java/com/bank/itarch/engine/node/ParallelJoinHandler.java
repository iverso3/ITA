package com.bank.itarch.engine.node;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bank.itarch.engine.WfEngineContext;
import com.bank.itarch.engine.execution.WfToken;
import com.bank.itarch.mapper.WfParallelJoinMapper;
import com.bank.itarch.model.entity.WfParallelJoin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 并行汇聚节点处理器
 * 等待所有并行分支完成后，汇聚继续流转
 */
@Slf4j
@Component
public class ParallelJoinHandler extends AbstractNodeHandler {

    @Override
    public String getNodeType() {
        return "PARALLEL_JOIN";
    }

    @Override
    public void enter(WfEngineContext context, WfToken token) {
        log.info("进入并行汇聚节点: nodeId={}, instanceId={}",
                token.getCurrentNodeId(), token.getInstance().getId());
        super.enter(context, token);

        // 更新当前分支完成状态
        updateBranchStatus(context, token.getInstance().getId(), token.getCurrentNodeId());

        // 检查是否所有分支都已完成
        if (areAllBranchesCompleted(context, token)) {
            // 所有分支完成，汇聚继续流转
            log.info("所有并行分支完成，汇聚继续流转");
            complete(context, token);
        } else {
            // 还有分支未完成，等待
            token.setTokenStatus(WfToken.STATUS_WAITING);
            log.info("还有并行分支未完成，等待...");
        }
    }

    private void updateBranchStatus(WfEngineContext context, Long instanceId, Long branchNodeId) {
        WfParallelJoin join = new WfParallelJoin();
        join.setBranchStatus("COMPLETED");
        join.setCompleteTime(LocalDateTime.now());

        context.getParallelJoinMapper().update(join,
            new LambdaQueryWrapper<WfParallelJoin>()
                .eq(WfParallelJoin::getInstanceId, instanceId)
                .eq(WfParallelJoin::getBranchNodeId, branchNodeId));
    }

    private boolean areAllBranchesCompleted(WfEngineContext context, WfToken token) {
        Long instanceId = token.getInstance().getId();
        Long parallelNodeId = findParallelBranchNodeId(context, token.getCurrentNodeId());

        if (parallelNodeId == null) {
            // 没有找到对应的并行分支，认为已完成
            return true;
        }

        List<WfParallelJoin> joins = context.getParallelJoinMapper().selectList(
            new LambdaQueryWrapper<WfParallelJoin>()
                .eq(WfParallelJoin::getInstanceId, instanceId)
                .eq(WfParallelJoin::getParallelNodeId, parallelNodeId));

        if (joins == null || joins.isEmpty()) {
            return true;
        }

        // 检查是否所有分支都是COMPLETED或CANCELLED状态
        return joins.stream().allMatch(j ->
            "COMPLETED".equals(j.getBranchStatus()) || "CANCELLED".equals(j.getBranchStatus()));
    }

    private Long findParallelBranchNodeId(WfEngineContext context, Long currentNodeId) {
        // 从当前汇聚节点反向查找对应的并行分支节点
        Long instanceId = context.getInstance().getId();

        // Find the parallel join record for the current branch node
        WfParallelJoin join = context.getParallelJoinMapper().selectOne(
            new LambdaQueryWrapper<WfParallelJoin>()
                .eq(WfParallelJoin::getInstanceId, instanceId)
                .eq(WfParallelJoin::getBranchNodeId, currentNodeId));
        return join != null ? join.getParallelNodeId() : null;
    }

    @Override
    public void leave(WfEngineContext context, WfToken token) {
        // 父类会流转到下一个节点
        super.leave(context, token);
    }
}
