package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {
    private String permissionCode;
    private String permissionName;
    private String permissionType;
    private Long menuId;
    private String resourceType;
    private Long resourceId;
    private String permission;
    private String method;
    private Integer isEnabled;
    private Integer sortOrder;
}
