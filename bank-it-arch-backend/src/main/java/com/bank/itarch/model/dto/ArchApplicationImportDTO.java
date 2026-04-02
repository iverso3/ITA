package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ArchApplicationImportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // Basic Info
    private String appCode;                     // 应用编号（必填）
    private String appName;                     // 应用名称（必填）
    private String appNameEn;                   // 英文全称
    private String appType;                     // 应用类型
    private String importanceLevel;             // 重要级别
    private String systemLayer;                 // 系统所属层
    private String lifecycle;                   // 生命周期
    private String status;                      // 应用状态
    private String departmentName;              // 应用主管部门
    private String implementationUnit;          // 实施单位
    private String implementationDivision;      // 实施处室
    private String implementationTeam;          // 实施项目组
    private String parentAppName;              // 所属系统名称（用于查询 parentAppId）
    private String goLiveDate;                 // 投产时间
    private String retireDate;                  // 下线日期
    private String mainBusinessDomain;          // 主业务功能领域
    private String secondaryBusinessDomain;     // 辅业务功能领域
    private String description;                 // 应用简介
    private String remark;                      // 备注

    // Classification
    private Integer isInternetApp;             // 是否互联网应用
    private Integer isPaymentApp;              // 是否支付应用
    private Integer isElectronicBankingApp;    // 是否电子银行应用
    private Integer isOnlineBankingApp;         // 是否网上银行应用
    private Integer isMobileApp;               // 是否移动应用
    private Integer isBillApp;                  // 是否票据应用
    private Integer isInternetFinanceApp;       // 是否互联网金融应用
    private String serviceObject;               // 服务对象

    // Security
    private String systemProtectionLevel;       // 所属系统等级保护安全等级
    private String protectionLevel;             // 等级保护安全等级

    // Tech
    private String implementationMethod;       // 实施方式
    private String implementationType;          // 实施类型
    private String loginUserField;              // 登录用户字段名称
    private String loginPasswordField;          // 登录密码字段名称
    private Integer isDeployedDmz;              // 是否部署在DMZ区
    private Integer hasUploadFunction;          // 是否存在上传功能
    private String pwdErrorFreezeMechanism;     // 账号密码错误冻结机制
    private Integer hasOutboundRequest;         // 是否存在主动外发请求
    private String outboundRequestDesc;         // 外发业务场景描述
    private String uploadFunctionDesc;          // 上传的功能描述和URL
    private String uploadFilePath;              // 文件上传路径
    private String uploadPathExecutable;        // 执行权限
    private String uploadFileTypes;             // 上传文件类型
    private Integer hasDownloadFunction;        // 是否存在下载功能
    private String downloadFunctionDesc;        // 下载的功能描述和URL
    private String downloadFileTypes;           // 下载文件类型
    private Integer isAppApplication;           // 是否APP应用
    private String openSourceInfo;              // 开源有关信息
    private String accessUrl;                   // 访问主页
    private String domainName;                  // 域名
    private String networkMode;                  // 网络结构模式
    private String accessType;                  // 访问方式
    private String vendorInfo;                  // 实施商信息
    private String authMethod;                  // 认证方式
    private Integer hasThirdPartyIntegration;   // 是否与外部第三方系统对接
    private String thirdPartyIntegrationDesc;   // 与外部第三方系统对接补充说明
    private Integer isInternetLine;             // 互联网线路类型
    private String xcCloudStatus;               // 应用是否上信创容器云
    private String serverXcStatus;               // 服务器信创情况
    private Integer isFullXc;                   // 是否全栈信创
    private Integer hasThirdPartyProduct;       // 是否使用第三方产品
    private Integer thirdPartyIsXc;             // 第三方产品是否信创
    private String xcClassification;             // XC分类
    private String dbServerXcStatus;            // 数据库服务器信创情况
    private String dbServerOs;                  // 数据库服务器操作系统
    private String databaseXcStatus;            // 数据库信创情况
    private String serverXcDesc;                // 服务器信创情况说明
    private String databaseXcDesc;              // 数据库信创情况说明
    private String partialXcDesc;               // 非全栈信创情况
    private String thirdPartyXcDesc;           // 第三方产品信创情况说明
    private String xcOverallDesc;               // 信创整体情况说明

    // Service
    private String customerType;                // 客户类型
    private String serviceTimeType;             // 服务时间类型
    private String internalUserScope;           // 内部用户范围
    private String serviceWindowDesc;           // 服务窗口补充说明
    private String usageScopeDesc;              // 使用范围补充说明

    // Ops
    private String cityRpo;                     // 同城RPO
    private String cityRto;                     // 同城RTO
    private String cityActiveType;              // 同城双活类型
    private Integer hasCityEnvironment;         // 是否具备同城环境
    private String remoteRpo;                   // 异地RPO
    private String remoteRto;                   // 异地RTO
    private String remoteActiveType;            // 异地双活类型
    private Integer hasDrEnvironment;           // 是否具备灾备环境
    private String opsLevel;                    // 运维等级
    private String oldOpsLevel;                 // 旧运维等级
    private String opsUnit;                     // 运维单位
    private String remoteAccessClass;           // 远程访问权限分类
    private Integer isChangeAutomation;         // 是否变更自动化
    private String changeDeployTime;            // 变更投产时点
    private String changeDeployTimeDesc;         // 变更投产时点补充说明
    private String mainBusinessHours;            // 主要业务时段
    private String dataAssetApprovalDept;        // 电子数据资产提取审批部门
    private Integer isContainerized;             // 是否容器化部署
    private String deploymentEnvironment;        // 部署环境
    private String deploymentLocation;           // 部署地点
    private String deploymentLocationDesc;       // 部署地点补充说明
    private String drLevel;                     // 灾备等级
    private String drRecoveryLevel;             // 灾备恢复能力等级
}