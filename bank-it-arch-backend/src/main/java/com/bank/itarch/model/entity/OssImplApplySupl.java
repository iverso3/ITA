package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("oss_impl_apply_supl")
public class OssImplApplySupl implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 申请单号 */
    private String implApplyNo;

    /** 评测信息列表json */
    private String evalInfoListJson;

    /** 评测结果列表json */
    private String evalResultListJson;

    /** 评测得分列表json */
    private String evalScoreListJson;

    /** 评测总结列表json */
    private String evalSummListJson;

    /** 评测附件json */
    private String evalAtchListJson;

    /** 介质前置仓库地址json */
    private String mediaPreWhsUrl;

    /** 流程实例ID */
    private Long procInstId;

    /** 同步时间 */
    private LocalDateTime syncDatetime;

    /** 创建方式: 0-本系统创建, 1-异地同步 */
    private Integer createMode;

    /** 创建者 */
    private String createUserId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDatetime;

    /** 更新者 */
    private String updateUserId;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateDatetime;

    /** 逻辑状态: 0-正常, 1-已删除 */
    @TableLogic
    @TableField(select = false)
    private Integer logicStatus;
}
