package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wf_task")
public class WfTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long instanceId;
    private Long nodeId;
    private String taskCode;
    private String taskName;
    private String taskType;
    private Long assigneeId;
    private String assigneeName;
    private String status;
    private String approveType;
    private Integer approvedCount;
    private Integer requiredCount;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    private LocalDateTime completeTime;
    private Long delegateFrom;
    private Long delegateTo;
    private String opinion;
    private String remark;
}
