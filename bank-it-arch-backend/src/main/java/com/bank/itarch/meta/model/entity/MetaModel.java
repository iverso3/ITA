package com.bank.itarch.meta.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bank.itarch.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 元模型定义表
 * 用于定义业务模型的元数据，包括模型编码、名称、类型、关联物理表等信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meta_model")
public class MetaModel extends BaseEntity {

    /** 模型编码（如: app, server, middleware） */
    private String modelCode;

    /** 模型名称 */
    private String modelName;

    /** 模型类型（ENTITY/RELATION/ABSTRACT） */
    private String modelType;

    /** 关联物理表名（为空表示纯动态表） */
    private String tableName;

    /** 物理表别名 */
    private String tableAlias;

    /** 父模型ID（继承关系） */
    private Long parentModelId;

    /** 树形路径 */
    private String treePath;

    /** 当前版本号 */
    private Integer version;

    /** 状态（DRAFT/PUBLISHED/ARCHIVED） */
    private String status;

    /** 是否生成物理表 */
    private Boolean isGeneratable;

    /** 是否启用 */
    private Boolean isActive;

    /** 描述 */
    private String description;

    /** 扩展配置（JSON） */
    private String extraConfig;
}
