package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("arch_data_flow")
public class ArchDataFlow extends BaseEntity {
    private String flowCode;
    private String flowName;
    private Long sourceAppId;
    private String sourceAppName;
    private Long sourceEntityId;
    private String sourceEntityName;
    private Long targetAppId;
    private String targetAppName;
    private Long targetEntityId;
    private String targetEntityName;
    private String flowType;
    private String transferType;
    private String frequency;
    private Long dataVolume;
    private String description;
    private String remark;
}
