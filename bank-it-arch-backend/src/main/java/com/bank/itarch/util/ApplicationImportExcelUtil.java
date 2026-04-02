package com.bank.itarch.util;

import com.bank.itarch.model.dto.ArchApplicationImportDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ApplicationImportExcelUtil {

    /** Excel 表头（中文名 -> 字段名映射），顺序与测试数据行完全对应 */
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
        values.add("APP-TEST");           // appCode
        values.add("测试应用");           // appName
        values.add("Test Application");  // appNameEn
        values.add("交易");              // appType
        values.add("一般");              // importanceLevel
        values.add("业务中台层");         // systemLayer
        values.add("PLANNING");          // lifecycle
        values.add("PLANNING");          // status
        values.add("信息科技部");         // departmentName
        values.add("测试单位");           // implementationUnit
        values.add("测试处室");           // implementationDivision
        values.add("测试项目组");         // implementationTeam
        values.add("");                  // parentAppName
        values.add("2026-12-01");       // goLiveDate
        values.add("");                  // retireDate
        values.add("");                  // mainBusinessDomain
        values.add("");                  // secondaryBusinessDomain
        values.add("这是一个测试应用简介"); // description
        values.add("");                  // remark
        // 分类信息
        values.add("0");                // isInternetApp
        values.add("0");                // isPaymentApp
        values.add("0");                // isElectronicBankingApp
        values.add("0");                // isOnlineBankingApp
        values.add("0");                // isMobileApp
        values.add("0");                // isBillApp
        values.add("0");                // isInternetFinanceApp
        values.add("行内客户");          // serviceObject
        // 安全等级
        values.add("");                 // systemProtectionLevel
        values.add("");                 // protectionLevel
        // 技术信息
        values.add("外购产品");          // implementationMethod
        values.add("业务处理类");        // implementationType
        values.add("username");           // loginUserField
        values.add("password");           // loginPasswordField
        values.add("0");                // isDeployedDmz
        values.add("0");                // hasUploadFunction
        values.add("");                  // pwdErrorFreezeMechanism
        values.add("0");                // hasOutboundRequest
        values.add("");                  // outboundRequestDesc
        values.add("");                  // uploadFunctionDesc
        values.add("");                  // uploadFilePath
        values.add("");                  // uploadPathExecutable
        values.add("");                  // uploadFileTypes
        values.add("0");                // hasDownloadFunction
        values.add("");                  // downloadFunctionDesc
        values.add("");                  // downloadFileTypes
        values.add("0");                // isAppApplication
        values.add("");                  // openSourceInfo
        values.add("http://test.example.com"); // accessUrl
        values.add("test.example.com");  // domainName
        values.add("B/S");              // networkMode
        values.add("浏览器");            // accessType
        values.add("");                  // vendorInfo
        values.add("");                  // authMethod
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
        values.add("");                 // internalUserScope
        values.add("");                 // serviceWindowDesc
        values.add("");                 // usageScopeDesc
        // 运维信息
        values.add("");                // cityRpo
        values.add("");                // cityRto
        values.add("");                 // cityActiveType
        values.add("0");               // hasCityEnvironment
        values.add("");                 // remoteRpo
        values.add("");                 // remoteRto
        values.add("");                 // remoteActiveType
        values.add("0");               // hasDrEnvironment
        values.add("C类");             // opsLevel
        values.add("");                // oldOpsLevel
        values.add("");                 // opsUnit
        values.add("");                 // remoteAccessClass
        values.add("0");               // isChangeAutomation
        values.add("");                 // changeDeployTime
        values.add("");                 // changeDeployTimeDesc
        values.add("");                 // mainBusinessHours
        values.add("");                 // dataAssetApprovalDept
        values.add("0");               // isContainerized
        values.add("测试环境");         // deploymentEnvironment
        values.add("");                 // deploymentLocation
        values.add("");                 // deploymentLocationDesc
        values.add("");                 // drLevel
        values.add("");                 // drRecoveryLevel

        return values.toArray(new String[0]);
    }
}