# 使用台账 Excel 导入功能设计方案

## 1. 概述

实现使用台账（OssUseStandingBook）的 Excel 批量导入功能。用户上传 Excel 文件，系统解析后按 `(swName, swVersion, appNo)` 分组，先删后写，将数据分别存入 main 表和 detail 表。

## 2. 业务逻辑

### 2.1 导入流程

```
Excel文件 → 解析 → 按(swName, swVersion, appNo)分组
         → 硬删除匹配数据 → 每组1条main + N条detail
```

### 2.2 分组与写入规则

- **分组 Key**：`swName + swVersion + appNo`
- **删除**：根据分组 Key 硬删除 main 表和 detail 表中所有匹配数据
- **Main 记录**：每组生成 1 条，冗余字段取每组第一行值
- **Detail 记录**：每组内所有 Excel 行均生成 1 条 detail

### 2.3 字段归属

| 字段 | 存 main | 存 detail |
|------|---------|-----------|
| swName | ✓ | ✓ |
| swVersion | ✓ | ✓ |
| swFullName | ✓ | ✓ |
| licAbbr | ✓ | ✓ |
| environment | ✓ | ✓ |
| swCategory | ✓ | ✓ |
| appNo | ✓ | ✓ |
| appName | ✓ | ✓ |
| installPath | - | ✓ |
| source | - | ✓ |
| scanTime | - | ✓ |
| ipOrHostName | - | ✓ |
| command | - | ✓ |
| detailedInfo | - | ✓ |
| fileType | - | ✓ |
| dependType | - | ✓ |
| isCommerc | - | ✓ |
| commercProductName | - | ✓ |
| commercProductVersion | - | ✓ |
| projectName | - | ✓ |

### 2.4 系统自动填充字段

| 字段 | 填充值 |
|------|--------|
| main.id | UUID |
| main.scanDate | `LocalDate.now()` |
| main.syncDatetime | `LocalDateTime.now()` |
| main.createMode | `0`（本系统创建） |
| main.logicStatus | `0` |
| main.createDatetime | 自动填充（MetaObjectHandler） |
| main.updateDatetime | 自动填充 |
| detail.id | UUID |
| detail.parentId | 关联 main.id |
| detail.syncDatetime | `LocalDateTime.now()` |
| detail.createMode | `0` |
| detail.logicStatus | `0` |
| detail.createDatetime | 自动填充 |
| detail.updateDatetime | 自动填充 |

## 3. 接口设计

### 3.1 下载模板

已预先放置于 `bank-it-arch-backend/src/main/resources/template/oss_use_standing_book_template.xlsx`，无需接口。

### 3.2 导入接口

```
POST /v1/oss/standing-book/main/import
Content-Type: multipart/form-data

参数：file (Excel 文件)

响应：
{
  "code": 200,
  "message": "导入成功",
  "data": {
    "total": 100,      // 总行数
    "success": 98,     // 成功行数
    "failed": 2,       // 失败行数
    "errors": [        // 失败详情（可选）
      { "row": 5, "reason": "swName 不能为空" }
    ]
  }
}
```

## 4. 后端改动

### 4.1 新增 Service 方法

**OssUseStandingBookMainService:**
```java
ImportResult importExcel(MultipartFile file);
```

### 4.2 新增 DTO

```java
// 用于接收 Excel 行数据
public class OssUseStandingBookImportDTO {
    private String swName;           // 开源软件名称
    private String swVersion;        // 开源软件版本
    private String swFullName;        // 开源软件全称
    private String licAbbr;          // 开源许可证集合
    private String environment;      // 所属环境
    private String swCategory;       // 软件分类
    private String appNo;            // 应用编号
    private String appName;          // 应用全称
    private String installPath;       // 项目路径/安装路径
    private Integer source;          // 台账来源
    private LocalDateTime scanTime;  // 扫描时间
    private String ipOrHostName;     // IP/主机名称
    private String command;          // 软件启动命令
    private String detailedInfo;     // 版本详细信息
    private String fileType;         // 组件文件类型
    private String dependType;       // 组件依赖类型
    private String isCommerc;       // 是否商用
    private String commercProductName; // 产品名称
    private String commercProductVersion; // 产品版本
    private String projectName;      // 项目名
}
```

### 4.3 异常处理

- 格式校验失败（表头不匹配、列数错误）→ 400 错误
- 空值行（swName/swVersion/appNo 任一为空）→ 跳过并记录
- 其他系统异常 → 500 错误

## 5. 前端改动

### 5.1 页面按钮

在 `StdBook.vue` 列表页新增：
- **导入按钮**：上传 Excel 文件，调用 `/main/import`
- **导入结果提示**：成功/失败条数弹窗

### 5.2 API

```javascript
importMain: (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/oss/standing-book/main/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
```

## 6. 模板文件

路径：`bank-it-arch-backend/src/main/resources/template/oss_use_standing_book_template.xlsx`

包含 18 个字段列 + 3 条测试数据。

## 7. 数据库操作（硬删除）

```sql
-- 按 swName + swVersion + appNo 删除 detail 表
DELETE FROM oss_use_standing_book_details_info
WHERE swName = ? AND swVersion = ? AND appNo = ?;

-- 按 swName + swVersion + appNo 删除 main 表
DELETE FROM oss_use_standing_book_main_info
WHERE swName = ? AND swVersion = ? AND appNo = ?;
```

## 8. 测试数据

模板包含 3 条测试行：

| swName | swVersion | appNo | swCategory | installPath | scanTime | ipOrHostName |
|--------|-----------|-------|------------|-------------|----------|--------------|
| MySQL | 8.0 | APP001 | 开源基础软件 | /opt/mysql | 2026-04-01 10:00:00 | 192.168.1.100 |
| Redis | 6.0 | APP001 | 开源基础软件 | /opt/redis | 2026-04-01 11:00:00 | 192.168.1.101 |
| SpringBoot | 2.7 | APP002 | 开源组件框架 | /home/app/springboot | 2026-04-01 12:00:00 | 192.168.1.102 |
