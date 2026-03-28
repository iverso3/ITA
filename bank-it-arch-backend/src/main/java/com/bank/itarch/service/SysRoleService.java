package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.mapper.SysRoleMapper;
import com.bank.itarch.model.entity.SysRole;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    public List<SysRole> listAll() {
        return list(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSortOrder));
    }

    public SysRole detail(Long id) {
        SysRole role = getById(id);
        if (role == null) throw new BusinessException(1001, "角色不存在");
        return role;
    }

    public SysRole create(SysRole entity) {
        save(entity);
        return entity;
    }

    public SysRole update(Long id, SysRole entity) {
        if (getById(id) == null) throw new BusinessException(1001, "角色不存在");
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(1001, "角色不存在");
        removeById(id);
    }
}
