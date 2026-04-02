package com.bank.itarch.meta.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bank.itarch.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 实体标签关联表
 * 记录实体与标签的多对多关系
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meta_entity_tag")
public class MetaEntityTag extends BaseEntity {

    /** 实体ID */
    private Long entityId;

    /** 标签ID */
    private Long tagId;
}
