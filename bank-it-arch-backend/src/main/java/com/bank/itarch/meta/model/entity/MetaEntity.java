package com.bank.itarch.meta.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bank.itarch.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 元实体配置表
 * 记录业务实体与元模型的关联，支持混表模式
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meta_entity")
public class MetaEntity extends BaseEntity {

    /** 实体编码（业务标识） */
    private String entityCode;

    /** 实体名称 */
    private String entityName;

    /** 模型ID */
    private Long modelId;

    /** 模型版本ID（发布版本） */
    private Long modelVersionId;

    /** 模型编码（冗余便于查询） */
    private String modelCode;

    /** 关联业务表主键（混表模式时使用） */
    private Long businessId;

    /** 状态（ACTIVE/LOCKED/DELETED） */
    private String status;

    /** 锁定原因 */
    private String lockReason;

    /** 锁定时间 */
    private LocalDateTime lockedAt;

    /** 锁定人 */
    private String lockedBy;

    /** 拥有者ID（数据权限） */
    private String ownerId;

    /** 拥有者类型（USER/ROLE/DEPARTMENT） */
    private String ownerType;

    /** 所属部门（数据权限） */
    private Long deptId;

    /** 部门路径 */
    private String deptPath;

    /** 数据版本（乐观锁） */
    private Integer dataVersion;

    /** 描述 */
    private String description;

    /** 创建人部门 */
    private Long creatorDeptId;
}
