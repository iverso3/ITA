package com.bank.itarch.controller;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.model.entity.SysUser;
import com.bank.itarch.model.entity.SysRole;
import com.bank.itarch.model.entity.SysOperationLog;
import com.bank.itarch.model.entity.SysMenu;
import com.bank.itarch.service.SysUserService;
import com.bank.itarch.service.SysRoleService;
import com.bank.itarch.service.SysOperationLogService;
import com.bank.itarch.service.SysDepartmentService;
import com.bank.itarch.service.SysMenuService;
import com.bank.itarch.service.SysRoleMenuService;
import com.bank.itarch.service.SysUserRoleService;
import com.bank.itarch.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/system")
@RequiredArgsConstructor
@Tag(name = "系统管理")
public class SystemController {

    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysOperationLogService logService;
    private final SysDepartmentService departmentService;
    private final SysMenuService menuService;
    private final SysRoleMenuService roleMenuService;
    private final SysUserRoleService userRoleService;

    // ===== 用户管理 =====
    @GetMapping("/users")
    @Operation(summary = "用户列表")
    public Result<PageResult<SysUser>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        return Result.success(userService.pageQuery(query, keyword, status, departmentId));
    }

    @GetMapping("/users/all")
    @Operation(summary = "所有用户下拉列表")
    public Result<List<SysUser>> listAllUsers() {
        return Result.success(userService.listAllSimple());
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "用户详情")
    public Result<SysUser> getUser(@PathVariable Long id) {
        return Result.success(userService.detail(id));
    }

    @PostMapping("/users")
    @Operation(summary = "新增用户")
    public Result<SysUser> createUser(@RequestBody SysUser entity) {
        return Result.success("创建成功", userService.create(entity));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "编辑用户")
    public Result<SysUser> updateUser(@PathVariable Long id, @RequestBody SysUser entity) {
        return Result.success("更新成功", userService.update(id, entity));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return Result.success("删除成功", null);
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "启用/禁用用户")
    public Result<Void> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.toggleStatus(id, body.get("status"));
        return Result.success("操作成功", null);
    }

    // ===== 角色管理 =====
    @GetMapping("/roles")
    @Operation(summary = "角色列表")
    public Result<List<SysRole>> listRoles() {
        return Result.success(roleService.listAll());
    }

    @GetMapping("/roles/{id}")
    @Operation(summary = "角色详情")
    public Result<SysRole> getRole(@PathVariable Long id) {
        return Result.success(roleService.detail(id));
    }

    @PostMapping("/roles")
    @Operation(summary = "新增角色")
    public Result<SysRole> createRole(@RequestBody SysRole entity) {
        return Result.success("创建成功", roleService.create(entity));
    }

    @PutMapping("/roles/{id}")
    @Operation(summary = "编辑角色")
    public Result<SysRole> updateRole(@PathVariable Long id, @RequestBody SysRole entity) {
        return Result.success("更新成功", roleService.update(id, entity));
    }

    @DeleteMapping("/roles/{id}")
    @Operation(summary = "删除角色")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success("删除成功", null);
    }

    // ===== 部门管理 =====
    @GetMapping("/departments/tree")
    @Operation(summary = "部门树")
    public Result<List<Map<String, Object>>> departmentTree() {
        return Result.success(departmentService.getTree());
    }

    // ===== 操作日志 =====
    @GetMapping("/logs")
    @Operation(summary = "操作日志列表")
    public Result<PageResult<SysOperationLog>> listLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operatorName) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        return Result.success(logService.pageQuery(query, module, operatorName));
    }

    // ===== 当前用户 =====
    @GetMapping("/users/me")
    @Operation(summary = "获取当前用户")
    public Result<SysUser> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    @PostMapping("/users/{id}/reset-password")
    @Operation(summary = "重置密码")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success("密码已重置为123456", null);
    }

    @PostMapping("/users/{id}/change-password")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.changePassword(id, body.get("oldPassword"), body.get("newPassword"));
        return Result.success("密码修改成功", null);
    }

    // ===== 菜单管理 =====
    @GetMapping("/menus/tree")
    @Operation(summary = "获取完整菜单树")
    public Result<List<Map<String, Object>>> getMenuTree() {
        return Result.success(menuService.getTree());
    }

    @GetMapping("/menus/user-tree")
    @Operation(summary = "获取当前用户菜单权限树")
    public Result<List<Map<String, Object>>> getUserMenuTree() {
        UserContext.UserInfo userInfo = UserContext.getUser();
        List<Long> roleIds = userRoleService.getRoleIdsByUserId(userInfo.getUserId());
        List<Long> menuIds = roleMenuService.getMenuIdsByUserId(null, roleIds);
        return Result.success(menuService.getUserMenuTree(menuIds));
    }

    @GetMapping("/menus")
    @Operation(summary = "菜单列表")
    public Result<List<SysMenu>> listMenus() {
        return Result.success(menuService.list());
    }

    @GetMapping("/menus/{id}")
    @Operation(summary = "菜单详情")
    public Result<SysMenu> getMenu(@PathVariable Long id) {
        return Result.success(menuService.detail(id));
    }

    @PostMapping("/menus")
    @Operation(summary = "新增菜单")
    public Result<SysMenu> createMenu(@RequestBody SysMenu entity) {
        return Result.success("创建成功", menuService.create(entity));
    }

    @PutMapping("/menus/{id}")
    @Operation(summary = "编辑菜单")
    public Result<SysMenu> updateMenu(@PathVariable Long id, @RequestBody SysMenu entity) {
        return Result.success("更新成功", menuService.update(id, entity));
    }

    @DeleteMapping("/menus/{id}")
    @Operation(summary = "删除菜单")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success("删除成功", null);
    }

    // ===== 角色菜单权限 =====
    @GetMapping("/roles/{id}/menu-tree")
    @Operation(summary = "获取角色已分配的菜单树")
    public Result<Map<String, Object>> getRoleMenuTree(@PathVariable Long id) {
        List<Map<String, Object>> allMenus = menuService.getRoleMenuTree(id);
        List<Long> assignedMenuIds = roleMenuService.getMenuIdsByRoleId(id);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("menus", allMenus);
        result.put("assignedMenuIds", assignedMenuIds);
        return Result.success(result);
    }

    @PutMapping("/roles/{id}/menus")
    @Operation(summary = "分配角色菜单权限")
    public Result<Void> assignRoleMenus(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> menuIds = body.get("menuIds");
        roleMenuService.assignMenus(id, menuIds);
        return Result.success("权限分配成功", null);
    }
}
