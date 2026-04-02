package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.OssFlowRoleMapper;
import com.bank.itarch.mapper.OssFlowRoleUserRelMapper;
import com.bank.itarch.model.entity.OssFlowRole;
import com.bank.itarch.model.entity.OssFlowRoleUserRel;
import com.bank.itarch.util.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OssFlowRoleUserRelService extends ServiceImpl<OssFlowRoleUserRelMapper, OssFlowRoleUserRel> {

    private final OssFlowRoleMapper flowRoleMapper;

    public OssFlowRoleUserRelService(OssFlowRoleMapper flowRoleMapper) {
        this.flowRoleMapper = flowRoleMapper;
    }

    public PageResult<OssFlowRoleUserRel> pageQuery(PageQuery query, String userName, String flowRoleName,
                                                     String branchId, String teamId, String orgId,
                                                     String updateUserId) {
        Page<OssFlowRoleUserRel> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<OssFlowRoleUserRel> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(userName), OssFlowRoleUserRel::getUserName, userName)
               .like(StringUtils.hasText(flowRoleName), OssFlowRoleUserRel::getFlowRoleName, flowRoleName)
               .eq(StringUtils.hasText(branchId), OssFlowRoleUserRel::getBranchId, branchId)
               .eq(StringUtils.hasText(teamId), OssFlowRoleUserRel::getTeamId, teamId)
               .eq(StringUtils.hasText(orgId), OssFlowRoleUserRel::getOrgId, orgId)
               .like(StringUtils.hasText(updateUserId), OssFlowRoleUserRel::getUpdateUserId, updateUserId)
               .orderByDesc(OssFlowRoleUserRel::getCreateDatetime);
        Page<OssFlowRoleUserRel> result = page(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    public OssFlowRoleUserRel detail(String relId) {
        OssFlowRoleUserRel rel = getById(relId);
        if (rel == null) throw new BusinessException(5002, "用户流程角色关系不存在");
        return rel;
    }

    public OssFlowRoleUserRel create(OssFlowRoleUserRel entity) {
        // 校验用户-角色组合是否已存在（同一用户不能被分配到同一角色多次）
        if (StringUtils.hasText(entity.getUserId()) && StringUtils.hasText(entity.getFlowRoleId())) {
            if (existsByUserIdAndFlowRoleId(entity.getUserId(), entity.getFlowRoleId())) {
                throw new BusinessException(5003, "该用户已分配此角色，请勿重复添加");
            }
        }
        // 设置创建人和创建时间
        if (UserContext.getUser() != null) {
            entity.setCreateUserId(UserContext.getUser().getUserId().toString());
        }
        entity.setCreateDatetime(LocalDateTime.now());
        // 自动填充 flowRoleName
        if (StringUtils.hasText(entity.getFlowRoleId()) && !StringUtils.hasText(entity.getFlowRoleName())) {
            OssFlowRole role = flowRoleMapper.selectById(entity.getFlowRoleId());
            if (role != null) {
                entity.setFlowRoleName(role.getFlowRoleName());
            }
        }
        // updateUserId 设置默认值避免数据库 NOT NULL 约束
        if (entity.getUpdateUserId() == null) {
            entity.setUpdateUserId("system");
        }
        if (entity.getUpdateDatetime() == null) {
            entity.setUpdateDatetime(LocalDateTime.now());
        }
        if (entity.getLogicStatus() == null) {
            entity.setLogicStatus("0");
        }
        save(entity);
        return entity;
    }

    public OssFlowRoleUserRel update(String relId, OssFlowRoleUserRel entity) {
        if (getById(relId) == null) throw new BusinessException(5002, "用户流程角色关系不存在");
        entity.setRelId(relId);
        entity.setUpdateUserId(UserContext.getUser() != null ? UserContext.getUser().getUserId().toString() : null);
        entity.setUpdateDatetime(LocalDateTime.now());
        updateById(entity);
        return getById(relId);
    }

    public void delete(String relId) {
        if (getById(relId) == null) throw new BusinessException(5002, "用户流程角色关系不存在");
        removeById(relId);
    }

    /**
     * 检查用户-角色组合是否已存在（同一用户不能被分配到同一角色多次）
     */
    public boolean existsByUserIdAndFlowRoleId(String userId, String flowRoleId) {
        return count(new LambdaQueryWrapper<OssFlowRoleUserRel>()
                .eq(OssFlowRoleUserRel::getUserId, userId)
                .eq(OssFlowRoleUserRel::getFlowRoleId, flowRoleId)
                .eq(OssFlowRoleUserRel::getLogicStatus, "0")) > 0;
    }

    /**
     * 根据流程角色ID查询所有关联的用户
     */
    public List<OssFlowRoleUserRel> getUsersByFlowRoleId(String flowRoleId) {
        if (flowRoleId == null) {
            throw new BusinessException(5002, "流程角色ID不能为空");
        }
        return list(new LambdaQueryWrapper<OssFlowRoleUserRel>()
                .eq(OssFlowRoleUserRel::getFlowRoleId, flowRoleId)
                .eq(OssFlowRoleUserRel::getLogicStatus, "0"));
    }
}