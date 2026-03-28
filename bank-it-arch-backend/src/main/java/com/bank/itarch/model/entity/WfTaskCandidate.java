package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wf_task_candidate")
public class WfTaskCandidate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long candidateId;
    private String candidateName;
    private String candidateType;
    private Integer isApproved;
    private LocalDateTime approveTime;
    private LocalDateTime createTime;
}
