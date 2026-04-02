package com.bank.itarch.meta.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bank.itarch.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 关系定义表
 * 定义模型之间的关系，如N:1、1:N、N:N、1:1、自引用等
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meta_relationship")
public class MetaRelationship extends BaseEntity {

    /** 关系编码 */
    private String relCode;

    /** 关系名称 */
    private String relName;

    /** 关系类型（N:1/1:N/N:N/1:1） */
    private String relType;

    /** 源模型ID */
    private Long sourceModelId;

    /** 源字段ID（可选） */
    private Long sourceFieldId;

    /** 目标模型ID */
    private Long targetModelId;

    /** 目标字段ID（可选） */
    private Long targetFieldId;

    /** 是否级联删除 */
    private Boolean isCascadeDelete;

    /** 是否自引用 */
    private Boolean isSelfRel;

    /** 关系配置 */
    private String relConfig;

    /** 描述 */
    private String description;

    /** 是否启用 */
    private Boolean isActive;
}
