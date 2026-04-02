# 应用信息表结构 (arch_application)

> 文档版本: v1.7
> 更新时间: 2026-03-30
> 数据库: bank_it_arch

## 表结构概览

| 序号 | 字段分组 | 字段数量 |
|------|----------|----------|
| 1 | 基本属性 | 25 |
| 2 | 安全等级 | 2 |
| 3 | 技术信息 - 基础实施 | 2 |
| 4 | 技术信息 - 安全基础 | 17 |
| 5 | 技术信息 - 其他信息 | 7 |
| 6 | 技术信息 - 信创情况 | 14 |
| 7 | 服务信息 | 5 |
| 8 | 运维信息 | 22 |
| **合计** | | **94** |

---

## 1. 基本属性

| 字段名 | 类型 | 说明 | 可选值/示例 |
|--------|------|------|-------------|
| app_code | varchar(32) | 应用编码（系统自动生成） | APP-00001 |
| app_name | varchar(128) | 应用名称 | IT架构管理应用 |
| app_name_en | varchar(256) | 英文全称/简称 | IT Architecture Management |
| app_type | varchar(32) | 应用类型 | 交易/渠道/管理/数据/基础设施 |
| importance_level | varchar(16) | 重要级别 | 核心/重要/一般/辅助 |
| system_layer | varchar(32) | 系统所属层 | 管理决策层/用户交互层/理财子公司/产品服务层/技术支撑层 |
| lifecycle | varchar(32) | 生命周期 | PLANNING/DEVELOPMENT/TESTING/PRODUCTION/DEPRECATED |
| business_domain | varchar(64) | 业务域 | |
| department_id | bigint | 主管部门ID | |
| department_name | varchar(128) | 主管部门名称 | 信息科技部/运维部/数据中心 |
| implementation_unit | varchar(128) | 实施单位 | 赞同科技股份有限公司 |
| implementation_division | varchar(128) | 实施处室 | |
| implementation_team | varchar(128) | 实施项目组 | |
| parent_app_id | bigint | 上级应用ID（所属系统） | |
| team_id | bigint | 团队ID | |
| team_name | varchar(64) | 团队名称 | |
| pm_name | varchar(64) | 产品经理 | |
| pm_email | varchar(128) | 产品经理邮箱 | |
| tech_lead | varchar(64) | 技术负责人 | |
| tech_lead_email | varchar(128) | 技术负责人邮箱 | |
| deployment_type | varchar(32) | 部署方式 | 单机/集群/分布式/云原生 |
| access_type | varchar(32) | 访问方式 | 浏览器/客户端/APP/内网/外网/内外网 |
| sla_level | varchar(32) | SLA级别 | |
| version | varchar(32) | 当前版本 | v1.0.0 |
| go_live_date | varchar(32) | 投产时间 | 2026-06-01 |
| retire_date | varchar(32) | 下线日期 | |
| status | varchar(32) | 应用状态 | PLANNING/DEVELOPMENT/TESTING/PRODUCTION/DEPRECATED |
| description | text | 应用简介 | |
| business_description | text | 业务描述 | |
| main_business_domain | varchar(128) | 主业务功能领域 | |
| secondary_business_domain | varchar(128) | 辅业务功能领域 | |
| service_object | varchar(128) | 服务对象 | |
| remark | text | 备注 | |

### 1.1 分类标识字段

| 字段名 | 类型 | 说明 | 可选值 |
|--------|------|------|--------|
| is_internet_app | tinyint | 是否互联网应用 | 0-否 / 1-是 |
| is_payment_app | tinyint | 是否支付应用 | 0-否 / 1-是 |
| is_online_banking_app | tinyint | 是否网上银行应用 | 0-否 / 1-是 |
| is_bill_app | tinyint | 是否票据应用 | 0-否 / 1-是 |
| is_electronic_banking_app | tinyint | 是否电子银行应用 | 0-否 / 1-是 |
| is_mobile_app | tinyint | 是否移动应用 | 0-否 / 1-是 |
| is_internet_finance_app | tinyint | 是否互联网金融应用 | 0-否 / 1-是 |

---

## 2. 安全等级

| 字段名 | 类型 | 说明 | 可选值 |
|--------|------|------|--------|
| system_protection_level | varchar(32) | 所属系统等级保护安全等级 | 一级/二级/三级/四级 |
| protection_level | varchar(32) | 等级保护安全等级 | 第一级/第二级/第三级/第四级/第五级 |

---

## 3. 技术信息 - 基础实施信息

| 字段名 | 类型 | 说明 | 可选值 |
|--------|------|------|--------|
| implementation_method | varchar(32) | 实施方式 | 外购产品/定制化产品 |
| implementation_type | varchar(32) | 实施类型 | 内部管理类/业务处理类/渠道类 |

---

## 4. 技术信息 - 安全基础信息

| 字段名 | 类型 | 说明 | 可选值/示例 |
|--------|------|------|-------------|
| login_user_field | varchar(64) | 登录用户字段名称 | account |
| login_password_field | varchar(64) | 登录密码字段名称 | password |
| is_deployed_dmz | tinyint | 是否部署在DMZ区 | 0-否 / 1-是 |
| has_upload_function | tinyint | 是否存在上传功能 | 0-否 / 1-是 |
| pwd_error_freeze_mechanism | varchar(256) | 账号密码错误冻结机制 | 与集中身份保持一致 |
| has_outbound_request | tinyint | 是否存在主动外发请求业务场景 | 0-否 / 1-是 |
| outbound_request_desc | varchar(512) | 外发业务场景描述 | 向集中身份平台同步获取更新用户信息 |
| upload_function_desc | varchar(512) | 上传的功能描述和URL | 各资产库可上传附件 |
| upload_file_path | varchar(256) | 文件上传路径 | /ita/docs/upload/ |
| upload_path_executable | varchar(16) | 文件上传路径执行权限 | 可执行/不可执行 |
| upload_file_types | varchar(256) | 上传文件类型 | 暂无限制 |
| has_download_function | tinyint | 是否存在下载功能 | 0-否 / 1-是 |
| download_function_desc | varchar(512) | 下载的功能描述和URL | |
| download_file_types | varchar(256) | 下载文件类型 | 暂无限制 |
| is_app_application | tinyint | 是否APP应用 | 0-否 / 1-是 |
| open_source_info | varchar(512) | 开源有关信息 | shiro, log4j, quartz, activiti |
| access_url | varchar(256) | 访问主页 | http://22.0.200.34:8080/ita/#/login |

---

## 5. 技术信息 - 其他信息

| 字段名 | 类型 | 说明 | 可选值/示例 |
|--------|------|------|-------------|
| domain_name | varchar(128) | 域名 | http://in-ita.cgbchina.com.cn/ |
| network_mode | varchar(32) | 网络结构模式 | B/S / C/S / 混合 |
| vendor_info | varchar(512) | 实施商信息（多个用逗号分隔） | 赞同科技股份有限公司,神州数码信息科技有限公司 |
| auth_method | varchar(128) | 认证方式 | 复用集中身份管理平台应用用户认证方式 |
| has_third_party_integration | tinyint | 是否与外部第三方系统对接 | 0-否 / 1-是 |
| third_party_integration_desc | varchar(512) | 与外部第三方系统对接补充说明 | 不涉及 |
| is_internet_line | tinyint | 互联网线路类型 | 0-否 / 1-是 |

---

## 6. 技术信息 - 信创情况

| 字段名 | 类型 | 说明 | 可选值/示例 |
|--------|------|------|-------------|
| xc_cloud_status | varchar(32) | 应用是否上信创容器云 | 已上云/未上云 |
| server_xc_desc | varchar(256) | 服务器信创情况说明 | 海光加麒麟（海光CPU+麒麟操作系统） |
| server_xc_status | varchar(32) | 服务器信创情况 | 全栈信创/部分信创/非信创 |
| has_third_party_product | tinyint | 是否使用第三方产品 | 0-否 / 1-是 |
| third_party_is_xc | tinyint | 第三方产品是否信创产品 | 0-否 / 1-是 |
| is_full_xc | tinyint | 是否全栈信创 | 0-否 / 1-是 |
| db_server_xc_status | varchar(32) | 数据库服务器信创情况 | 全栈信创 |
| db_server_os | varchar(32) | 数据库服务器操作系统 | 麒麟/统信/其他 |
| database_xc_status | varchar(32) | 数据库信创情况 | 全栈信创 |
| database_xc_desc | varchar(256) | 数据库信创情况说明 | 使用GDB数据库 |
| partial_xc_desc | varchar(512) | 非全栈信创情况 | |
| xc_classification | varchar(32) | XC分类（信创分类） | 一般/重要/核心 |
| third_party_xc_desc | varchar(256) | 第三方产品信创情况说明 | 不涉及第三方产品 |
| xc_overall_desc | varchar(1024) | 信创整体情况说明 | 应用服务器已上自研信创云，数据库服务器和数据库均为信创产品 |

---

## 7. 服务信息

| 字段名 | 类型 | 说明 | 可选值/示例 |
|--------|------|------|-------------|
| customer_type | varchar(32) | 客户类型 | 行内客户/外部客户/混合 |
| service_time_type | varchar(32) | 服务时间类型 | 7*24 / 5*8 / 定制 |
| service_window_desc | varchar(256) | 服务窗口补充说明 | |
| internal_user_scope | varchar(128) | 内部用户范围 | 总行 |
| usage_scope_desc | varchar(512) | 使用范围补充说明 | 主要作为科技内部应用使用 |

---

## 8. 运维信息

### 8.1 容灾配置

| 字段名 | 类型 | 说明 | 可选值/示例 |
|--------|------|------|-------------|
| city_rpo | varchar(32) | 同城RPO | 不涉及/分钟级 |
| city_rto | varchar(32) | 同城RTO | 不涉及/分钟级 |
| city_active_type | varchar(32) | 同城双活类型 | 不涉及 |
| has_city_environment | tinyint | 是否具备同城环境 | 0-否 / 1-是 |
| remote_rpo | varchar(32) | 异地RPO | 不涉及 |
| remote_rto | varchar(32) | 异地RTO | 不涉及 |
| remote_active_type | varchar(32) | 异地双活类型 | 不涉及 |
| has_dr_environment | tinyint | 是否具备灾备环境 | 0-否 / 1-是 |
| dr_level | varchar(16) | 灾备等级 | A类/B类/C类/D类 |
| dr_recovery_level | varchar(16) | 灾备恢复能力等级 | 1级/2级/3级/4级 |

### 8.2 运维配置

| 字段名 | 类型 | 说明 | 可选值/示例 |
|--------|------|------|-------------|
| ops_level | varchar(16) | 运维等级 | A类/B类/C类/D类 |
| old_ops_level | varchar(16) | 旧运维等级 | |
| ops_unit | varchar(128) | 运维单位 | 数据中心 |
| remote_access_class | varchar(16) | 远程访问权限分类 | A类/B类/C类 |
| is_change_automation | tinyint | 是否变更自动化 | 0-否 / 1-是 |
| change_deploy_time | varchar(64) | 变更投产时点 | 18:00-20:00 |
| change_deploy_time_desc | varchar(256) | 变更投产时点补充说明 | |
| main_business_hours | varchar(64) | 主要业务时段 | 08:30-18:00 |
| data_asset_approval_dept | varchar(256) | 电子数据资产提取审批部门 | 研发中心、信息科技部 |
| is_containerized | tinyint | 是否容器化部署 | 0-否 / 1-是 |

### 8.3 部署配置

| 字段名 | 类型 | 说明 | 可选值/示例 |
|--------|------|------|-------------|
| deployment_environment | varchar(64) | 部署环境 | 生产环境/测试环境/开发环境/准生产环境 |
| deployment_location | varchar(128) | 部署地点 | 南海生产机房 |
| deployment_location_desc | varchar(512) | 部署地点补充说明 | 部署在ECC，由数据中心管理 |

---

## 字段命名对照表

| Java字段名(驼峰) | 数据库列名(下划线) | 说明 |
|------------------|-------------------|------|
| appCode | app_code | 应用编码 |
| appName | app_name | 应用名称 |
| appNameEn | app_name_en | 英文名称 |
| appType | app_type | 应用类型 |
| importanceLevel | importance_level | 重要级别 |
| systemLayer | system_layer | 系统所属层 |
| lifecycle | lifecycle | 生命周期 |
| businessDomain | business_domain | 业务域 |
| departmentId | department_id | 部门ID |
| departmentName | department_name | 部门名称 |
| implementationDivision | implementation_division | 实施处室 |
| implementationUnit | implementation_unit | 实施单位 |
| implementationTeam | implementation_team | 实施项目组 |
| parentAppId | parent_app_id | 上级应用ID |
| pmName | pm_name | 产品经理 |
| pmEmail | pm_email | 产品经理邮箱 |
| techLead | tech_lead | 技术负责人 |
| techLeadEmail | tech_lead_email | 技术负责人邮箱 |
| deploymentType | deployment_type | 部署方式 |
| accessType | access_type | 访问方式 |
| slaLevel | sla_level | SLA级别 |
| goLiveDate | go_live_date | 投产时间 |
| retireDate | retire_date | 下线日期 |
| mainBusinessDomain | main_business_domain | 主业务功能领域 |
| secondaryBusinessDomain | secondary_business_domain | 辅业务功能领域 |
| serviceObject | service_object | 服务对象 |
| isInternetApp | is_internet_app | 是否互联网应用 |
| isPaymentApp | is_payment_app | 是否支付应用 |
| isOnlineBankingApp | is_online_banking_app | 是否网上银行应用 |
| isBillApp | is_bill_app | 是否票据应用 |
| isElectronicBankingApp | is_electronic_banking_app | 是否电子银行应用 |
| isMobileApp | is_mobile_app | 是否移动应用 |
| isInternetFinanceApp | is_internet_finance_app | 是否互联网金融应用 |
| implementationMethod | implementation_method | 实施方式 |
| implementationType | implementation_type | 实施类型 |
| loginUserField | login_user_field | 登录用户字段名称 |
| loginPasswordField | login_password_field | 登录密码字段名称 |
| isDeployedDmz | is_deployed_dmz | 是否部署在DMZ区 |
| hasUploadFunction | has_upload_function | 是否存在上传功能 |
| pwdErrorFreezeMechanism | pwd_error_freeze_mechanism | 账号密码错误冻结机制 |
| hasOutboundRequest | has_outbound_request | 是否存在主动外发请求 |
| outboundRequestDesc | outbound_request_desc | 外发业务场景描述 |
| uploadFunctionDesc | upload_function_desc | 上传功能描述和URL |
| uploadFilePath | upload_file_path | 文件上传路径 |
| uploadPathExecutable | upload_path_executable | 上传路径执行权限 |
| uploadFileTypes | upload_file_types | 上传文件类型 |
| hasDownloadFunction | has_download_function | 是否存在下载功能 |
| downloadFunctionDesc | download_function_desc | 下载功能描述和URL |
| downloadFileTypes | download_file_types | 下载文件类型 |
| isAppApplication | is_app_application | 是否APP应用 |
| openSourceInfo | open_source_info | 开源有关信息 |
| accessUrl | access_url | 访问主页 |
| domainName | domain_name | 域名 |
| networkMode | network_mode | 网络结构模式 |
| vendorInfo | vendor_info | 实施商信息 |
| authMethod | auth_method | 认证方式 |
| hasThirdPartyIntegration | has_third_party_integration | 是否与外部第三方系统对接 |
| thirdPartyIntegrationDesc | third_party_integration_desc | 第三方对接补充说明 |
| isInternetLine | is_internet_line | 互联网线路类型 |
| xcCloudStatus | xc_cloud_status | 是否上信创容器云 |
| serverXcDesc | server_xc_desc | 服务器信创情况说明 |
| serverXcStatus | server_xc_status | 服务器信创情况 |
| hasThirdPartyProduct | has_third_party_product | 是否使用第三方产品 |
| thirdPartyIsXc | third_party_is_xc | 第三方产品是否信创 |
| isFullXc | is_full_xc | 是否全栈信创 |
| dbServerXcStatus | db_server_xc_status | 数据库服务器信创情况 |
| dbServerOs | db_server_os | 数据库服务器操作系统 |
| databaseXcStatus | database_xc_status | 数据库信创情况 |
| databaseXcDesc | database_xc_desc | 数据库信创情况说明 |
| partialXcDesc | partial_xc_desc | 非全栈信创情况 |
| xcClassification | xc_classification | XC分类 |
| thirdPartyXcDesc | third_party_xc_desc | 第三方产品信创情况说明 |
| xcOverallDesc | xc_overall_desc | 信创整体情况说明 |
| systemProtectionLevel | system_protection_level | 所属系统等级保护安全等级 |
| protectionLevel | protection_level | 等级保护安全等级 |
| customerType | customer_type | 客户类型 |
| serviceTimeType | service_time_type | 服务时间类型 |
| serviceWindowDesc | service_window_desc | 服务窗口补充说明 |
| internalUserScope | internal_user_scope | 内部用户范围 |
| usageScopeDesc | usage_scope_desc | 使用范围补充说明 |
| cityRpo | city_rpo | 同城RPO |
| cityRto | city_rto | 同城RTO |
| cityActiveType | city_active_type | 同城双活类型 |
| hasCityEnvironment | has_city_environment | 是否具备同城环境 |
| remoteRpo | remote_rpo | 异地RPO |
| remoteRto | remote_rto | 异地RTO |
| remoteActiveType | remote_active_type | 异地双活类型 |
| hasDrEnvironment | has_dr_environment | 是否具备灾备环境 |
| opsLevel | ops_level | 运维等级 |
| oldOpsLevel | old_ops_level | 旧运维等级 |
| opsUnit | ops_unit | 运维单位 |
| remoteAccessClass | remote_access_class | 远程访问权限分类 |
| isChangeAutomation | is_change_automation | 是否变更自动化 |
| changeDeployTime | change_deploy_time | 变更投产时点 |
| changeDeployTimeDesc | change_deploy_time_desc | 变更投产时点补充说明 |
| mainBusinessHours | main_business_hours | 主要业务时段 |
| dataAssetApprovalDept | data_asset_approval_dept | 电子数据资产提取审批部门 |
| isContainerized | is_containerized | 是否容器化部署 |
| deploymentEnvironment | deployment_environment | 部署环境 |
| deploymentLocation | deployment_location | 部署地点 |
| deploymentLocationDesc | deployment_location_desc | 部署地点补充说明 |
| drLevel | dr_level | 灾备等级 |
| drRecoveryLevel | dr_recovery_level | 灾备恢复能力等级 |

---

## 相关文件

- 实体类: `bank-it-arch-backend/src/main/java/com/bank/itarch/model/entity/ArchApplication.java`
- 控制器: `bank-it-arch-backend/src/main/java/com/bank/itarch/controller/ArchApplicationController.java`
- 服务类: `bank-it-arch-backend/src/main/java/com/bank/itarch/service/ArchApplicationService.java`
- 前端列表: `bank-it-arch-frontend/src/views/arch/Application.vue`
- 前端详情: `bank-it-arch-frontend/src/views/arch/ApplicationDetail.vue`
- 数据库迁移:
  - `database/migration_v1.4_application_fields.sql`
  - `database/migration_v1.6_tech_and_xc_fields.sql`
  - `database/migration_v1.7_security_service_ops_fields.sql`
