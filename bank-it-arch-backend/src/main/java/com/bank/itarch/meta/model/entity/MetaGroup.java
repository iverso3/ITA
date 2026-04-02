package com.bank.itarch.meta.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bank.itarch.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字段分组定义表
 * 用于将模型的字段分组管理，如安全信息组、运维信息组等
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meta_group")
public class MetaGroup extends BaseEntity {

    /** 分组编码（如: security_group, ops_group） */
    private String groupCode;

    /** 分组名称 */
    private String groupName;

    /** 分组类型（SECURITY/OPS/BASIC/DESCRIPTION/RELATION） */
    private String groupType;

    /** 所属模型ID */
    private Long modelId;

    /** 排序 */
    private Integer sortOrder;

    /** 描述 */
    private String description;

    /** 是否启用 */
    private Boolean isActive;
}
