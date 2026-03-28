package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.mapper.SysRoleMenuMapper;
import com.bank.itarch.model.entity.SysMenu;
import com.bank.itarch.model.entity.SysRoleMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleMenuService extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> {

    private final SysMenuService sysMenuService;

    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    public List<Long> getMenuIdsByUserId(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return baseMapper.selectMenuIdsByRoleIds(roleIds);
    }

    public List<SysMenu> getMenusByRoleId(Long roleId) {
        List<Long> menuIds = getMenuIdsByRoleId(roleId);
        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }
        return sysMenuService.list(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getIsActive, 1));
    }

    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        baseMapper.deleteByRoleId(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            baseMapper.insertBatch(roleId, menuIds);
        }
    }

    public boolean hasPermission(Long userId, List<Long> roleIds, String permission) {
        if (!StringUtils.hasText(permission)) {
            return true;
        }
        List<SysMenu> menus = getMenusByUserRoles(roleIds);
        return menus.stream().anyMatch(m -> permission.equals(m.getPermission()));
    }

    public boolean hasMenuPermission(Long userId, List<Long> roleIds, String path) {
        List<SysMenu> menus = getMenusByUserRoles(roleIds);
        return menus.stream().anyMatch(m -> path.equals(m.getPath()));
    }

    private List<SysMenu> getMenusByUserRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> menuIds = getMenuIdsByUserId(null, roleIds);
        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }
        return sysMenuService.list(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getIsActive, 1));
    }
}
