package com.bank.itarch.meta.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bank.itarch.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 发布记录表
 * 记录模型的发布、归档、回滚操作
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meta_publish_record")
public class MetaPublishRecord extends BaseEntity {

    /** 记录编码 */
    private String recordCode;

    /** 模型ID */
    private Long modelId;

    /** 源版本（回滚时使用） */
    private Integer versionFrom;

    /** 目标版本 */
    private Integer versionTo;

    /** 操作类型（PUBLISH/ARCHIVE/ROLLBACK） */
    private String actionType;

    /** 状态（PROCESSING/SUCCESS/FAILED） */
    private String status;

    /** 执行配置 */
    private String executeConfig;

    /** 执行结果 */
    private String executeResult;

    /** 错误信息 */
    private String errorMessage;

    /** 操作人 */
    private String operator;

    /** 操作时间 */
    private LocalDateTime operateTime;
}
