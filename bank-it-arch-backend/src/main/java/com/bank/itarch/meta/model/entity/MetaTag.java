package com.bank.itarch.meta.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bank.itarch.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签定义表
 * 支持灵活的数据打标，如重要程度、业务分类等
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meta_tag")
public class MetaTag extends BaseEntity {

    /** 标签编码 */
    private String tagCode;

    /** 标签名称 */
    private String tagName;

    /** 标签类型（CUSTOM/SYSTEM/AUTO） */
    private String tagType;

    /** 颜色 */
    private String color;

    /** 描述 */
    private String description;

    /** 是否启用 */
    private Boolean isActive;
}
