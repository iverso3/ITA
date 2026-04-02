package com.bank.itarch.meta.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bank.itarch.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 关系实例表
 * 存储实体之间的关系绑定
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meta_relation")
public class MetaRelation extends BaseEntity {

    /** 关系定义ID */
    private Long relId;

    /** 关系编码（冗余） */
    private String relCode;

    /** 关系类型（N:1/1:N/N:N/1:1） */
    private String relType;

    /** 源实体ID */
    private Long sourceEntityId;

    /** 源实体编码（冗余） */
    private String sourceEntityCode;

    /** 目标实体ID */
    private Long targetEntityId;

    /** 目标实体编码（冗余） */
    private String targetEntityCode;

    /** 关系级别（NORMAL/IMPORTANT/CRITICAL） */
    private String relationLevel;

    /** 关系描述 */
    private String description;

    /** 关系开始日期 */
    private LocalDate startDate;

    /** 关系结束日期 */
    private LocalDate endDate;

    /** 是否有效 */
    private Boolean isActive;
}
