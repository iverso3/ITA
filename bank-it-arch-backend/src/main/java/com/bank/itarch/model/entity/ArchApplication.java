package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
    private String systemLayer;
    private String lifecycle;
    private String businessDomain;
    private Long departmentId;
    private String departmentName;
    private String implementationDivision;
    private Long teamId;
    private String teamName;
    private Long parentAppId;
    private String implementationUnit;
    private String implementationTeam;
    private String pmName;
    private String pmEmail;
    private String techLead;
    private String techLeadEmail;

    // 互联网应用标记
    private Integer isInternetApp;
    private Integer isPaymentApp;
    private Integer isOnlineBankingApp;
    private Integer isBillApp;
    private Integer isElectronicBankingApp;
    private Integer isMobileApp;
    private Integer isInternetFinanceApp;

    // 部署信息
    private String deploymentType;
    private String accessType;
    private String slaLevel;
    private String version;
    private String implementationMethod;
    private String implementationType;

    // 扩展属性JSON（存储原54个扩展字段）
    @TableField("ext_attrs")
    private String extAttrs;

    // 核心日期和状态
    private String goLiveDate;
    private String retireDate;
    private String status;
}
