package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.mapper.SysMenuMapper;
import com.bank.itarch.model.entity.SysMenu;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> {

    public List<Map<String, Object>> getTree() {
        List<SysMenu> all = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getIsActive, 1)
                .orderByAsc(SysMenu::getSortOrder));
        Map<Long, List<SysMenu>> grouped = all.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() == null ? 0L : m.getParentId()));
        return buildTree(grouped, 0L);
    }

    private List<Map<String, Object>> buildTree(Map<Long, List<SysMenu>> grouped, Long parentId) {
        List<SysMenu> children = grouped.getOrDefault(parentId, Collections.emptyList());
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysMenu menu : children) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", menu.getId());
            node.put("menuCode", menu.getMenuCode());
            node.put("menuName", menu.getMenuName());
            node.put("menuType", menu.getMenuType());
            node.put("icon", menu.getIcon());
            node.put("path", menu.getPath());
            node.put("component", menu.getComponent());
            node.put("isVisible", menu.getIsVisible());
            node.put("permission", menu.getPermission());
            node.put("children", buildTree(grouped, menu.getId()));
            result.add(node);
        }
        return result;
    }

    public SysMenu detail(Long id) {
        SysMenu menu = getById(id);
        if (menu == null) throw new BusinessException(1001, "菜单不存在");
        return menu;
    }

    public SysMenu create(SysMenu entity) {
        save(entity);
        return entity;
    }

    public SysMenu update(Long id, SysMenu entity) {
        if (getById(id) == null) throw new BusinessException(1001, "菜单不存在");
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(1001, "菜单不存在");
        removeById(id);
    }

    public List<Map<String, Object>> getUserMenuTree(List<Long> menuIds) {
        List<SysMenu> menus = list(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getIsActive, 1)
                .orderByAsc(SysMenu::getSortOrder));
        Map<Long, List<SysMenu>> grouped = menus.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() == null ? 0L : m.getParentId()));
        return buildTree(grouped, 0L);
    }

    public List<Map<String, Object>> getRoleMenuTree(Long roleId) {
        List<SysMenu> all = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getIsActive, 1)
                .orderByAsc(SysMenu::getSortOrder));
        Map<Long, List<SysMenu>> grouped = all.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() == null ? 0L : m.getParentId()));
        return buildTree(grouped, 0L);
    }
}
