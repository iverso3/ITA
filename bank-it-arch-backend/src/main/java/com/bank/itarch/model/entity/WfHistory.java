package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批历史
 */
@Data
@TableName("wf_history")
public class WfHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long instanceId;
    private Long taskId;
    private Long nodeId;
    private String nodeName;
    private Long operatorId;
    private String operatorName;
    private String operatorDeptName;
    private String action;
    private String opinion;
    private LocalDateTime createTime;
    private Long duration;
}
