package com.bank.itarch.meta.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bank.itarch.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 扩展字典表
 * 支持动态字典项管理，用于字段的枚举值配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meta_data_dict_ext")
public class MetaDataDictExt extends BaseEntity {

    /** 字典编码 */
    private String dictCode;

    /** 字典名称 */
    private String dictName;

    /** 字典项编码 */
    private String itemCode;

    /** 字典项名称 */
    private String itemName;

    /** 字典项值 */
    private String itemValue;

    /** 父级编码（树形字典） */
    private String parentItemCode;

    /** 树形路径 */
    private String treePath;

    /** 排序 */
    private Integer sortOrder;

    /** 是否启用 */
    private Boolean isActive;

    /** 描述 */
    private String description;
}
