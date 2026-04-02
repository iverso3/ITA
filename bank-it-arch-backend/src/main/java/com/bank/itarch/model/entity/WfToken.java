package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 流程令牌
 */
@Data
@TableName("wf_token")
public class WfToken {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long instanceId;
    private Long currentNodeId;
    private String tokenStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(select = false)
    private Integer deleted;
}
