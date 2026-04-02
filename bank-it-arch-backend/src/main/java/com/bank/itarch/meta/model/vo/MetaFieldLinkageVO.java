package com.bank.itarch.meta.model.vo;

import lombok.Data;

/**
 * 字段联动配置VO
 */
@Data
public class MetaFieldLinkageVO {

    /** 源字段编码 */
    private String sourceField;

    /** 源字段值 */
    private Object sourceValue;

    /** 目标字段编码 */
    private String targetField;

    /** 联动动作（HIDE/SHOW/REQUIRED/OPTIONS） */
    private String action;

    /** 动作参数 */
    private Object actionValue;
}
