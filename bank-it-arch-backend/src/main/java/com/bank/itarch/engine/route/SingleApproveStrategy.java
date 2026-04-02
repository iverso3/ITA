package com.bank.itarch.engine.route;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bank.itarch.engine.WfEngineContext;
import com.bank.itarch.mapper.SysUserMapper;
import com.bank.itarch.mapper.SysUserRoleMapper;
import com.bank.itarch.model.entity.SysUser;
import com.bank.itarch.model.entity.WfDefinitionNode;
import com.bank.itarch.model.entity.WfTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 单人审批策略
 * 只有一个人审批，一票通过
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SingleApproveStrategy implements WfRouteStrategy {

    private final SysUserRoleMapper userRoleMapper;
    private final SysUserMapper userMapper;

    @Override
    public void handleCandidates(WfEngineContext context, WfTask task, WfDefinitionNode node) {
        // 单人审批：直接指定审批人
        if (node.getApproverId() != null) {
            task.setAssigneeId(node.getApproverId());
            task.setAssigneeName(node.getApproverName());
            log.info("单人审批-指定审批人: userId={}, userName={}",
                    node.getApproverId(), node.getApproverName());
        } else if (node.getAssignedRoleId() != null) {
            // 根据角色查询审批人
            Long userId = userRoleMapper.selectUserIdByRoleId(node.getAssignedRoleId());
            if (userId != null) {
                SysUser user = userMapper.selectById(userId);
                if (user != null) {
                    task.setAssigneeId(user.getId());
                    task.setAssigneeName(user.getUsername());
                    log.info("单人审批-角色审批: roleId={}, userId={}, userName={}",
                            node.getAssignedRoleId(), user.getId(), user.getUsername());
                } else {
                    log.warn("单人审批-角色找到用户但用户为空: roleId={}, userId={}",
                            node.getAssignedRoleId(), userId);
                }
            } else {
                throw new RuntimeException("角色[roleId=" + node.getAssignedRoleId() + "]没有关联的审批人，请先为该角色分配用户");
            }
        }
    }

    @Override
    public boolean checkAndProceed(WfEngineContext context, WfTask task) {
        // 单人审批：直接通过
        return true;
    }
}
