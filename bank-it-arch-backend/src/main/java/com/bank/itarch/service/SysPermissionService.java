package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.mapper.SysPermissionMapper;
import com.bank.itarch.model.entity.SysPermission;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysPermissionService extends ServiceImpl<SysPermissionMapper, SysPermission> {

    public List<SysPermission> listByMenuId(Long menuId) {
        return list(new LambdaQueryWrapper<SysPermission>()
                .eq(menuId != null, SysPermission::getMenuId, menuId)
                .eq(SysPermission::getIsEnabled, 1)
                .orderByAsc(SysPermission::getSortOrder));
    }

    public SysPermission detail(Long id) {
        SysPermission permission = getById(id);
        if (permission == null) throw new BusinessException(1001, "权限不存在");
        return permission;
    }

    public SysPermission create(SysPermission entity) {
        save(entity);
        return entity;
    }

    public SysPermission update(Long id, SysPermission entity) {
        if (getById(id) == null) throw new BusinessException(1001, "权限不存在");
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(1001, "权限不存在");
        removeById(id);
    }
}
