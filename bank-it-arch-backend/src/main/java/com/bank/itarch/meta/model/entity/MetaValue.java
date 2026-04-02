package com.bank.itarch.meta.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 字段值表
 * 存储实体的字段值，支持多态值存储（text/int/boolean/date/json）
 */
@Data
@TableName("meta_value")
public class MetaValue {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 实体ID */
    private Long entityId;

    /** 字段ID */
    private Long fieldId;

    /** 字段编码（冗余） */
    private String fieldCode;

    /** 存储模式（COLUMN/JSON/TAG） */
    private String storageMode;

    // ========== 多态值存储 ==========
    /** 文本值 */
    private String valueText;

    /** 整数值 */
    private Long valueInt;

    /** 小数值 */
    private BigDecimal valueDecimal;

    /** 布尔值 */
    private Boolean valueBoolean;

    /** 日期值 */
    private LocalDate valueDate;

    /** 日期时间值 */
    private LocalDateTime valueDatetime;

    /** JSON/对象值 */
    private String valueJson;

    /** 长文本值 */
    private String valueLongText;

    // ========== 版本管理 ==========
    /** 值版本 */
    private Integer version;

    /** 是否当前值 */
    private Boolean isCurrent;

    // ========== 审计 ==========
    /** 创建时间 */
    private LocalDateTime createTime;

    /** 创建人 */
    private String creator;
}
