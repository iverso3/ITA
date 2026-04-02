package com.bank.itarch.meta.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bank.itarch.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据权限配置表
 * 定义行级权限、字段级权限和操作权限
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meta_data_permission")
public class MetaDataPermission extends BaseEntity {

    /** 权限编码 */
    private String permissionCode;

    /** 权限名称 */
    private String permissionName;

    /** 模型ID */
    private Long modelId;

    /** 权限类型（FIELD/ROW/ACTION） */
    private String permissionType;

    /** 字段编码列表（字段级权限） */
    private String fieldCodes;

    /** 过滤规则（行级权限） */
    private String filterRule;

    /** 允许的操作（CREATE/READ/UPDATE/DELETE） */
    private String allowedActions;

    /** 关联角色ID */
    private Long roleId;

    /** 描述 */
    private String description;

    /** 是否启用 */
    private Boolean isActive;
}
