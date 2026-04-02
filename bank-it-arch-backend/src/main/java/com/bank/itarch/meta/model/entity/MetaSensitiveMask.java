package com.bank.itarch.meta.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bank.itarch.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 敏感数据脱敏规则表
 * 定义字段的脱敏规则和脱敏模式
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meta_sensitive_mask")
public class MetaSensitiveMask extends BaseEntity {

    /** 规则编码 */
    private String maskCode;

    /** 规则名称 */
    private String maskName;

    /** 字段类型 */
    private String fieldType;

    /** 脱敏模式（PHONE/ID_CARD/EMAIL/BANK_CARD/CUSTOM） */
    private String maskPattern;

    /** 自定义正则 */
    private String customPattern;

    /** 替换格式（如: $1****$2） */
    private String replaceFormat;

    /** 是否启用 */
    private Boolean isActive;

    /** 描述 */
    private String description;
}
