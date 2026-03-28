package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_instance")
public class WfInstance extends BaseEntity {
    private String instanceCode;
    private Long definitionId;
    private String definitionName;
    private String processType;
    private String businessType;
    private Long businessId;
    private String businessKey;
    private String title;
    private Long applicantId;
    private String applicantName;
    private Long applicantDeptId;
    private String applicantDeptName;
    private Long currentNodeId;
    private String currentNodeName;
    private String status;
    private String priority;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private String result;
    private String businessData;
    private String remark;
}
