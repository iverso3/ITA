package com.bank.itarch.meta.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 元数据操作日志表
 * 记录所有元模型相关的操作日志，包括变更前后值
 */
@Data
@TableName("meta_audit_log")
public class MetaAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作类型（CREATE/UPDATE/DELETE/PUBLISH/ARCHIVE/ROLLBACK） */
    private String auditType;

    /** 目标类型（MODEL/FIELD/RELATION/ENTITY/VALUE） */
    private String targetType;

    /** 目标ID */
    private Long targetId;

    /** 目标编码 */
    private String targetCode;

    /** 目标名称 */
    private String targetName;

    /** 变更前值 */
    private String beforeValue;

    /** 变更后值 */
    private String afterValue;

    /** 变更字段列表 */
    private String changeFields;

    /** 操作状态 */
    private String operateStatus;

    /** 错误信息 */
    private String errorMessage;

    /** 操作IP */
    private String operateIp;

    /** 操作位置 */
    private String operateLocation;

    /** 执行时长(ms) */
    private Integer executeTime;

    /** 操作人ID */
    private String operatorId;

    /** 操作人名称 */
    private String operatorName;

    /** 操作时间 */
    private LocalDateTime operateTime;
}
