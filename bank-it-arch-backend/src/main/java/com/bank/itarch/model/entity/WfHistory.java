package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

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
