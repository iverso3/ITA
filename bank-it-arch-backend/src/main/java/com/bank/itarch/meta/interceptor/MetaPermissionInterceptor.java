package com.bank.itarch.meta.interceptor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bank.itarch.meta.mapper.MetaDataPermissionMapper;
import com.bank.itarch.meta.model.entity.MetaDataPermission;
import com.bank.itarch.meta.util.MetaSQLUtil;
import com.bank.itarch.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据权限拦截器
 * 在执行查询前自动应用数据权限过滤规则
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetaPermissionInterceptor {

    private final MetaDataPermissionMapper permissionMapper;

    /**
     * 获取用户可访问的字段列表
     *
     * @param modelId 模型ID
     * @return 可访问的字段编码列表，null表示全部可访问
     */
    public List<String> getAllowedFields(Long modelId) {
        try {
            UserContext.UserInfo user = UserContext.getUser();
            if (user == null) {
                return null; // 未登录用户，不做字段权限控制
            }

            // 获取该模型的数据权限配置
            List<MetaDataPermission> permissions = getPermissions(modelId, user.getRoleIds());
            if (permissions.isEmpty()) {
                return null; // 没有权限配置，默认全部可访问
            }

            // 收集所有被限制的字段
            Set<String> restrictedFields = new HashSet<>();
            for (MetaDataPermission permission : permissions) {
                if ("FIELD".equals(permission.getPermissionType())) {
                    String fieldCodes = permission.getFieldCodes();
                    if (fieldCodes != null && !fieldCodes.isEmpty()) {
                        try {
                            List<String> fields = JSON.parseArray(fieldCodes, String.class);
                            restrictedFields.addAll(fields);
                        } catch (Exception e) {
                            log.warn("解析字段权限失败: {}", fieldCodes);
                        }
                    }
                }
            }

            // 如果有字段被限制，返回排除受限字段后的字段列表
            if (!restrictedFields.isEmpty()) {
                return null; // TODO: 返回允许的字段列表
            }

            return null; // 无限制
        } catch (Exception e) {
            log.error("获取允许字段失败: modelId={}", modelId, e);
            return null;
        }
    }

    /**
     * 构建行级数据过滤条件
     *
     * @param modelId 模型ID
     * @param user    当前用户
     * @return 过滤条件（SQL片段），null表示不过滤
     */
    public String buildRowFilter(Long modelId, UserContext.UserInfo user) {
        if (user == null) {
            return null;
        }

        try {
            List<MetaDataPermission> permissions = getPermissions(modelId, user.getRoleIds());
            if (permissions.isEmpty()) {
                return null;
            }

            // 收集所有行级过滤条件
            List<String> filters = new ArrayList<>();
            for (MetaDataPermission permission : permissions) {
                if ("ROW".equals(permission.getPermissionType())) {
                    String filterRule = permission.getFilterRule();
                    if (filterRule != null && !filterRule.isEmpty()) {
                        String builtRule = buildFilterRule(filterRule, user);
                        if (builtRule != null && !builtRule.isEmpty()) {
                            filters.add(builtRule);
                        }
                    }
                }
            }

            if (filters.isEmpty()) {
                return null;
            }

            // 使用OR连接多个过滤条件
            return "(" + String.join(" OR ", filters) + ")";

        } catch (Exception e) {
            log.error("构建行级过滤条件失败: modelId={}", modelId, e);
            return null;
        }
    }

    /**
     * 检查用户是否有指定操作的权限
     *
     * @param modelId    模型ID
     * @param action     操作类型（CREATE/READ/UPDATE/DELETE）
     * @return true表示有权限
     */
    public boolean checkActionPermission(Long modelId, String action) {
        try {
            UserContext.UserInfo user = UserContext.getUser();
            if (user == null) {
                return false;
            }

            // 超级管理员拥有所有权限
            if (user.getRoleCodes() != null &&
                    user.getRoleCodes().contains("SUPER_ADMIN")) {
                return true;
            }

            List<MetaDataPermission> permissions = getPermissions(modelId, user.getRoleIds());
            for (MetaDataPermission permission : permissions) {
                if ("ACTION".equals(permission.getPermissionType())) {
                    String allowedActions = permission.getAllowedActions();
                    if (allowedActions != null && allowedActions.contains(action)) {
                        return true;
                    }
                }
            }

            // 如果没有找到限制，默认允许
            return true;

        } catch (Exception e) {
            log.error("检查操作权限失败: modelId={}, action={}", modelId, action, e);
            return true; // 出错时默认允许
        }
    }

    /**
     * 获取用户对指定模型的数据权限
     *
     * @param modelId 模型ID
     * @param roleIds 角色ID列表
     * @return 权限配置列表
     */
    private List<MetaDataPermission> getPermissions(Long modelId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询该模型的所有权限配置
        List<MetaDataPermission> allPermissions = permissionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MetaDataPermission>()
                        .eq(MetaDataPermission::getModelId, modelId)
                        .eq(MetaDataPermission::getIsActive, true)
                        .in(MetaDataPermission::getRoleId, roleIds)
        );

        // 过滤出与用户角色匹配的权限
        return allPermissions.stream()
                .filter(p -> roleIds.contains(p.getRoleId()))
                .collect(Collectors.toList());
    }

    /**
     * 构建过滤规则
     * 支持的规则格式：
     * - {"field": "dept_id", "op": "IN", "value": [1,2,3]}
     * - {"field": "owner_id", "op": "EQ", "value": "current_user"}
     * - {"field": "status", "op": "EQ", "value": "ACTIVE"}
     *
     * @param ruleJson 规则JSON
     * @param user     当前用户
     * @return SQL条件片段
     */
    private String buildFilterRule(String ruleJson, UserContext.UserInfo user) {
        try {
            JSONObject rule = JSON.parseObject(ruleJson);
            String field = rule.getString("field");
            String op = rule.getString("op");
            Object value = rule.get("value");

            // 安全检查：字段名必须安全
            if (!MetaSQLUtil.isSafeIdentifier(field)) {
                log.warn("不安全的字段名: {}", field);
                return null;
            }

            // 处理特殊值
            if ("current_user".equals(value)) {
                value = user.getUserId() != null ? user.getUserId().toString() : user.getUsername();
            } else if ("current_username".equals(value)) {
                value = user.getUsername();
            }

            // 构建SQL条件
            switch (op.toUpperCase()) {
                case "EQ":
                    if (value instanceof String) {
                        return field + " = '" + escapeSQL(String.valueOf(value)) + "'";
                    } else {
                        return field + " = " + value;
                    }

                case "NE":
                    if (value instanceof String) {
                        return field + " != '" + escapeSQL(String.valueOf(value)) + "'";
                    } else {
                        return field + " != " + value;
                    }

                case "IN":
                    if (value instanceof JSONArray) {
                        JSONArray arr = (JSONArray) value;
                        List<String> list = new ArrayList<>();
                        for (int i = 0; i < arr.size(); i++) {
                            Object v = arr.get(i);
                            if (v instanceof String) {
                                list.add("'" + escapeSQL((String) v) + "'");
                            } else {
                                list.add(String.valueOf(v));
                            }
                        }
                        return field + " IN (" + String.join(", ", list) + ")";
                    }
                    return null;

                case "NOT_IN":
                    if (value instanceof JSONArray) {
                        JSONArray arr = (JSONArray) value;
                        List<String> list = new ArrayList<>();
                        for (int i = 0; i < arr.size(); i++) {
                            Object v = arr.get(i);
                            if (v instanceof String) {
                                list.add("'" + escapeSQL((String) v) + "'");
                            } else {
                                list.add(String.valueOf(v));
                            }
                        }
                        return field + " NOT IN (" + String.join(", ", list) + ")";
                    }
                    return null;

                case "LIKE":
                    return field + " LIKE '%" + escapeSQL(String.valueOf(value)) + "%'";

                case "LIKESTART":
                    return field + " LIKE '" + escapeSQL(String.valueOf(value)) + "%'";

                case "IS_NULL":
                    return field + " IS NULL";

                case "IS_NOT_NULL":
                    return field + " IS NOT NULL";

                default:
                    log.warn("不支持的操作符: {}", op);
                    return null;
            }

        } catch (Exception e) {
            log.error("构建过滤规则失败: rule={}", ruleJson, e);
            return null;
        }
    }

    /**
     * SQL注入防护：转义单引号
     */
    private String escapeSQL(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("'", "''");
    }

    /**
     * 过滤数据，只返回用户有权限访问的字段
     *
     * @param data        原始数据Map
     * @param allowedFields 允许访问的字段列表，null表示不过滤
     * @return 过滤后的数据
     */
    public Map<String, Object> filterFields(Map<String, Object> data, List<String> allowedFields) {
        if (data == null || allowedFields == null || allowedFields.isEmpty()) {
            return data;
        }

        Map<String, Object> filtered = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (allowedFields.contains(entry.getKey())) {
                filtered.put(entry.getKey(), entry.getValue());
            } else {
                // 不允许访问的字段返回null或脱敏值
                filtered.put(entry.getKey(), "***");
            }
        }
        return filtered;
    }
}
