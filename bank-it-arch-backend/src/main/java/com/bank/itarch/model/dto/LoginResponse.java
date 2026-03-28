package com.bank.itarch.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "Token类型")
    private String tokenType;

    @Schema(description = "过期时间(秒)")
    private Long expiresIn;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "角色列表")
    private List<Map<String, Object>> roles;

    @Schema(description = "菜单权限树")
    private List<Map<String, Object>> menuTree;

    @Schema(description = "按钮权限列表")
    private List<String> permissions;
}
