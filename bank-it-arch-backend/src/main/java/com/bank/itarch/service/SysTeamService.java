package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.mapper.SysTeamMapper;
import com.bank.itarch.model.entity.SysTeam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysTeamService extends ServiceImpl<SysTeamMapper, SysTeam> {

    public List<SysTeam> listByDepartmentId(Long departmentId) {
        return list(new LambdaQueryWrapper<SysTeam>()
                .eq(departmentId != null, SysTeam::getDepartmentId, departmentId)
                .eq(SysTeam::getIsActive, 1)
                .orderByAsc(SysTeam::getSortOrder));
    }

    public SysTeam detail(Long id) {
        SysTeam team = getById(id);
        if (team == null) throw new BusinessException(1001, "团队不存在");
        return team;
    }

    public SysTeam create(SysTeam entity) {
        save(entity);
        return entity;
    }

    public SysTeam update(Long id, SysTeam entity) {
        if (getById(id) == null) throw new BusinessException(1001, "团队不存在");
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(1001, "团队不存在");
        removeById(id);
    }
}
