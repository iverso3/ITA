package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("arch_application")
public class ArchApplication extends BaseEntity {
    private String appCode;
    private String appName;
    private String appNameEn;
    private String appType;
    private String importanceLevel;
    private String lifecycle;
    private String businessDomain;
    private Long departmentId;
    private String departmentName;
    private Long teamId;
    private String teamName;
    private String pmName;
    private String pmEmail;
    private String techLead;
    private String techLeadEmail;
    private String description;
    private String businessDescription;
    private String deploymentType;
    private String accessType;
    private String slaLevel;
    private String version;
    private String goLiveDate;
    private String retireDate;
    private String status;
    private String remark;
}
