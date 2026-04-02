package com.bank.itarch.meta.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bank.itarch.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 模型版本管理表
 * 记录模型的版本变更历史，支持发布、归档、回滚
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meta_model_version")
public class MetaModelVersion extends BaseEntity {

    /** 模型ID */
    private Long modelId;

    /** 版本号 */
    private Integer version;

    /** 版本名称（如: v1.0.0） */
    private String versionName;

    /** 状态（DRAFT/TESTING/PUBLISHED/ARCHIVED） */
    private String status;

    /** 变更摘要 */
    private String changeSummary;

    /** 变更内容（JSON格式） */
    private String diffContent;

    /** 发布时间 */
    private LocalDateTime publishedAt;

    /** 发布人 */
    private String publishedBy;

    /** 归档时间 */
    private LocalDateTime archivedAt;

    /** 归档人 */
    private String archivedBy;

    /** 回滚来源版本 */
    private Integer rollbackFromVersion;
}
