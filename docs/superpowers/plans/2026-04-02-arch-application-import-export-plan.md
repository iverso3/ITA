# 应用导入导出功能实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现应用导入（Excel模板+后端解析+upsert）和可配置导出（默认基本信息Tab字段）

**Architecture:**
- 模板生成：后端 Apache POI 生成 xlsx，存 `template/application_import_template_{timestamp}.xlsx`
- 导入：后端 POI 解析 Excel → ArchApplicationImportDTO → ArchApplication (ext_attrs JSON 处理) → upsert by appCode
- 导出：前端 XLSX 保持现有逻辑，仅修改默认选中字段
- ext_attrs 处理：import 时读取 ext_attrs JSON → 合并 DTO 中非 entity 字段 → 序列化回 ext_attrs

**Tech Stack:** Spring Boot 3.2.0, Apache POI 5.x, Vue 3, Element Plus, XLSX

---

## 文件变更清单

### 后端
- **Create:** `src/main/java/com/bank/itarch/model/dto/ArchApplicationImportDTO.java` — 导入 DTO，所有导入字段
- **Create:** `src/main/java/com/bank/itarch/util/ApplicationImportExcelUtil.java` — Excel 工具类（模板生成+解析）
- **Modify:** `src/main/java/com/bank/itarch/model/dto/ImportResult.java` — 增加 updated/created 字段
- **Modify:** `src/main/java/com/bank/itarch/controller/ArchApplicationController.java` — 新增 template/import 端点
- **Modify:** `src/main/java/com/bank/itarch/service/ArchApplicationService.java` — 实现 generateTemplate/importList

### 前端
- **Modify:** `src/api/index.js` — 新增 getImportTemplate API
- **Modify:** `src/views/arch/Application.vue` — handleDownloadTemplate 调用后端 + exportFieldGroups 默认选中调整

---

## 第一阶段：后端基础设施

### Task 1: 创建导入 DTO

**Files:**
- Create: `bank-it-arch-backend/src/main/java/com/bank/itarch/model/dto/ArchApplicationImportDTO.java`

- [ ] **Step 1: 创建 ArchApplicationImportDTO**

```java
package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ArchApplicationImportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // ===== 基本信息 =====
    private String appCode;            // 应用编号（必填）
    private String appName;             // 应用名称（必填）
    private String appNameEn;           // 英文全称
    private String appType;            // 应用类型
    private String importanceLevel;    // 重要级别
    private String systemLayer;        // 系统所属层
    private String lifecycle;          // 生命周期
    private String status;             // 应用状态
    private String departmentName;    // 应用主管部门
    private String implementationUnit; // 实施单位
    private String implementationDivision; // 实施处室
    private String implementationTeam; // 实施项目组
    private String parentAppName;      // 所属系统名称（用于查询 parentAppId）
    private String goLiveDate;         // 投产时间
    private String retireDate;         // 下线日期
    private String mainBusinessDomain;  // 主业务功能领域
    private String secondaryBusinessDomain; // 辅业务功能领域
    private String description;        // 应用简介
    private String remark;             // 备注

    // ===== 分类信息 =====
    private Integer isInternetApp;         // 是否互联网应用
    private Integer isPaymentApp;          // 是否支付应用
    private Integer isElectronicBankingApp; // 是否电子银行应用
    private Integer isOnlineBankingApp;   // 是否网上银行应用
    private Integer isMobileApp;          // 是否移动应用
    private Integer isBillApp;            // 是否票据应用
    private Integer isInternetFinanceApp; // 是否互联网金融应用
    private String serviceObject;          // 服务对象

    // ===== 安全等级 =====
    private String systemProtectionLevel;  // 所属系统等级保护安全等级
    private String protectionLevel;       // 等级保护安全等级

    // ===== 技术信息 =====
    private String implementationMethod;   // 实施方式
    private String implementationType;     // 实施类型
    private String loginUserField;         // 登录用户字段名称
    private String loginPasswordField;     // 登录密码字段名称
    private Integer isDeployedDmz;         // 是否部署在DMZ区
    private Integer hasUploadFunction;     // 是否存在上传功能
    private String pwdErrorFreezeMechanism; // 账号密码错误冻结机制
    private Integer hasOutboundRequest;    // 是否存在主动外发请求
    private String outboundRequestDesc;    // 外发业务场景描述
    private String uploadFunctionDesc;     // 上传的功能描述和URL
    private String uploadFilePath;         // 文件上传路径
    private String uploadPathExecutable;   // 执行权限
    private String uploadFileTypes;        // 上传文件类型
    private Integer hasDownloadFunction;   // 是否存在下载功能
    private String downloadFunctionDesc;   // 下载的功能描述和URL
    private String downloadFileTypes;      // 下载文件类型
    private Integer isAppApplication;     // 是否APP应用
    private String openSourceInfo;         // 开源有关信息
    private String accessUrl;             // 访问主页
    private String domainName;             // 域名
    private String networkMode;            // 网络结构模式
    private String accessType;            // 访问方式
    private String vendorInfo;             // 实施商信息
    private String authMethod;             // 认证方式
    private Integer hasThirdPartyIntegration; // 是否与外部第三方系统对接
    private String thirdPartyIntegrationDesc;  // 与外部第三方系统对接补充说明
    private Integer isInternetLine;       // 互联网线路类型
    private String xcCloudStatus;         // 应用是否上信创容器云
    private String serverXcStatus;        // 服务器信创情况
    private Integer isFullXc;             // 是否全栈信创
    private Integer hasThirdPartyProduct;  // 是否使用第三方产品
    private Integer thirdPartyIsXc;       // 第三方产品是否信创
    private String xcClassification;      // XC分类
    private String dbServerXcStatus;      // 数据库服务器信创情况
    private String dbServerOs;            // 数据库服务器操作系统
    private String databaseXcStatus;      // 数据库信创情况
    private String serverXcDesc;           // 服务器信创情况说明
    private String databaseXcDesc;        // 数据库信创情况说明
    private String partialXcDesc;         // 非全栈信创情况
    private String thirdPartyXcDesc;      // 第三方产品信创情况说明
    private String xcOverallDesc;         // 信创整体情况说明

    // ===== 服务信息 =====
    private String customerType;           // 客户类型
    private String serviceTimeType;        // 服务时间类型
    private String internalUserScope;       // 内部用户范围
    private String serviceWindowDesc;      // 服务窗口补充说明
    private String usageScopeDesc;         // 使用范围补充说明

    // ===== 运维信息 =====
    private String cityRpo;               // 同城RPO
    private String cityRto;               // 同城RTO
    private String cityActiveType;        // 同城双活类型
    private Integer hasCityEnvironment;    // 是否具备同城环境
    private String remoteRpo;             // 异地RPO
    private String remoteRto;             // 异地RTO
    private String remoteActiveType;       // 异地双活类型
    private Integer hasDrEnvironment;      // 是否具备灾备环境
    private String opsLevel;              // 运维等级
    private String oldOpsLevel;           // 旧运维等级
    private String opsUnit;               // 运维单位
    private String remoteAccessClass;      // 远程访问权限分类
    private Integer isChangeAutomation;    // 是否变更自动化
    private String changeDeployTime;       // 变更投产时点
    private String changeDeployTimeDesc;   // 变更投产时点补充说明
    private String mainBusinessHours;      // 主要业务时段
    private String dataAssetApprovalDept;  // 电子数据资产提取审批部门
    private Integer isContainerized;       // 是否容器化部署
    private String deploymentEnvironment;  // 部署环境
    private String deploymentLocation;     // 部署地点
    private String deploymentLocationDesc; // 部署地点补充说明
    private String drLevel;               // 灾备等级
    private String drRecoveryLevel;        // 灾备恢复能力等级
}
```

- [ ] **Step 2: 验证编译**

Run: `cd bank-it-arch-backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/bank/itarch/model/dto/ArchApplicationImportDTO.java
git commit -m "feat(arch-application): add ArchApplicationImportDTO for import"
```

---

### Task 2: 修改 ImportResult 增加 updated/created 字段

**Files:**
- Modify: `bank-it-arch-backend/src/main/java/com/bank/itarch/model/dto/ImportResult.java`

- [ ] **Step 1: 修改 ImportResult.java，追加 updated/created 字段**

在 `ImportResult.java` 的 `total/success/failed/errors` 字段后追加：

```java
    private Integer updated;   // 更新行数
    private Integer created;  // 新增行数
```

在 `ok()` 方法中追加 `updated`/`created` 参数，在 `ImportResult` 静态工厂方法中增加对应入参版本（保留无参版本兼容）：

```java
    public static ImportResult ok(int total, int success, int updated, int created) {
        ImportResult result = new ImportResult();
        result.setTotal(total);
        result.setSuccess(success);
        result.setFailed(total - success);
        result.setUpdated(updated);
        result.setCreated(created);
        return result;
    }
```

- [ ] **Step 2: 验证编译**

Run: `cd bank-it-arch-backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/bank/itarch/model/dto/ImportResult.java
git commit -m "feat: extend ImportResult with updated/created counts"
```

---

### Task 3: 创建 Excel 工具类

**Files:**
- Create: `bank-it-arch-backend/src/main/java/com/bank/itarch/util/ApplicationImportExcelUtil.java`

- [ ] **Step 1: 创建 ApplicationImportExcelUtil.java**

完整代码如下（参考 OssUseStandingBookMainService.parseExcel 和 generateTemplate 逻辑）：

```java
package com.bank.itarch.util;

import com.bank.itarch.model.dto.ArchApplicationImportDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ApplicationImportExcelUtil {

    /** Excel 表头（中文名 -> 字段名映射） */
    private static final LinkedHashMap<String, String> HEADER_MAP = new LinkedHashMap<>();
    static {
        // 基本信息
        HEADER_MAP.put("应用编号", "appCode");
        HEADER_MAP.put("应用名称", "appName");
        HEADER_MAP.put("英文全称", "appNameEn");
        HEADER_MAP.put("应用类型", "appType");
        HEADER_MAP.put("重要级别", "importanceLevel");
        HEADER_MAP.put("系统所属层", "systemLayer");
        HEADER_MAP.put("生命周期", "lifecycle");
        HEADER_MAP.put("应用状态", "status");
        HEADER_MAP.put("应用主管部门", "departmentName");
        HEADER_MAP.put("实施单位", "implementationUnit");
        HEADER_MAP.put("实施处室", "implementationDivision");
        HEADER_MAP.put("实施项目组", "implementationTeam");
        HEADER_MAP.put("所属系统名称", "parentAppName");
        HEADER_MAP.put("投产时间", "goLiveDate");
        HEADER_MAP.put("下线日期", "retireDate");
        HEADER_MAP.put("主业务功能领域", "mainBusinessDomain");
        HEADER_MAP.put("辅业务功能领域", "secondaryBusinessDomain");
        HEADER_MAP.put("应用简介", "description");
        HEADER_MAP.put("备注", "remark");
        // 分类信息
        HEADER_MAP.put("是否互联网应用", "isInternetApp");
        HEADER_MAP.put("是否支付应用", "isPaymentApp");
        HEADER_MAP.put("是否电子银行应用", "isElectronicBankingApp");
        HEADER_MAP.put("是否网上银行应用", "isOnlineBankingApp");
        HEADER_MAP.put("是否移动应用", "isMobileApp");
        HEADER_MAP.put("是否票据应用", "isBillApp");
        HEADER_MAP.put("是否互联网金融应用", "isInternetFinanceApp");
        HEADER_MAP.put("服务对象", "serviceObject");
        // 安全等级
        HEADER_MAP.put("所属系统等级保护安全等级", "systemProtectionLevel");
        HEADER_MAP.put("等级保护安全等级", "protectionLevel");
        // 技术信息
        HEADER_MAP.put("实施方式", "implementationMethod");
        HEADER_MAP.put("实施类型", "implementationType");
        HEADER_MAP.put("登录用户字段名称", "loginUserField");
        HEADER_MAP.put("登录密码字段名称", "loginPasswordField");
        HEADER_MAP.put("是否部署在DMZ区", "isDeployedDmz");
        HEADER_MAP.put("是否存在上传功能", "hasUploadFunction");
        HEADER_MAP.put("账号密码错误冻结机制", "pwdErrorFreezeMechanism");
        HEADER_MAP.put("是否存在主动外发请求", "hasOutboundRequest");
        HEADER_MAP.put("外发业务场景描述", "outboundRequestDesc");
        HEADER_MAP.put("上传的功能描述和URL", "uploadFunctionDesc");
        HEADER_MAP.put("文件上传路径", "uploadFilePath");
        HEADER_MAP.put("执行权限", "uploadPathExecutable");
        HEADER_MAP.put("上传文件类型", "uploadFileTypes");
        HEADER_MAP.put("是否存在下载功能", "hasDownloadFunction");
        HEADER_MAP.put("下载的功能描述和URL", "downloadFunctionDesc");
        HEADER_MAP.put("下载文件类型", "downloadFileTypes");
        HEADER_MAP.put("是否APP应用", "isAppApplication");
        HEADER_MAP.put("开源有关信息", "openSourceInfo");
        HEADER_MAP.put("访问主页", "accessUrl");
        HEADER_MAP.put("域名", "domainName");
        HEADER_MAP.put("网络结构模式", "networkMode");
        HEADER_MAP.put("访问方式", "accessType");
        HEADER_MAP.put("实施商信息", "vendorInfo");
        HEADER_MAP.put("认证方式", "authMethod");
        HEADER_MAP.put("是否与外部第三方系统对接", "hasThirdPartyIntegration");
        HEADER_MAP.put("与外部第三方系统对接补充说明", "thirdPartyIntegrationDesc");
        HEADER_MAP.put("互联网线路类型", "isInternetLine");
        HEADER_MAP.put("应用是否上信创容器云", "xcCloudStatus");
        HEADER_MAP.put("服务器信创情况", "serverXcStatus");
        HEADER_MAP.put("是否全栈信创", "isFullXc");
        HEADER_MAP.put("是否使用第三方产品", "hasThirdPartyProduct");
        HEADER_MAP.put("第三方产品是否信创", "thirdPartyIsXc");
        HEADER_MAP.put("XC分类", "xcClassification");
        HEADER_MAP.put("数据库服务器信创情况", "dbServerXcStatus");
        HEADER_MAP.put("数据库服务器操作系统", "dbServerOs");
        HEADER_MAP.put("数据库信创情况", "databaseXcStatus");
        HEADER_MAP.put("服务器信创情况说明", "serverXcDesc");
        HEADER_MAP.put("数据库信创情况说明", "databaseXcDesc");
        HEADER_MAP.put("非全栈信创情况", "partialXcDesc");
        HEADER_MAP.put("第三方产品信创情况说明", "thirdPartyXcDesc");
        HEADER_MAP.put("信创整体情况说明", "xcOverallDesc");
        // 服务信息
        HEADER_MAP.put("客户类型", "customerType");
        HEADER_MAP.put("服务时间类型", "serviceTimeType");
        HEADER_MAP.put("内部用户范围", "internalUserScope");
        HEADER_MAP.put("服务窗口补充说明", "serviceWindowDesc");
        HEADER_MAP.put("使用范围补充说明", "usageScopeDesc");
        // 运维信息
        HEADER_MAP.put("同城RPO", "cityRpo");
        HEADER_MAP.put("同城RTO", "cityRto");
        HEADER_MAP.put("同城双活类型", "cityActiveType");
        HEADER_MAP.put("是否具备同城环境", "hasCityEnvironment");
        HEADER_MAP.put("异地RPO", "remoteRpo");
        HEADER_MAP.put("异地RTO", "remoteRto");
        HEADER_MAP.put("异地双活类型", "remoteActiveType");
        HEADER_MAP.put("是否具备灾备环境", "hasDrEnvironment");
        HEADER_MAP.put("运维等级", "opsLevel");
        HEADER_MAP.put("旧运维等级", "oldOpsLevel");
        HEADER_MAP.put("运维单位", "opsUnit");
        HEADER_MAP.put("远程访问权限分类", "remoteAccessClass");
        HEADER_MAP.put("是否变更自动化", "isChangeAutomation");
        HEADER_MAP.put("变更投产时点", "changeDeployTime");
        HEADER_MAP.put("变更投产时点补充说明", "changeDeployTimeDesc");
        HEADER_MAP.put("主要业务时段", "mainBusinessHours");
        HEADER_MAP.put("电子数据资产提取审批部门", "dataAssetApprovalDept");
        HEADER_MAP.put("是否容器化部署", "isContainerized");
        HEADER_MAP.put("部署环境", "deploymentEnvironment");
        HEADER_MAP.put("部署地点", "deploymentLocation");
        HEADER_MAP.put("部署地点补充说明", "deploymentLocationDesc");
        HEADER_MAP.put("灾备等级", "drLevel");
        HEADER_MAP.put("灾备恢复能力等级", "drRecoveryLevel");
    }

    /** 生成导入模板（含表头+测试数据行），写入 outputStream */
    public static void generateTemplate(OutputStream outputStream) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("应用导入模板");
            Row headerRow = sheet.createRow(0);

            // 写入表头
            String[] headers = HEADER_MAP.keySet().toArray(new String[0]);
            CellStyle headerStyle = createHeaderStyle(wb);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 写入测试数据行
            Row dataRow = sheet.createRow(1);
            String[] testValues = getTestDataRow();
            for (int i = 0; i < testValues.length; i++) {
                Cell cell = dataRow.createCell(i);
                cell.setCellValue(testValues[i]);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 20 * 256);
            }

            wb.write(outputStream);
        }
    }

    /** 解析 Excel 文件，返回导入 DTO 列表 */
    public static List<ArchApplicationImportDTO> parseExcel(InputStream inputStream) throws Exception {
        List<ArchApplicationImportDTO> rows = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel表头不能为空");
            }

            // 构建 header -> 列索引 映射
            Map<String, Integer> headerIndexMap = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    String headerName = cell.getStringCellValue().trim();
                    if (HEADER_MAP.containsKey(headerName)) {
                        headerIndexMap.put(headerName, i);
                    }
                }
            }

            // 解析数据行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                ArchApplicationImportDTO dto = new ArchApplicationImportDTO();
                for (Map.Entry<String, Integer> entry : headerIndexMap.entrySet()) {
                    String headerName = entry.getKey();
                    int colIndex = entry.getValue();
                    String fieldName = HEADER_MAP.get(headerName);
                    String value = getCellStringValue(row, colIndex);
                    setFieldValue(dto, fieldName, value);
                }
                rows.add(dto);
            }
        }
        return rows;
    }

    private static void setFieldValue(ArchApplicationImportDTO dto, String fieldName, String value) {
        try {
            java.lang.reflect.Field field = ArchApplicationImportDTO.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Class<?> type = field.getType();

            if (type == Integer.class || type == int.class) {
                if (StringUtils.hasText(value)) {
                    field.set(dto, Integer.parseInt(value));
                }
            } else {
                field.set(dto, value);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 忽略未知字段
        }
    }

    private static String getCellStringValue(Row row, int colIndex) {
        if (colIndex < 0) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return null;
        }
    }

    private static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /** 返回测试数据行，与 HEADER_MAP 顺序完全对应 */
    private static String[] getTestDataRow() {
        List<String> values = new ArrayList<>();
        // 基本信息
        values.add("APP-TEST");          // appCode
        values.add("测试应用");          // appName
        values.add("Test Application");  // appNameEn
        values.add("交易");             // appType
        values.add("一般");             // importanceLevel
        values.add("业务中台层");       // systemLayer
        values.add("PLANNING");         // lifecycle
        values.add("PLANNING");         // status
        values.add("信息科技部");        // departmentName
        values.add("测试单位");          // implementationUnit
        values.add("测试处室");         // implementationDivision
        values.add("测试项目组");       // implementationTeam
        values.add("");                 // parentAppName
        values.add("2026-12-01");       // goLiveDate
        values.add("");                 // retireDate
        values.add("");                 // mainBusinessDomain
        values.add("");                 // secondaryBusinessDomain
        values.add("这是一个测试应用简介"); // description
        values.add("");                 // remark
        // 分类信息
        values.add("0");                // isInternetApp
        values.add("0");                // isPaymentApp
        values.add("0");                // isElectronicBankingApp
        values.add("0");                // isOnlineBankingApp
        values.add("0");                // isMobileApp
        values.add("0");                // isBillApp
        values.add("0");                // isInternetFinanceApp
        values.add("行内客户");         // serviceObject
        // 安全等级
        values.add("");                 // systemProtectionLevel
        values.add("");                 // protectionLevel
        // 技术信息
        values.add("外购产品");         // implementationMethod
        values.add("业务处理类");       // implementationType
        values.add("username");          // loginUserField
        values.add("password");          // loginPasswordField
        values.add("0");                // isDeployedDmz
        values.add("0");                // hasUploadFunction
        values.add("");                 // pwdErrorFreezeMechanism
        values.add("0");                // hasOutboundRequest
        values.add("");                 // outboundRequestDesc
        values.add("");                 // uploadFunctionDesc
        values.add("");                 // uploadFilePath
        values.add("");                 // uploadPathExecutable
        values.add("");                 // uploadFileTypes
        values.add("0");                // hasDownloadFunction
        values.add("");                 // downloadFunctionDesc
        values.add("");                 // downloadFileTypes
        values.add("0");                // isAppApplication
        values.add("");                 // openSourceInfo
        values.add("http://test.example.com"); // accessUrl
        values.add("test.example.com");  // domainName
        values.add("B/S");             // networkMode
        values.add("浏览器");           // accessType
        values.add("");                 // vendorInfo
        values.add("");                 // authMethod
        values.add("0");               // hasThirdPartyIntegration
        values.add("");                // thirdPartyIntegrationDesc
        values.add("0");               // isInternetLine
        values.add("未上云");           // xcCloudStatus
        values.add("非信创");           // serverXcStatus
        values.add("0");               // isFullXc
        values.add("0");               // hasThirdPartyProduct
        values.add("0");               // thirdPartyIsXc
        values.add("");                // xcClassification
        values.add("");                // dbServerXcStatus
        values.add("");                // dbServerOs
        values.add("非信创");           // databaseXcStatus
        values.add("");                // serverXcDesc
        values.add("");                // databaseXcDesc
        values.add("");                // partialXcDesc
        values.add("");                // thirdPartyXcDesc
        values.add("");                // xcOverallDesc
        // 服务信息
        values.add("行内客户");         // customerType
        values.add("5*8");             // serviceTimeType
        values.add("");                // internalUserScope
        values.add("");                // serviceWindowDesc
        values.add("");                // usageScopeDesc
        // 运维信息
        values.add("");                // cityRpo
        values.add("");                // cityRto
        values.add("");                // cityActiveType
        values.add("0");               // hasCityEnvironment
        values.add("");                // remoteRpo
        values.add("");                // remoteRto
        values.add("");                // remoteActiveType
        values.add("0");               // hasDrEnvironment
        values.add("C类");             // opsLevel
        values.add("");                // oldOpsLevel
        values.add("");                // opsUnit
        values.add("");                // remoteAccessClass
        values.add("0");               // isChangeAutomation
        values.add("");                // changeDeployTime
        values.add("");                // changeDeployTimeDesc
        values.add("");                // mainBusinessHours
        values.add("");                // dataAssetApprovalDept
        values.add("0");               // isContainerized
        values.add("测试环境");        // deploymentEnvironment
        values.add("");                // deploymentLocation
        values.add("");                // deploymentLocationDesc
        values.add("");                // drLevel
        values.add("");                // drRecoveryLevel

        return values.toArray(new String[0]);
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd bank-it-arch-backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/bank/itarch/util/ApplicationImportExcelUtil.java
git commit -m "feat(arch-application): add ApplicationImportExcelUtil for template generation and parsing"
```

---

### Task 4: 实现 ArchApplicationService 导入/模板逻辑

**Files:**
- Modify: `bank-it-arch-backend/src/main/java/com/bank/itarch/service/ArchApplicationService.java`

- [ ] **Step 1: 在 ArchApplicationService 中注入依赖**

确认已有:
```java
private final ArchApplicationMapper mapper;
```

需要注入Mapper以使用 baseMapper（继承自ServiceImpl）:

```java
// 已在 ServiceImpl 中拥有 baseMapper
```

- [ ] **Step 2: 实现 generateImportTemplate 方法**

在 `ArchApplicationService.java` 中添加:

```java
    private static final String TEMPLATE_DIR = "template/";

    /**
     * 生成导入模板文件，返回文件路径
     */
    public String generateImportTemplate() throws IOException {
        String filename = "application_import_template_" 
            + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
        String filePath = TEMPLATE_DIR + filename;

        // 确保目录存在
        File dir = new File(TEMPLATE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            ApplicationImportExcelUtil.generateTemplate(fos);
        }
        return filePath;
    }
```

- [ ] **Step 3: 实现 importList 方法（替换原有的 throw）**

将原有的 `throw new UnsupportedOperationException` 替换为完整实现:

```java
    @Transactional
    public ImportResult importList(MultipartFile file) throws Exception {
        // 1. 解析 Excel
        List<ArchApplicationImportDTO> rows;
        try {
            rows = ApplicationImportExcelUtil.parseExcel(file.getInputStream());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, e.getMessage());
        } catch (Exception e) {
            throw new BusinessException(400, "Excel解析失败: " + e.getMessage());
        }

        if (rows.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        // 2. 校验空行
        List<ImportResult.ImportError> errors = new ArrayList<>();
        List<ArchApplicationImportDTO> validRows = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            ArchApplicationImportDTO row = rows.get(i);
            if (!StringUtils.hasText(row.getAppCode()) || !StringUtils.hasText(row.getAppName())) {
                errors.add(new ImportResult.ImportError(i + 2, "appCode/appName 不能为空"));
            } else {
                validRows.add(row);
            }
        }

        // 3. Upsert
        int updatedCount = 0;
        int createdCount = 0;
        for (ArchApplicationImportDTO dto : validRows) {
            // 查询是否已存在
            LambdaQueryWrapper<ArchApplication> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ArchApplication::getAppCode, dto.getAppCode());
            ArchApplication existing = baseMapper.selectOne(wrapper);

            if (existing != null) {
                // 更新
                applyDtoToEntity(existing, dto);
                baseMapper.updateById(existing);
                updatedCount++;
            } else {
                // 新增
                ArchApplication entity = new ArchApplication();
                applyDtoToEntity(entity, dto);
                // appCode 自生成逻辑只在 create 时生效，导入时使用 Excel 中的 appCode
                entity.setAppCode(dto.getAppCode());
                baseMapper.insert(entity);
                createdCount++;
            }
        }

        if (!errors.isEmpty()) {
            return ImportResult.error(rows.size(), errors);
        }
        return ImportResult.ok(rows.size(), validRows.size(), updatedCount, createdCount);
    }
```

- [ ] **Step 4: 添加 applyDtoToEntity 私有方法**

```java
    /**
     * 将 DTO 中的字段设置到 entity（entity 字段直接设，扩展字段设入 ext_attrs JSON）
     */
    private void applyDtoToEntity(ArchApplication entity, ArchApplicationImportDTO dto) {
        // 直接字段设置
        if (dto.getAppName() != null) entity.setAppName(dto.getAppName());
        if (dto.getAppNameEn() != null) entity.setAppNameEn(dto.getAppNameEn());
        if (dto.getAppType() != null) entity.setAppType(dto.getAppType());
        if (dto.getImportanceLevel() != null) entity.setImportanceLevel(dto.getImportanceLevel());
        if (dto.getSystemLayer() != null) entity.setSystemLayer(dto.getSystemLayer());
        if (dto.getLifecycle() != null) entity.setLifecycle(dto.getLifecycle());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        else entity.setStatus("PLANNING"); // 默认值
        if (dto.getDepartmentName() != null) entity.setDepartmentName(dto.getDepartmentName());
        if (dto.getImplementationUnit() != null) entity.setImplementationUnit(dto.getImplementationUnit());
        if (dto.getImplementationDivision() != null) entity.setImplementationDivision(dto.getImplementationDivision());
        if (dto.getImplementationTeam() != null) entity.setImplementationTeam(dto.getImplementationTeam());
        if (dto.getGoLiveDate() != null) entity.setGoLiveDate(dto.getGoLiveDate());
        if (dto.getRetireDate() != null) entity.setRetireDate(dto.getRetireDate());

        // 布尔标志
        if (dto.getIsInternetApp() != null) entity.setIsInternetApp(dto.getIsInternetApp());
        if (dto.getIsPaymentApp() != null) entity.setIsPaymentApp(dto.getIsPaymentApp());
        if (dto.getIsElectronicBankingApp() != null) entity.setIsElectronicBankingApp(dto.getIsElectronicBankingApp());
        if (dto.getIsOnlineBankingApp() != null) entity.setIsOnlineBankingApp(dto.getIsOnlineBankingApp());
        if (dto.getIsMobileApp() != null) entity.setIsMobileApp(dto.getIsMobileApp());
        if (dto.getIsBillApp() != null) entity.setIsBillApp(dto.getIsBillApp());
        if (dto.getIsInternetFinanceApp() != null) entity.setIsInternetFinanceApp(dto.getIsInternetFinanceApp());

        // parentAppName -> parentAppId
        if (StringUtils.hasText(dto.getParentAppName())) {
            LambdaQueryWrapper<ArchApplication> parentWrapper = new LambdaQueryWrapper<>();
            parentWrapper.eq(ArchApplication::getAppName, dto.getParentAppName()).last("LIMIT 1");
            ArchApplication parent = baseMapper.selectOne(parentWrapper);
            if (parent != null) {
                entity.setParentAppId(parent.getId());
            }
        }

        // 处理 ext_attrs JSON
        Map<String, Object> extAttrs = new HashMap<>();
        if (StringUtils.hasText(entity.getExtAttrs())) {
            try {
                extAttrs = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    entity.getExtAttrs(), Map.class);
            } catch (Exception e) {
                extAttrs = new HashMap<>();
            }
        }

        // 设置扩展字段
        setExtAttr(extAttrs, dto.getMainBusinessDomain(), "mainBusinessDomain");
        setExtAttr(extAttrs, dto.getSecondaryBusinessDomain(), "secondaryBusinessDomain");
        setExtAttr(extAttrs, dto.getDescription(), "description");
        setExtAttr(extAttrs, dto.getRemark(), "remark");
        setExtAttr(extAttrs, dto.getServiceObject(), "serviceObject");
        setExtAttr(extAttrs, dto.getSystemProtectionLevel(), "systemProtectionLevel");
        setExtAttr(extAttrs, dto.getProtectionLevel(), "protectionLevel");
        setExtAttr(extAttrs, dto.getImplementationMethod(), "implementationMethod");
        setExtAttr(extAttrs, dto.getImplementationType(), "implementationType");
        setExtAttr(extAttrs, dto.getLoginUserField(), "loginUserField");
        setExtAttr(extAttrs, dto.getLoginPasswordField(), "loginPasswordField");
        setExtAttr(extAttrs, dto.getIsDeployedDmz(), "isDeployedDmz");
        setExtAttr(extAttrs, dto.getHasUploadFunction(), "hasUploadFunction");
        setExtAttr(extAttrs, dto.getPwdErrorFreezeMechanism(), "pwdErrorFreezeMechanism");
        setExtAttr(extAttrs, dto.getHasOutboundRequest(), "hasOutboundRequest");
        setExtAttr(extAttrs, dto.getOutboundRequestDesc(), "outboundRequestDesc");
        setExtAttr(extAttrs, dto.getUploadFunctionDesc(), "uploadFunctionDesc");
        setExtAttr(extAttrs, dto.getUploadFilePath(), "uploadFilePath");
        setExtAttr(extAttrs, dto.getUploadPathExecutable(), "uploadPathExecutable");
        setExtAttr(extAttrs, dto.getUploadFileTypes(), "uploadFileTypes");
        setExtAttr(extAttrs, dto.getHasDownloadFunction(), "hasDownloadFunction");
        setExtAttr(extAttrs, dto.getDownloadFunctionDesc(), "downloadFunctionDesc");
        setExtAttr(extAttrs, dto.getDownloadFileTypes(), "downloadFileTypes");
        setExtAttr(extAttrs, dto.getIsAppApplication(), "isAppApplication");
        setExtAttr(extAttrs, dto.getOpenSourceInfo(), "openSourceInfo");
        setExtAttr(extAttrs, dto.getAccessUrl(), "accessUrl");
        setExtAttr(extAttrs, dto.getDomainName(), "domainName");
        setExtAttr(extAttrs, dto.getNetworkMode(), "networkMode");
        setExtAttr(extAttrs, dto.getAccessType(), "accessType");
        setExtAttr(extAttrs, dto.getVendorInfo(), "vendorInfo");
        setExtAttr(extAttrs, dto.getAuthMethod(), "authMethod");
        setExtAttr(extAttrs, dto.getHasThirdPartyIntegration(), "hasThirdPartyIntegration");
        setExtAttr(extAttrs, dto.getThirdPartyIntegrationDesc(), "thirdPartyIntegrationDesc");
        setExtAttr(extAttrs, dto.getIsInternetLine(), "isInternetLine");
        setExtAttr(extAttrs, dto.getXcCloudStatus(), "xcCloudStatus");
        setExtAttr(extAttrs, dto.getServerXcStatus(), "serverXcStatus");
        setExtAttr(extAttrs, dto.getIsFullXc(), "isFullXc");
        setExtAttr(extAttrs, dto.getHasThirdPartyProduct(), "hasThirdPartyProduct");
        setExtAttr(extAttrs, dto.getThirdPartyIsXc(), "thirdPartyIsXc");
        setExtAttr(extAttrs, dto.getXcClassification(), "xcClassification");
        setExtAttr(extAttrs, dto.getDbServerXcStatus(), "dbServerXcStatus");
        setExtAttr(extAttrs, dto.getDbServerOs(), "dbServerOs");
        setExtAttr(extAttrs, dto.getDatabaseXcStatus(), "databaseXcStatus");
        setExtAttr(extAttrs, dto.getServerXcDesc(), "serverXcDesc");
        setExtAttr(extAttrs, dto.getDatabaseXcDesc(), "databaseXcDesc");
        setExtAttr(extAttrs, dto.getPartialXcDesc(), "partialXcDesc");
        setExtAttr(extAttrs, dto.getThirdPartyXcDesc(), "thirdPartyXcDesc");
        setExtAttr(extAttrs, dto.getXcOverallDesc(), "xcOverallDesc");
        setExtAttr(extAttrs, dto.getCustomerType(), "customerType");
        setExtAttr(extAttrs, dto.getServiceTimeType(), "serviceTimeType");
        setExtAttr(extAttrs, dto.getInternalUserScope(), "internalUserScope");
        setExtAttr(extAttrs, dto.getServiceWindowDesc(), "serviceWindowDesc");
        setExtAttr(extAttrs, dto.getUsageScopeDesc(), "usageScopeDesc");
        setExtAttr(extAttrs, dto.getCityRpo(), "cityRpo");
        setExtAttr(extAttrs, dto.getCityRto(), "cityRto");
        setExtAttr(extAttrs, dto.getCityActiveType(), "cityActiveType");
        setExtAttr(extAttrs, dto.getHasCityEnvironment(), "hasCityEnvironment");
        setExtAttr(extAttrs, dto.getRemoteRpo(), "remoteRpo");
        setExtAttr(extAttrs, dto.getRemoteRto(), "remoteRto");
        setExtAttr(extAttrs, dto.getRemoteActiveType(), "remoteActiveType");
        setExtAttr(extAttrs, dto.getHasDrEnvironment(), "hasDrEnvironment");
        setExtAttr(extAttrs, dto.getOpsLevel(), "opsLevel");
        setExtAttr(extAttrs, dto.getOldOpsLevel(), "oldOpsLevel");
        setExtAttr(extAttrs, dto.getOpsUnit(), "opsUnit");
        setExtAttr(extAttrs, dto.getRemoteAccessClass(), "remoteAccessClass");
        setExtAttr(extAttrs, dto.getIsChangeAutomation(), "isChangeAutomation");
        setExtAttr(extAttrs, dto.getChangeDeployTime(), "changeDeployTime");
        setExtAttr(extAttrs, dto.getChangeDeployTimeDesc(), "changeDeployTimeDesc");
        setExtAttr(extAttrs, dto.getMainBusinessHours(), "mainBusinessHours");
        setExtAttr(extAttrs, dto.getDataAssetApprovalDept(), "dataAssetApprovalDept");
        setExtAttr(extAttrs, dto.getIsContainerized(), "isContainerized");
        setExtAttr(extAttrs, dto.getDeploymentEnvironment(), "deploymentEnvironment");
        setExtAttr(extAttrs, dto.getDeploymentLocation(), "deploymentLocation");
        setExtAttr(extAttrs, dto.getDeploymentLocationDesc(), "deploymentLocationDesc");
        setExtAttr(extAttrs, dto.getDrLevel(), "drLevel");
        setExtAttr(extAttrs, dto.getDrRecoveryLevel(), "drRecoveryLevel");

        try {
            entity.setExtAttrs(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(extAttrs));
        } catch (Exception e) {
            // ignore
        }
    }

    private void setExtAttr(Map<String, Object> extAttrs, Object value, String key) {
        if (value != null) {
            extAttrs.put(key, value);
        }
    }
```

- [ ] **Step 5: 添加必要的 import**

在 ArchApplicationService.java 顶部追加:
```java
import com.bank.itarch.model.dto.ArchApplicationImportDTO;
import com.bank.itarch.model.dto.ImportResult;
import com.bank.itarch.util.ApplicationImportExcelUtil;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Date;
```

- [ ] **Step 6: 验证编译**

Run: `cd bank-it-arch-backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: 提交**

```bash
git add src/main/java/com/bank/itarch/service/ArchApplicationService.java
git commit -m "feat(arch-application): implement generateTemplate and importList with ext_attrs handling"
```

---

### Task 5: 新增 Controller 端点

**Files:**
- Modify: `bank-it-arch-backend/src/main/java/com/bank/itarch/controller/ArchApplicationController.java`

- [ ] **Step 1: 在 ArchApplicationController 中添加两个新端点**

在现有的 `POST /applications/import` 方法后面添加:

```java
    @GetMapping("/applications/import/template")
    @Operation(summary = "下载应用导入模板")
    public void getImportTemplate(HttpServletResponse response) {
        try {
            String filePath = applicationService.generateImportTemplate();
            File file = new File(filePath);
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", 
                "attachment;filename*=UTF-8''" + java.net.URLEncoder.encode(file.getName(), "UTF-8"));
            try (java.io.InputStream is = new java.io.FileInputStream(file);
                 java.io.OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
```

同时在 Controller 顶部添加:
```java
import javax.servlet.http.HttpServletResponse;
import java.io.File;
```

- [ ] **Step 2: 修改现有 import 端点，替换 importList 实现**

原有的 `importApplications` 方法的 `throw UnsupportedOperationException` 需要被 service 层实现替代 — 确认 Controller 调用不变即可:

```java
    @PostMapping("/applications/import")
    @Operation(summary = "导入应用")
    public Result<ImportResult> importApplications(
            @RequestParam("file") MultipartFile file) {
        try {
            ImportResult result = applicationService.importList(file);
            return Result.success("导入完成", result);
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }
```

- [ ] **Step 3: 验证编译**

Run: `cd bank-it-arch-backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/bank/itarch/controller/ArchApplicationController.java
git commit -m "feat(arch-application): add import template and import endpoints"
```

---

## 第二阶段：前端改造

### Task 6: 前端 API 和下载模板改造

**Files:**
- Modify: `bank-it-arch-frontend/src/api/index.js` (archAppApi 段落)
- Modify: `bank-it-arch-frontend/src/views/arch/Application.vue`

#### 6.1 修改 api/index.js

- [ ] **Step 1: 在 archAppApi 中添加 getImportTemplate 方法**

在 `archAppApi` 对象中添加:

```javascript
  getImportTemplate: () => api.get('/arch/applications/import/template', { responseType: 'blob' }),
```

#### 6.2 修改 Application.vue

- [ ] **Step 2: 修改 handleDownloadTemplate，调用后端 API**

将原有的前端生成模板逻辑替换为调用后端 API:

```javascript
const handleDownloadTemplate = async () => {
  try {
    loading.value = true
    const res = await archAppApi.getImportTemplate()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    // 从 Content-Disposition 获取文件名
    const contentDisposition = res.headers['content-disposition']
    let fileName = 'application_import_template.xlsx'
    if (contentDisposition) {
      const fileNameMatch = contentDisposition.match(/filename\*?=['"]?(?:UTF-8'')?([^;\n"']+)/i)
      if (fileNameMatch && fileNameMatch[1]) {
        fileName = decodeURIComponent(fileNameMatch[1])
      }
    }
    link.download = fileName
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (e) {
    ElMessage.error('模板下载失败')
  } finally {
    loading.value = false
  }
}
```

**注意:** axios 拦截器会自动提取 `.data`，所以 `res` 实际是 blob 数据。如果拦截器处理了 blob，需要调整。根据项目 axios 拦截器配置（`src/api/index.js` 中的响应拦截器），如果 blob 未被正确处理，需要在请求配置中明确不使用拦截器的 data 提取。

实际项目中，更安全的做法是直接用 `window.open(url)` 或创建一个隐藏的 a 标签下载。

**如果后端直接返回文件流（绕过拦截器提取），前端响应需配置 `responseType: 'blob'` 并让 axios 不做 JSON 转换。** 参照项目中已有的 `export: (params) => api.get('/arch/applications/export', { params, responseType: 'blob' })` 用法，确认 `getImportTemplate` 配置 `responseType: 'blob'` 后，拦截器返回的 res 应该是完整的 blob。

**备选方案（最安全）：使用原生 XMLHttpRequest**

```javascript
const handleDownloadTemplate = () => {
  const xhr = new XMLHttpRequest()
  xhr.open('GET', '/api/v1/arch/applications/import/template', true)
  xhr.responseType = 'blob'
  xhr.onload = function() {
    if (xhr.status === 200) {
      const blob = xhr.response
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = 'application_import_template.xlsx'
      link.click()
      window.URL.revokeObjectURL(url)
      ElMessage.success('模板下载成功')
    } else {
      ElMessage.error('模板下载失败')
    }
  }
  xhr.send()
}
```

#### 6.3 修改 exportFieldGroups 默认选中

- [ ] **Step 3: 修改默认导出字段为基本信息Tab**

在 Application.vue 中找到 `selectedExportFields`，将默认值从:
```javascript
const selectedExportFields = ref([
  'appCode', 'appName', 'appNameEn', 'status', 'departmentName',
  'implementationDivision', 'implementationTeam', 'importanceLevel',
  'recordStatus', 'version'
])
```

改为:
```javascript
const selectedExportFields = ref([
  'appCode', 'appName', 'appNameEn', 'status', 'departmentName',
  'implementationUnit', 'implementationDivision', 'implementationTeam',
  'importanceLevel', 'systemLayer'
])
```

#### 6.4 验证前端编译

- [ ] **Step 4: 运行前端构建**

Run: `cd bank-it-arch-frontend && npm run build 2>&1 | head -30`
Expected: 无编译错误

- [ ] **Step 5: 提交**

```bash
git add src/api/index.js src/views/arch/Application.vue
git commit -m "feat(arch-application): call backend for import template; update default export fields to basic tab"
```

---

## 第三阶段：验证与测试

### Task 7: 启动应用并验证

- [ ] **Step 1: 启动后端**

Run: `cd bank-it-arch-backend && mvn spring-boot:run -q`
Expected: 启动无报错，日志显示 `Started` 

- [ ] **Step 2: 测试模板下载接口**

在浏览器或 Postman 访问:
```
GET http://localhost:8080/api/v1/arch/applications/import/template
```
Expected: 返回 xlsx 文件流，文件名含时间戳

- [ ] **Step 3: 测试导入接口（准备测试数据）**

用下载的模板，在第二行填入:
- appCode: `APP-00001`
- appName: `验证应用`
- appNameEn: `Verify App`
- appType: `交易`
- importanceLevel: `一般`
- systemLayer: `业务中台层`
- status: `PLANNING`

用 curl 发送:
```bash
curl -X POST "http://localhost:8080/api/v1/arch/applications/import" \
  -H "Authorization: Bearer <token>" \
  -F "file=@验证模板.xlsx"
```
Expected: `{"code":200,"message":"success","data":{"total":1,"success":1,"updated":0,"created":1}}`

- [ ] **Step 4: 再次导入相同 appCode 验证覆盖**

Expected: updated=1, created=0

- [ ] **Step 5: 前端启动验证**

Run: `cd bank-it-arch-frontend && npm run dev`
Expected: 开发服务器启动，http://localhost:5173 可访问

---

## 实施核对清单

- [ ] Task 1: ArchApplicationImportDTO 创建并编译通过
- [ ] Task 2: ImportResult 更新 updated/created 并编译通过
- [ ] Task 3: ApplicationImportExcelUtil 创建并编译通过
- [ ] Task 4: ArchApplicationService importList/generateTemplate 实现并编译通过
- [ ] Task 5: Controller 端点添加，编译通过
- [ ] Task 6: 前端 API 和组件修改，构建通过
- [ ] Task 7: 启动后端+前端，全流程验证

---

## 关键设计决策记录

1. **ext_attrs 处理**: ArchApplication 的 ext_attrs 为 JSON 字符串字段。导入时读取现有 ext_attrs → 合并 DTO 中非 entity 字段 → 序列化回 ext_attrs。
2. **parentAppName 处理**: 导入时通过 parentAppName 查询得到 parentAppId 存入 entity。
3. **模板文件**: 动态生成，文件名含时间戳，每次调用生成新文件。
4. **Upsert 策略**: 按 appCode 唯一键，存在则更新，不存在则新增。
5. **导出保持前端实现**: 不改现有导出逻辑，仅调整默认字段。
