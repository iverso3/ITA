# 使用台账 Excel 导入功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现使用台账 Excel 批量导入功能，用户上传 Excel，按 `(swName, swVersion, appNo)` 分组先删后写，数据分别存入 main 表和 detail 表。

**Architecture:** 后端新增 `importExcel` Service 方法和 `/main/import` Controller 接口，使用 Apache POI 解析 Excel，按分组 Key 硬删除后写入。前端新增导入按钮，调用接口并展示结果。

**Tech Stack:** Spring Boot + MyBatis-Plus + Apache POI (已引入), Vue 3 + Element Plus + Axios

---

## 文件清单

| 操作 | 文件 |
|------|------|
| 新建 | `bank-it-arch-backend/src/main/java/com/bank/itarch/model/dto/OssUseStandingBookImportDTO.java` |
| 新建 | `bank-it-arch-backend/src/main/java/com/bank/itarch/model/dto/ImportResult.java` |
| 修改 | `bank-it-arch-backend/src/main/java/com/bank/itarch/service/OssUseStandingBookMainService.java` |
| 修改 | `bank-it-arch-backend/src/main/java/com/bank/itarch/controller/OssUseStandingBookController.java` |
| 修改 | `bank-it-arch-backend/src/main/java/com/bank/itarch/mapper/OssUseStandingBookDetailsMapper.java` |
| 新建 | `bank-it-arch-backend/src/main/resources/mapper/OssUseStandingBookDetailsMapper.xml` |
| 修改 | `bank-it-arch-frontend/src/api/index.js` |
| 修改 | `bank-it-arch-frontend/src/views/oss/StdBook.vue` |

---

## Task 1: 创建 ImportDTO

**文件:** 创建 `bank-it-arch-backend/src/main/java/com/bank/itarch/model/dto/OssUseStandingBookImportDTO.java`

- [ ] **Step 1: 创建文件**

```java
package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OssUseStandingBookImportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String swName;                     // 开源软件名称
    private String swVersion;                  // 开源软件版本
    private String swFullName;                 // 开源软件全称
    private String licAbbr;                    // 开源许可证集合
    private String environment;                // 所属环境
    private String swCategory;                 // 软件分类
    private String appNo;                      // 应用编号
    private String appName;                    // 应用全称
    private String installPath;                // 项目路径/安装路径
    private Integer source;                     // 台账来源
    private LocalDateTime scanTime;            // 扫描时间
    private String ipOrHostName;               // IP/主机名称
    private String command;                    // 软件启动命令
    private String detailedInfo;               // 版本详细信息
    private String fileType;                   // 组件文件类型
    private String dependType;                 // 组件依赖类型
    private String isCommerc;                  // 是否商用
    private String commercProductName;         // 产品名称
    private String commercProductVersion;       // 产品版本
    private String projectName;                // 项目名
}
```

- [ ] **Step 2: 提交**

```bash
git add bank-it-arch-backend/src/main/java/com/bank/itarch/model/dto/OssUseStandingBookImportDTO.java
git commit -m "feat(oss-standing-book): add OssUseStandingBookImportDTO for Excel import"
```

---

## Task 2: 创建 ImportResult DTO

**文件:** 创建 `bank-it-arch-backend/src/main/java/com/bank/itarch/model/dto/ImportResult.java`

- [ ] **Step 1: 创建文件**

```java
package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer total;        // 总行数（不含表头）
    private Integer success;      // 成功行数
    private Integer failed;       // 失败行数
    private List<ImportError> errors = new ArrayList<>();

    @Data
    public static class ImportError implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer row;      // 行号（从1开始，表头为0）
        private String reason;     // 失败原因

        public ImportError(Integer row, String reason) {
            this.row = row;
            this.reason = reason;
        }
    }

    public static ImportResult ok(int total, int success) {
        ImportResult result = new ImportResult();
        result.setTotal(total);
        result.setSuccess(success);
        result.setFailed(total - success);
        return result;
    }

    public static ImportResult error(int total, List<ImportError> errors) {
        ImportResult result = new ImportResult();
        result.setTotal(total);
        result.setFailed(errors.size());
        result.setSuccess(total - errors.size());
        result.setErrors(errors);
        return result;
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add bank-it-arch-backend/src/main/java/com/bank/itarch/model/dto/ImportResult.java
git commit -m "feat(oss-standing-book): add ImportResult DTO"
```

---

## Task 3: Mapper 硬删除方法

**文件:**
- 修改 `bank-it-arch-backend/src/main/java/com/bank/itarch/mapper/OssUseStandingBookDetailsMapper.java`
- 新建 `bank-it-arch-backend/src/main/resources/mapper/OssUseStandingBookDetailsMapper.xml`

- [ ] **Step 1: 修改 Mapper 接口，添加硬删除方法**

```java
package com.bank.itarch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bank.itarch.model.entity.OssUseStandingBookDetails;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OssUseStandingBookDetailsMapper extends BaseMapper<OssUseStandingBookDetails> {

    /**
     * 根据 swName + swVersion + appNo 硬删除 detail 表数据
     */
    @Select("DELETE FROM oss_use_standing_book_details_info WHERE sw_name = #{swName} AND sw_version = #{swVersion} AND app_no = #{appNo}")
    int hardDeleteBySwKey(@Param("swName") String swName, @Param("swVersion") String swVersion, @Param("appNo") String appNo);
}
```

- [ ] **Step 2: 提交**

```bash
git add bank-it-arch-backend/src/main/java/com/bank/itarch/mapper/OssUseStandingBookDetailsMapper.java
git commit -m "feat(oss-standing-book): add hardDeleteBySwKey to details mapper"
```

---

## Task 4: Service 层 importExcel 方法

**文件:** 修改 `bank-it-arch-backend/src/main/java/com/bank/itarch/service/OssUseStandingBookMainService.java`

- [ ] **Step 1: 添加 importExcel 方法**

在 `OssUseStandingBookMainService` 类中添加以下方法（放在 `listAllForExport` 方法之后）：

```java
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
```

添加方法：

```java
@Transactional
public ImportResult importExcel(MultipartFile file) {
    // 1. 解析 Excel
    List<OssUseStandingBookImportDTO> rows;
    try {
        rows = parseExcel(file);
    } catch (Exception e) {
        throw new BusinessException(400, "Excel解析失败: " + e.getMessage());
    }

    // 2. 校验空行
    List<ImportResult.ImportError> errors = new ArrayList<>();
    List<OssUseStandingBookImportDTO> validRows = new ArrayList<>();
    for (int i = 0; i < rows.size(); i++) {
        OssUseStandingBookImportDTO row = rows.get(i);
        if (!StringUtils.hasText(row.getSwName()) || !StringUtils.hasText(row.getSwVersion()) || !StringUtils.hasText(row.getAppNo())) {
            errors.add(new ImportResult.ImportError(i + 2, "swName/swVersion/appNo 不能为空"));
        } else {
            validRows.add(row);
        }
    }

    // 3. 按 (swName, swVersion, appNo) 分组
    Map<String, List<OssUseStandingBookImportDTO>> groupMap = validRows.stream()
            .collect(Collectors.groupingBy(r -> r.getSwName() + "|" + r.getSwVersion() + "|" + r.getAppNo()));

    // 4. 先删后写
    for (Map.Entry<String, List<OssUseStandingBookImportDTO>> entry : groupMap.entrySet()) {
        List<OssUseStandingBookImportDTO> groupRows = entry.getValue();
        OssUseStandingBookImportDTO firstRow = groupRows.get(0);
        String swName = firstRow.getSwName();
        String swVersion = firstRow.getSwVersion();
        String appNo = firstRow.getAppNo();

        // 硬删除 detail 表
        detailsMapper.hardDeleteBySwKey(swName, swVersion, appNo);
        // 硬删除 main 表（根据 swName + swVersion + appNo）
        LambdaQueryWrapper<OssUseStandingBookMain> mainWrapper = new LambdaQueryWrapper<>();
        mainWrapper.eq(OssUseStandingBookMain::getSwName, swName)
                   .eq(OssUseStandingBookMain::getSwVersion, swVersion)
                   .eq(OssUseStandingBookMain::getAppNo, appNo);
        baseMapper.delete(mainWrapper);

        // 创建 main 记录
        OssUseStandingBookMain main = new OssUseStandingBookMain();
        main.setId(UUID.randomUUID().toString());
        main.setSwName(swName);
        main.setSwVersion(swVersion);
        main.setSwFullName(firstRow.getSwFullName());
        main.setLicAbbr(firstRow.getLicAbbr());
        main.setEnvironment(firstRow.getEnvironment());
        main.setSwCategory(firstRow.getSwCategory());
        main.setAppNo(appNo);
        main.setAppName(firstRow.getAppName());
        main.setScanDate(LocalDate.now());
        main.setSyncDatetime(LocalDateTime.now());
        main.setCreateMode(0);
        main.setLogicStatus(0);
        main.setIsCommerc(firstRow.getIsCommerc());
        baseMapper.insert(main);

        // 创建 detail 记录（每行一条）
        for (OssUseStandingBookImportDTO row : groupRows) {
            OssUseStandingBookDetails detail = new OssUseStandingBookDetails();
            detail.setId(UUID.randomUUID().toString());
            detail.setParentId(main.getId());
            detail.setSwName(swName);
            detail.setSwVersion(swVersion);
            detail.setSwFullName(row.getSwFullName());
            detail.setLicAbbr(row.getLicAbbr());
            detail.setEnvironment(row.getEnvironment());
            detail.setSwCategory(row.getSwCategory());
            detail.setAppNo(appNo);
            detail.setAppName(row.getAppName());
            detail.setInstallPath(row.getInstallPath());
            detail.setSource(row.getSource() != null ? row.getSource() : 1);
            detail.setScanTime(row.getScanTime());
            detail.setIpOrHostName(row.getIpOrHostName());
            detail.setCommand(row.getCommand());
            detail.setDetailedInfo(row.getDetailedInfo());
            detail.setFileType(row.getFileType());
            detail.setDependType(row.getDependType());
            detail.setSyncDatetime(LocalDateTime.now());
            detail.setCreateMode(0);
            detail.setLogicStatus(0);
            detail.setIsCommerc(row.getIsCommerc());
            detail.setCommercProductName(row.getCommercProductName());
            detail.setCommercProductVersion(row.getCommercProductVersion());
            detail.setProjectName(row.getProjectName());
            detailsMapper.insert(detail);
        }
    }

    if (!errors.isEmpty()) {
        return ImportResult.error(rows.size(), errors);
    }
    return ImportResult.ok(rows.size(), validRows.size());
}

private List<OssUseStandingBookImportDTO> parseExcel(MultipartFile file) throws Exception {
    List<OssUseStandingBookImportDTO> rows = new ArrayList<>();
    try (InputStream is = file.getInputStream();
         Workbook wb = new XSSFWorkbook(is)) {
        Sheet sheet = wb.getSheetAt(0);
        // 校验表头
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new BusinessException(400, "Excel表头不能为空");
        }
        // 表头列索引映射
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                headerMap.put(cell.getStringCellValue().trim(), i);
            }
        }
        String[] requiredHeaders = {"开源软件名称", "开源软件版本", "应用编号"};
        for (String h : requiredHeaders) {
            if (!headerMap.containsKey(h)) {
                throw new BusinessException(400, "缺少必需列: " + h);
            }
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            OssUseStandingBookImportDTO dto = new OssUseStandingBookImportDTO();
            dto.setSwName(getCellValue(row, headerMap.get("开源软件名称")));
            dto.setSwVersion(getCellValue(row, headerMap.get("开源软件版本")));
            dto.setSwFullName(getCellValue(row, headerMap.get("开源软件全称")));
            dto.setLicAbbr(getCellValue(row, headerMap.get("开源许可证集合")));
            dto.setEnvironment(getCellValue(row, headerMap.get("所属环境")));
            dto.setSwCategory(getCellValue(row, headerMap.get("软件分类")));
            dto.setAppNo(getCellValue(row, headerMap.get("应用编号")));
            dto.setAppName(getCellValue(row, headerMap.get("应用全称")));
            dto.setInstallPath(getCellValue(row, headerMap.get("项目路径/安装路径")));
            // source 列
            Integer source = null;
            if (headerMap.containsKey("台账来源")) {
                Cell cell = row.getCell(headerMap.get("台账来源"));
                if (cell != null) {
                    try { source = (int) cell.getNumericCellValue(); } catch (Exception e) { /* ignore */ }
                }
            }
            dto.setSource(source);
            // scanTime
            LocalDateTime scanTime = null;
            if (headerMap.containsKey("扫描时间")) {
                Cell cell = row.getCell(headerMap.get("扫描时间"));
                if (cell != null) {
                    try {
                        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                            scanTime = cell.getLocalDateTimeCellValue();
                        } else {
                            String val = getCellValue(row, headerMap.get("扫描时间"));
                            if (StringUtils.hasText(val)) {
                                scanTime = LocalDateTime.parse(val, dtf);
                            }
                        }
                    } catch (Exception e) { /* ignore */ }
                }
            }
            dto.setScanTime(scanTime);
            dto.setIpOrHostName(getCellValue(row, headerMap.get("IP/主机名称")));
            dto.setCommand(getCellValue(row, headerMap.get("软件启动命令")));
            dto.setDetailedInfo(getCellValue(row, headerMap.get("版本详细信息")));
            dto.setFileType(getCellValue(row, headerMap.get("组件文件类型")));
            dto.setDependType(getCellValue(row, headerMap.get("组件依赖类型")));
            dto.setIsCommerc(getCellValue(row, headerMap.get("是否商用")));
            dto.setCommercProductName(getCellValue(row, headerMap.get("产品名称")));
            dto.setCommercProductVersion(getCellValue(row, headerMap.get("产品版本")));
            dto.setProjectName(getCellValue(row, headerMap.get("项目名")));
            rows.add(dto);
        }
    }
    return rows;
}

private String getCellValue(Row row, Integer colIndex) {
    if (colIndex == null) return null;
    Cell cell = row.getCell(colIndex);
    if (cell == null) return null;
    switch (cell.getCellType()) {
        case STRING: return cell.getStringCellValue().trim();
        case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
        case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
        case FORMULA: return cell.getCellFormula();
        default: return null;
    }
}
```

同时在文件顶部添加以下 import：

```java
import com.bank.itarch.model.dto.ImportResult;
import com.bank.itarch.model.entity.OssUseStandingBookDetails;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
```

- [ ] **Step 2: 提交**

```bash
git add bank-it-arch-backend/src/main/java/com/bank/itarch/service/OssUseStandingBookMainService.java
git commit -m "feat(oss-standing-book): implement importExcel in main service"
```

---

## Task 5: Controller 层导入接口

**文件:** 修改 `bank-it-arch-backend/src/main/java/com/bank/itarch/controller/OssUseStandingBookController.java`

- [ ] **Step 1: 添加 importExcel 接口**

在 `OssUseStandingBookController` 类的 `importMain` 方法添加到 main 接口区域：

```java
import com.bank.itarch.model.dto.ImportResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
```

在 main 接口区域（`exportMain` 方法之后，`details` 接口区域之前）添加：

```java
@PostMapping("/main/import")
@Operation(summary = "导入台账主列表")
public Result<ImportResult> importMain(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
        return Result.error("请选择要上传的Excel文件");
    }
    String filename = file.getOriginalFilename();
    if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
        return Result.error("仅支持 .xlsx 或 .xls 格式的Excel文件");
    }
    ImportResult result = mainService.importExcel(file);
    if (result.getFailed() > 0) {
        return Result.success("导入完成，部分数据失败", result);
    }
    return Result.success("导入成功", result);
}
```

- [ ] **Step 2: 提交**

```bash
git add bank-it-arch-backend/src/main/java/com/bank/itarch/controller/OssUseStandingBookController.java
git commit -m "feat(oss-standing-book): add /main/import endpoint"
```

---

## Task 6: 前端 API

**文件:** 修改 `bank-it-arch-frontend/src/api/index.js`

- [ ] **Step 1: 在 ossUseStandingBookApi 中添加 importMain 方法**

在 `ossUseStandingBookApi` 对象内，在 `exportMain` 之后添加：

```javascript
importMain: (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/oss/standing-book/main/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
},
```

完整 `ossUseStandingBookApi` 变为：

```javascript
export const ossUseStandingBookApi = {
  // Main表
  listMain: (params) => api.get('/oss/standing-book/main/list', { params }),
  getMainById: (id) => api.get(`/oss/standing-book/main/${id}`),
  createMain: (data) => api.post('/oss/standing-book/main', data),
  updateMain: (id, data) => api.put(`/oss/standing-book/main/${id}`, data),
  deleteMain: (id) => api.delete(`/oss/standing-book/main/${id}`),
  exportMain: (params) => api.get('/oss/standing-book/main/export', { params }),
  importMain: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/oss/standing-book/main/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  // Detail表
  listDetails: (params) => api.get('/oss/standing-book/details', { params }),
  getDetailById: (id) => api.get(`/oss/standing-book/details/${id}`),
  createDetail: (data) => api.post('/oss/standing-book/details', data),
  updateDetail: (id, data) => api.put(`/oss/standing-book/details/${id}`, data),
  deleteDetail: (id) => api.delete(`/oss/standing-book/details/${id}`)
}
```

- [ ] **Step 2: 提交**

```bash
git add bank-it-arch-frontend/src/api/index.js
git commit -m "feat(oss-standing-book): add importMain API"
```

---

## Task 7: 前端导入功能

**文件:** 修改 `bank-it-arch-frontend/src/views/oss/StdBook.vue`

- [ ] **Step 1: 添加导入相关状态**

在 `<script setup>` 区域内，添加：

```javascript
const importLoading = ref(false)
const importRef = ref(null)  // 用于触发 file input
const importFile = ref(null)
```

- [ ] **Step 2: 添加导入处理函数**

在 `loadDetails` 函数之后添加：

```javascript
function handleImportClick() {
  importFile.value = null
  importRef.value?.click()
}

async function handleImportChange(e) {
  const file = e.target.files[0]
  if (!file) return
  importLoading.value = true
  try {
    const res = await ossUseStandingBookApi.importMain(file)
    const data = res.data || {}
    const msg = `导入完成：共 ${data.total || 0} 行，成功 ${data.success || 0} 行，失败 ${data.failed || 0} 行`
    if (data.failed > 0 && data.errors && data.errors.length > 0) {
      const errList = data.errors.slice(0, 5).map(e => `第${e.row}行: ${e.reason}`).join('\n')
      ElMessage.warning({ message: msg + '\n' + errList, duration: 0, showClose: true })
    } else {
      ElMessage.success(msg)
    }
    await loadData()
  } catch (e) {
    ElMessage.error(e?.message || '导入失败')
  } finally {
    importLoading.value = false
    e.target.value = ''
  }
}
```

- [ ] **Step 3: 修改导入按钮**

将原来的：
```html
<el-button type="danger" plain>导入</el-button>
```

改为：
```html
<el-button type="danger" plain :loading="importLoading" @click="handleImportClick">导入</el-button>
<input ref="importRef" type="file" accept=".xlsx,.xls" style="display:none" @change="handleImportChange" />
```

- [ ] **Step 4: 提交**

```bash
git add bank-it-arch-frontend/src/views/oss/StdBook.vue
git commit -m "feat(oss-standing-book): add import button to StdBook page"
```

---

## Task 8: 编译验证

- [ ] **Step 1: 后端编译**

```bash
cd bank-it-arch-backend && mvn compile -q
```

期望：无错误输出

- [ ] **Step 2: 前端编译**

```bash
cd bank-it-arch-frontend && npm run build 2>&1 | tail -20
```

期望：无错误

- [ ] **Step 3: 提交所有更改**

```bash
git add -A && git commit -m "feat: complete oss standing book Excel import feature"
```

---

## Task 9: 自测

启动后端和前端服务，使用 Excel 模板进行导入测试：

1. 启动后端：`cd bank-it-arch-backend && mvn spring-boot:run`
2. 启动前端：`cd bank-it-arch-frontend && npm run dev`
3. 访问 http://localhost:5173 → 使用台账管理页面
4. 点击「导入」按钮，选择 `oss_use_standing_book_template.xlsx`
5. 验证：
   - main 表中生成 2 条记录（MySQL+Redis 同 APP001，SpringBoot 单独）
   - detail 表中生成 3 条记录
   - 再次导入同一文件，验证先删后写（记录数不变）

---

## 自检清单

- [ ] 所有 DTO 类字段名与 entity 一致
- [ ] `swName/swVersion/appNo` 任一为空时跳过该行并记录
- [ ] 硬删除使用自定义 SQL，不走逻辑删除
- [ ] 分组后 main 只取第一条的冗余字段
- [ ] detail 每行都写入
- [ ] source 默认为 1（手工上传）
- [ ] controller 文件名校验
- [ ] 前端 FormData 上传，Content-Type 为 multipart/form-data
