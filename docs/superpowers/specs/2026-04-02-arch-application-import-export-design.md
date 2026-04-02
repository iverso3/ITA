# 应用导入导出功能设计方案

## 1. 概述

### 1.1 背景与目标

银行IT架构管理平台的应用架构模块（`ArchApplication`）需要新增 Excel 导入导出功能：

- **导入**：生成含测试数据的 Excel 导入模板，支持导入应用详情所有 Tab 页字段
- **导出**：支持用户配置导出字段，默认导出"基本信息" Tab 字段

### 1.2 范围

| 功能 | 说明 |
|------|------|
| 模板生成 | 后端动态生成含测试数据的 xlsx 文件，保存至 `template/` 目录 |
| 导入 | 后端解析 Excel，upsert 主应用数据（按 appCode 判断） |
| 导出 | 前端 XLSX 根据用户选择的字段生成 Excel（保持现有方式） |

### 1.3 非范围

- 服务列表（ArchService）数据不随应用导入，属独立模块
- 导出字段配置为会话级，不持久化

---

## 2. 技术方案

### 2.1 技术选型

- **Excel 处理**：Apache POI 5.x（与项目现有 OssUseStandingBookMainService 一致）
- **模板引擎**：直接通过 POI 操作 Workbook 生成
- **前端导出**：保持现有 XLSX 库实现

### 2.2 项目现有模式参考

`OssUseStandingBookMainService.importExcel()` 方法已实现完整的 Excel 解析和导入逻辑，本设计参照其模式。

---

## 3. API 设计

### 3.1 模板下载

```
GET /api/v1/arch/applications/import/template
```

**响应**：文件流 `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

**生成规则**：
- 文件名：`application_import_template_{yyyyMMddHHmmss}.xlsx`
- 存放路径：`template/application_import_template_{yyyyMMddHHmmss}.xlsx`
- Sheet 名：`应用导入模板`
- 首行列名，第二行测试数据

### 3.2 导入

```
POST /api/v1/arch/applications/import
Content-Type: multipart/form-data
```

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | Excel 文件 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "success": 8,
    "updated": 2,
    "created": 6,
    "errors": [
      { "row": 3, "message": "appCode 重复" }
    ]
  }
}
```

**导入校验规则**：
- `appCode` 必填，唯一标识
- `appName` 必填
- `status` 可选，默认为 PLANNING
- `systemLayer` 可选

**Upsert 逻辑**：
- 按 `appCode` 查询主应用记录
- 存在则更新全部字段
- 不存在则新增

### 3.3 导出（配置导出）

现有前端导出方式不变，通过 `handleDoExport` 调用 `archAppApi.list()` 获取数据，前端 XLSX 根据用户选择字段过滤并生成文件。

**默认导出字段（基本信息Tab）**：
```
appCode, appName, appNameEn, status, departmentName,
implementationUnit, implementationDivision, implementationTeam,
importanceLevel, systemLayer
```

---

## 4. 导入字段定义

按应用详情 Tab 页分组：

### 4.1 基本信息（Basic Info）

| 字段名 | 中文名 | 必填 | 类型 |
|--------|--------|------|------|
| appCode | 应用编号 | 是 | String |
| appName | 应用名称 | 是 | String |
| appNameEn | 英文全称 | 否 | String |
| appType | 应用类型 | 否 | String |
| importanceLevel | 重要级别 | 否 | String |
| systemLayer | 系统所属层 | 否 | String |
| lifecycle | 生命周期 | 否 | String |
| status | 应用状态 | 否 | String |
| departmentName | 应用主管部门 | 否 | String |
| implementationUnit | 实施单位 | 否 | String |
| implementationDivision | 实施处室 | 否 | String |
| implementationTeam | 实施项目组 | 否 | String |
| parentAppName | 所属系统名称 | 否 | String |
| goLiveDate | 投产时间 | 否 | String |
| retireDate | 下线日期 | 否 | String |
| mainBusinessDomain | 主业务功能领域 | 否 | String |
| secondaryBusinessDomain | 辅业务功能领域 | 否 | String |
| description | 应用简介 | 否 | String |
| remark | 备注 | 否 | String |

### 4.2 分类信息（Classification）

| 字段名 | 中文名 | 必填 | 类型 |
|--------|--------|------|------|
| isInternetApp | 是否互联网应用 | 否 | Integer(0/1) |
| isPaymentApp | 是否支付应用 | 否 | Integer(0/1) |
| isElectronicBankingApp | 是否电子银行应用 | 否 | Integer(0/1) |
| isOnlineBankingApp | 是否网上银行应用 | 否 | Integer(0/1) |
| isMobileApp | 是否移动应用 | 否 | Integer(0/1) |
| isBillApp | 是否票据应用 | 否 | Integer(0/1) |
| isInternetFinanceApp | 是否互联网金融应用 | 否 | Integer(0/1) |
| serviceObject | 服务对象 | 否 | String |

### 4.3 安全等级（Security）

| 字段名 | 中文名 | 必填 | 类型 |
|--------|--------|------|------|
| systemProtectionLevel | 所属系统等级保护安全等级 | 否 | String |
| protectionLevel | 等级保护安全等级 | 否 | String |

### 4.4 技术信息（Tech）

| 字段名 | 中文名 | 必填 | 类型 |
|--------|--------|------|------|
| implementationMethod | 实施方式 | 否 | String |
| implementationType | 实施类型 | 否 | String |
| loginUserField | 登录用户字段名称 | 否 | String |
| loginPasswordField | 登录密码字段名称 | 否 | String |
| isDeployedDmz | 是否部署在DMZ区 | 否 | Integer(0/1) |
| hasUploadFunction | 是否存在上传功能 | 否 | Integer(0/1) |
| pwdErrorFreezeMechanism | 账号密码错误冻结机制 | 否 | String |
| hasOutboundRequest | 是否存在主动外发请求 | 否 | Integer(0/1) |
| outboundRequestDesc | 外发业务场景描述 | 否 | String |
| uploadFunctionDesc | 上传的功能描述和URL | 否 | String |
| uploadFilePath | 文件上传路径 | 否 | String |
| uploadPathExecutable | 执行权限 | 否 | String |
| uploadFileTypes | 上传文件类型 | 否 | String |
| hasDownloadFunction | 是否存在下载功能 | 否 | Integer(0/1) |
| downloadFunctionDesc | 下载的功能描述和URL | 否 | String |
| downloadFileTypes | 下载文件类型 | 否 | String |
| isAppApplication | 是否APP应用 | 否 | Integer(0/1) |
| openSourceInfo | 开源有关信息 | 否 | String |
| accessUrl | 访问主页 | 否 | String |
| domainName | 域名 | 否 | String |
| networkMode | 网络结构模式 | 否 | String |
| accessType | 访问方式 | 否 | String |
| vendorInfo | 实施商信息 | 否 | String |
| authMethod | 认证方式 | 否 | String |
| hasThirdPartyIntegration | 是否与外部第三方系统对接 | 否 | Integer(0/1) |
| thirdPartyIntegrationDesc | 与外部第三方系统对接补充说明 | 否 | String |
| isInternetLine | 互联网线路类型 | 否 | Integer(0/1) |
| xcCloudStatus | 应用是否上信创容器云 | 否 | String |
| serverXcStatus | 服务器信创情况 | 否 | String |
| isFullXc | 是否全栈信创 | 否 | Integer(0/1) |
| hasThirdPartyProduct | 是否使用第三方产品 | 否 | Integer(0/1) |
| thirdPartyIsXc | 第三方产品是否信创 | 否 | Integer(0/1) |
| xcClassification | XC分类 | 否 | String |
| dbServerXcStatus | 数据库服务器信创情况 | 否 | String |
| dbServerOs | 数据库服务器操作系统 | 否 | String |
| databaseXcStatus | 数据库信创情况 | 否 | String |
| serverXcDesc | 服务器信创情况说明 | 否 | String |
| databaseXcDesc | 数据库信创情况说明 | 否 | String |
| partialXcDesc | 非全栈信创情况 | 否 | String |
| thirdPartyXcDesc | 第三方产品信创情况说明 | 否 | String |
| xcOverallDesc | 信创整体情况说明 | 否 | String |

### 4.5 服务信息（Service）

| 字段名 | 中文名 | 必填 | 类型 |
|--------|--------|------|------|
| customerType | 客户类型 | 否 | String |
| serviceTimeType | 服务时间类型 | 否 | String |
| internalUserScope | 内部用户范围 | 否 | String |
| serviceWindowDesc | 服务窗口补充说明 | 否 | String |
| usageScopeDesc | 使用范围补充说明 | 否 | String |

### 4.6 运维信息（Ops）

| 字段名 | 中文名 | 必填 | 类型 |
|--------|--------|------|------|
| cityRpo | 同城RPO | 否 | String |
| cityRto | 同城RTO | 否 | String |
| cityActiveType | 同城双活类型 | 否 | String |
| hasCityEnvironment | 是否具备同城环境 | 否 | Integer(0/1) |
| remoteRpo | 异地RPO | 否 | String |
| remoteRto | 异地RTO | 否 | String |
| remoteActiveType | 异地双活类型 | 否 | String |
| hasDrEnvironment | 是否具备灾备环境 | 否 | Integer(0/1) |
| opsLevel | 运维等级 | 否 | String |
| oldOpsLevel | 旧运维等级 | 否 | String |
| opsUnit | 运维单位 | 否 | String |
| remoteAccessClass | 远程访问权限分类 | 否 | String |
| isChangeAutomation | 是否变更自动化 | 否 | Integer(0/1) |
| changeDeployTime | 变更投产时点 | 否 | String |
| changeDeployTimeDesc | 变更投产时点补充说明 | 否 | String |
| mainBusinessHours | 主要业务时段 | 否 | String |
| dataAssetApprovalDept | 电子数据资产提取审批部门 | 否 | String |
| isContainerized | 是否容器化部署 | 否 | Integer(0/1) |
| deploymentEnvironment | 部署环境 | 否 | String |
| deploymentLocation | 部署地点 | 否 | String |
| deploymentLocationDesc | 部署地点补充说明 | 否 | String |
| drLevel | 灾备等级 | 否 | String |
| drRecoveryLevel | 灾备恢复能力等级 | 否 | String |

---

## 5. 测试数据

导入模板第二行包含以下测试数据：

| 字段 | 测试值 |
|------|--------|
| appCode | APP-TEST |
| appName | 测试应用 |
| appNameEn | Test Application |
| appType | 交易 |
| importanceLevel | 一般 |
| systemLayer | 业务中台层 |
| lifecycle | PLANNING |
| status | PLANNING |
| departmentName | 信息科技部 |
| implementationUnit | 测试单位 |
| implementationDivision | 测试处室 |
| implementationTeam | 测试项目组 |
| goLiveDate | 2026-12-01 |
| description | 这是一个测试应用简介 |
| isInternetApp | 0 |
| isPaymentApp | 0 |
| serviceObject | 行内客户 |
| implementationMethod | 外购产品 |
| implementationType | 业务处理类 |
| loginUserField | username |
| loginPasswordField | password |
| isDeployedDmz | 0 |
| hasUploadFunction | 0 |
| hasDownloadFunction | 0 |
| accessUrl | http://test.example.com |
| domainName | test.example.com |
| networkMode | B/S |
| accessType | 浏览器 |
| xcCloudStatus | 未上云 |
| serverXcStatus | 非信创 |
| databaseXcStatus | 非信创 |
| customerType | 行内客户 |
| serviceTimeType | 5*8 |
| cityRpo | 0 |
| cityRto | 0 |
| hasCityEnvironment | 0 |
| hasDrEnvironment | 0 |
| opsLevel | C类 |
| isContainerized | 0 |
| deploymentEnvironment | 测试环境 |

---

## 6. 导出配置

前端配置导出对话框已有字段分组，修改默认选中为"基本信息"分组：

### 6.1 字段分组

| 分组 | 字段 |
|------|------|
| 基本属性 | appCode*, appName*, appNameEn*, systemLayer, remark, parentAppName, implementationUnit, goLiveDate, retireDate, mainBusinessDomain, secondaryBusinessDomain, description |
| 分类信息 | status*, appType, importanceLevel, departmentName, isInternetApp, serviceObject, isPaymentApp, isElectronicBankingApp, isOnlineBankingApp, isMobileApp, recordStatus, version |
| 实施信息 | implementationDivision*, implementationTeam* |
| 安全等级 | systemProtectionLevel, protectionLevel |
| 技术信息 | implementationMethod, implementationType, loginUserField, loginPasswordField, isDeployedDmz, hasUploadFunction, pwdErrorFreezeMechanism, hasOutboundRequest, accessUrl, domainName, networkMode, accessType, vendorInfo, authMethod, hasThirdPartyIntegration, xcCloudStatus, serverXcStatus, isFullXc, xcClassification, dbServerXcStatus, dbServerOs, databaseXcStatus |
| 服务信息 | customerType, serviceTimeType, internalUserScope |
| 运维信息 | cityRpo, cityRto, cityActiveType, hasCityEnvironment, remoteRpo, remoteRto, remoteActiveType, hasDrEnvironment, opsLevel, oldOpsLevel, opsUnit, remoteAccessClass, isChangeAutomation, changeDeployTime, mainBusinessHours, isContainerized, deploymentEnvironment, deploymentLocation, drLevel, drRecoveryLevel |

### 6.2 默认导出字段（基本信息Tab）

```
appCode, appName, appNameEn, status, departmentName,
implementationUnit, implementationDivision, implementationTeam,
importanceLevel, systemLayer
```

---

## 7. 文件变更清单

### 7.1 后端

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `ArchApplicationController.java` | 修改 | 新增 `GET /import/template` 端点 |
| `ArchApplicationService.java` | 修改 | 实现 `generateImportTemplate()` 和 `importList()` 逻辑 |
| `ArchApplicationImportDTO.java` | 新增 | 导入 DTO，包含所有导入字段 |
| `ImportResult.java` | 新增 | 导入结果封装类 |

### 7.2 前端

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `Application.vue` | 修改 | 扩展 `handleDownloadTemplate` 包含全部字段；修改 `exportFieldGroups` 默认选中 |
| `index.js` (api) | 修改 | 新增 `getImportTemplate()` API |

---

## 8. 错误处理

| 场景 | 处理方式 |
|------|----------|
| Excel 解析失败 | 返回 `code: 400`，message 说明解析错误原因 |
| 缺少必填字段 | 记录到 errors 数组，该行跳过 |
| appCode 重复 | 按 appCode 更新现有记录 |
| 文件格式错误 | 返回 `code: 400`，message 说明 "仅支持 xlsx/xls 格式" |
| 文件为空 | 返回 `code: 400`，message 说明 "文件不能为空" |

---

## 9. 性能考虑

- 导入使用事务管理，单个应用导入失败不影响其他
- 模板生成：简单 Workbook 操作，无性能问题
- 导出：前端 XLSX 处理，建议单次导出不超过 10000 条

---

## 10. 测试用例

| 用例 | 预期结果 |
|------|----------|
| 下载模板 | 获得含 2 行数据（表头+测试）的 xlsx |
| 导入空文件 | 返回错误 "文件不能为空" |
| 导入无 appCode 行 | 该行记录到 errors，返回 partial success |
| 导入 appCode 已存在记录 | 更新现有记录 |
| 导入 appCode 不存在记录 | 新增记录 |
| 配置导出-全选 | 生成包含所有字段的 Excel |
| 配置导出-默认 | 生成仅含基本信息 10 个字段的 Excel |
