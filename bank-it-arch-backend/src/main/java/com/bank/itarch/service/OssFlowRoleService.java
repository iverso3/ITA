package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.OssFlowRoleMapper;
import com.bank.itarch.model.entity.OssFlowRole;
import com.bank.itarch.util.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OssFlowRoleService extends ServiceImpl<OssFlowRoleMapper, OssFlowRole> {

    public PageResult<OssFlowRole> pageQuery(PageQuery query, String flowRoleName, String flowRoleDesc) {
        Page<OssFlowRole> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<OssFlowRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(flowRoleName), OssFlowRole::getFlowRoleName, flowRoleName)
               .like(StringUtils.hasText(flowRoleDesc), OssFlowRole::getFlowRoleDesc, flowRoleDesc)
               .orderByAsc(OssFlowRole::getFlowRowSeq);
        Page<OssFlowRole> result = page(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    public OssFlowRole detail(String flowRoleId) {
        OssFlowRole role = getById(flowRoleId);
        if (role == null) throw new BusinessException(5001, "流程角色不存在");
        return role;
    }

    public OssFlowRole create(OssFlowRole entity) {
        // 设置创建人和创建时间
        if (UserContext.getUser() != null) {
            entity.setCreateUserId(UserContext.getUser().getUserId().toString());
        }
        entity.setCreateDatetime(LocalDateTime.now());
        // 设置默认值避免数据库 NOT NULL 约束
        if (entity.getUpdateUserId() == null) {
            entity.setUpdateUserId("system");
        }
        if (entity.getUpdateTime() == null) {
            entity.setUpdateTime(LocalDateTime.now());
        }
        if (entity.getLogicStatus() == null) {
            entity.setLogicStatus("0");
        }
        save(entity);
        return entity;
    }

    public OssFlowRole update(String flowRoleId, OssFlowRole entity) {
        if (getById(flowRoleId) == null) throw new BusinessException(5001, "流程角色不存在");
        entity.setFlowRoleId(flowRoleId);
        entity.setUpdateUserId(UserContext.getUser() != null ? UserContext.getUser().getUserId().toString() : "system");
        entity.setUpdateTime(LocalDateTime.now());
        updateById(entity);
        return getById(flowRoleId);
    }

    public void delete(String flowRoleId) {
        if (getById(flowRoleId) == null) throw new BusinessException(5001, "流程角色不存在");
        removeById(flowRoleId);
    }

    public List<OssFlowRole> listAll() {
        return list(new LambdaQueryWrapper<OssFlowRole>()
                .orderByAsc(OssFlowRole::getFlowRowSeq));
    }
}