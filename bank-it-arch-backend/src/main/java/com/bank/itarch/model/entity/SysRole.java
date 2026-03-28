package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {
    private String roleCode;
    private String roleName;
    private String roleType;
    private String dataScope;
    private String description;
    private Integer isActive;
    private Integer sortOrder;
    private Long teamId;
    private String remark;
}
