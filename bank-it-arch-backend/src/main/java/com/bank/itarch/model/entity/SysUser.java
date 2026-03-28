package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Long departmentId;
    private String departmentName;
    private String status;
    private String userType;
    private String ldapDn;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime passwordExpireTime;
    private Integer isFirstLogin;
    private String remark;
    private Long teamId;
}
