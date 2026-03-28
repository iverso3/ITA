package com.bank.itarch.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.config.JwtConfig;
import com.bank.itarch.model.dto.LoginRequest;
import com.bank.itarch.model.dto.LoginResponse;
import com.bank.itarch.model.entity.SysMenu;
import com.bank.itarch.model.entity.SysRole;
import com.bank.itarch.model.entity.SysUser;
import com.bank.itarch.service.AuthService;
import com.bank.itarch.service.SysMenuService;
import com.bank.itarch.service.SysRoleMenuService;
import com.bank.itarch.service.SysRoleService;
import com.bank.itarch.service.SysUserRoleService;
import com.bank.itarch.service.SysUserService;
import com.bank.itarch.util.UserContext;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysMenuService menuService;
    private final SysUserRoleService userRoleService;
    private final SysRoleMenuService roleMenuService;
    private final JwtConfig jwtConfig;

    @Override
    public LoginResponse login(LoginRequest request) {
        if (!StrUtil.isNotBlank(request.getUsername()) || !StrUtil.isNotBlank(request.getPassword())) {
            throw new BusinessException(1001, "用户名或密码不能为空");
        }

        SysUser user = userService.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(1001, "用户名或密码错误");
        }

        if (!"ENABLED".equals(user.getStatus())) {
            throw new BusinessException(1001, "账号已被禁用");
        }

        String md5Password = SecureUtil.md5(request.getPassword());
        if (!md5Password.equals(user.getPassword())) {
            throw new BusinessException(1001, "用户名或密码错误");
        }

        List<Long> roleIds = userRoleService.getRoleIdsByUserId(user.getId());
        List<String> roleCodes = userRoleService.getRoleCodesByUserId(user.getId());

        String token = generateToken(user.getId(), user.getUsername(), roleIds, roleCodes);

        userService.updateLoginInfo(user.getId(), null);

        return buildLoginResponse(user, roleIds, token);
    }

    @Override
    public LoginResponse getCurrentUser() {
        UserContext.UserInfo userInfo = UserContext.getUser();
        if (userInfo == null) {
            throw new BusinessException(1001, "用户未登录");
        }

        SysUser user = userService.getById(userInfo.getUserId());
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }

        List<Long> roleIds = userRoleService.getRoleIdsByUserId(user.getId());

        return buildLoginResponse(user, roleIds, null);
    }

    @Override
    public void logout() {
        // 前端清除token，后端无需处理
    }

    private String generateToken(Long userId, String username, List<Long> roleIds, List<String> roleCodes) {
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("roleIds", roleIds)
                .claim("roleCodes", roleCodes)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    private LoginResponse buildLoginResponse(SysUser user, List<Long> roleIds, String token) {
        List<Map<String, Object>> roles = new ArrayList<>();
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysRole> roleList = roleService.listByIds(roleIds);
            roles = roleList.stream().map(role -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", role.getId());
                map.put("roleCode", role.getRoleCode());
                map.put("roleName", role.getRoleName());
                return map;
            }).collect(Collectors.toList());
        }

        List<Map<String, Object>> menuTree = getMenuTreeByRoles(roleIds);
        List<String> permissions = getPermissionsByRoles(roleIds);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpiration() / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .departmentName(user.getDepartmentName())
                .roles(roles)
                .menuTree(menuTree)
                .permissions(permissions)
                .build();
    }

    private List<Map<String, Object>> getMenuTreeByRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> menuIds = roleMenuService.getMenuIdsByUserId(null, roleIds);
        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<SysMenu> allMenus = menuService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getIsActive, 1)
                .orderByAsc(SysMenu::getSortOrder));

        return buildMenuTree(allMenus, 0L);
    }

    private List<Map<String, Object>> buildMenuTree(List<SysMenu> menus, Long parentId) {
        List<SysMenu> children = menus.stream()
                .filter(m -> (m.getParentId() == null && parentId == 0) ||
                        (m.getParentId() != null && m.getParentId().equals(parentId)))
                .sorted(Comparator.comparingInt(m -> m.getSortOrder() == null ? 0 : m.getSortOrder()))
                .collect(Collectors.toList());

        return children.stream().map(menu -> {
            Map<String, Object> node = new HashMap<>();
            node.put("id", menu.getId());
            node.put("menuCode", menu.getMenuCode());
            node.put("menuName", menu.getMenuName());
            node.put("menuType", menu.getMenuType());
            node.put("icon", menu.getIcon());
            node.put("path", menu.getPath());
            node.put("component", menu.getComponent());
            node.put("permission", menu.getPermission());
            node.put("isVisible", menu.getIsVisible());

            if (!"BUTTON".equals(menu.getMenuType())) {
                List<Map<String, Object>> childMenus = buildMenuTree(menus, menu.getId());
                if (!childMenus.isEmpty()) {
                    node.put("children", childMenus);
                }
            }
            return node;
        }).collect(Collectors.toList());
    }

    private List<String> getPermissionsByRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> menuIds = roleMenuService.getMenuIdsByUserId(null, roleIds);
        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<SysMenu> menus = menuService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .isNotNull(SysMenu::getPermission)
                .ne(SysMenu::getPermission, "")
                .eq(SysMenu::getIsActive, 1));

        return menus.stream()
                .map(SysMenu::getPermission)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}
