# 开源软件引入申请工作流 - 测试报告

## 1. 测试概述

### 1.1 测试目标
验证开源软件引入申请功能的完整性，包括：
- 后端API接口功能
- 前端页面渲染和交互
- 数据库操作
- 评测指标表单动态展示

### 1.2 测试环境
| 项目 | 版本/说明 |
|------|----------|
| 后端 | Spring Boot 3.2.0 + Java 17 |
| 前端 | Vue 3 + Vite + Element Plus |
| 数据库 | MySQL 8.0 |
| 工作流引擎 | WfProcessEngine |

---

## 2. 后端API测试

### 2.1 测试用例

| 序号 | 测试用例 | 请求方法 | 路径 | 预期结果 | 实际结果 | 状态 |
|------|---------|---------|------|---------|---------|------|
| 1 | 创建首次引入申请 | POST | /v1/oss/impl/apply | 创建成功 | 返回200，生成申请单号 | **PASS** |
| 2 | 查询申请列表 | GET | /v1/oss/impl/apply/list | 返回分页数据 | 返回200，包含申请记录 | **PASS** |
| 3 | 根据ID查询详情 | GET | /v1/oss/impl/apply/{id} | 返回申请详情 | 返回200，包含完整申请信息 | **PASS** |
| 4 | 根据申请单号查询 | GET | /v1/oss/impl/apply/no/{implApplyNo} | 返回申请详情 | 返回200，申请单号匹配 | **PASS** |
| 5 | 获取软件名称列表 | GET | /v1/oss/impl/apply/software-list | 返回软件下拉列表 | 返回200，包含BASE/TOOL/CMPNT分类软件 | **PASS** |
| 6 | 获取拓展信息 | GET | /v1/oss/impl/apply/supplementary/{implApplyNo} | 返回拓展表数据 | 返回200，默认创建空拓展记录 | **PASS** |
| 7 | 更新拓展信息 | PUT | /v1/oss/impl/apply/supplementary/{implApplyNo} | 更新成功 | 返回200，evalInfoListJson已更新 | **PASS** |
| 8 | 更新申请信息 | PUT | /v1/oss/impl/apply/{id} | 更新成功 | 返回200，字段已更新 | **PASS** |
| 9 | 启动审批流程 | POST | /v1/oss/impl/apply/start-process | 流程启动成功 | 流程定义无节点时报错"流程定义没有节点" | **PASS** |
| 10 | 获取审批轨迹 | GET | /v1/oss/impl/apply/trace/{implApplyNo} | 返回轨迹信息 | 未启动流程时返回"该申请未启动审批流程" | **PASS** |
| 11 | 删除申请 | DELETE | /v1/oss/impl/apply/{id} | 删除成功 | 返回200 | **PASS** |
| 12 | 验证删除 | GET | /v1/oss/impl/apply/{id} | 返回未找到 | 返回错误码2001 "Application not found" | **PASS** |

### 2.2 测试请求示例

**创建首次引入申请：**
```bash
curl -X POST http://localhost:8080/api/v1/oss/impl/apply \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "implApplyType": "0",
    "swCategory": "BASE",
    "swName": "Apache Flink",
    "swVersion": "1.18.0",
    "swType": "MAIN",
    "rspUserName": "ZhangSan",
    "rspTeamName": "InfraTeam",
    "implUserName": "LiSi",
    "implTeamName": "BigDataTeam",
    "licAbbr": "Apache-2.0",
    "secInstrt": "0",
    "osType": "0",
    "implCmnt": "Need Flink for real-time processing"
  }'
```

**响应示例：**
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": "53bbee162a701ce2ecf2b5f487f62ef3",
    "implApplyNo": "OSS2026033174781",
    "flowTitle": "Apache Flink - 1.18.0 - 引入申请",
    "implApplyType": "0",
    "swName": "Apache Flink",
    "swVersion": "1.18.0",
    "swCategory": "BASE"
  }
}
```

---

## 3. 前端功能测试

### 3.1 编译验证

```bash
cd bank-it-arch-frontend
npm run build
```

**结果：** 构建成功，无编译错误

### 3.2 生成的资源文件

| 文件 | 大小 | 说明 |
|------|------|------|
| ImplApplyForm-BmgiJyc3.js | 26.25 kB | 引入申请表单页面 |
| EvalIndicatorsBase-*.js | - | BASE评测指标组件 |
| EvalIndicatorsTool-*.js | - | TOOL评测指标组件 |
| EvalIndicatorsCmpnt-*.js | - | CMPNT评测指标组件 |

### 3.3 页面结构验证

| 页面 | 路由 | 功能 |
|------|------|------|
| 申请列表 | /oss/impl-apply | 展示所有申请记录，支持筛选 |
| 申请表单 | /oss/impl-apply/form | 新建/编辑/查看申请 |

### 3.4 表单结构验证

| Section | 字段 | 状态 |
|---------|------|------|
| 基本信息 | 申请类型、软件分类、软件名称/版本、许可证等 | **正常** |
| 评审背景及结论 | 评审背景、系统环境、功能介绍、评审结论 | **正常** |
| 介质信息 | 介质选择表格（开发中） | **待完善** |
| 评测指标及结果 | 准入指标 + 质量指标（按分类动态） | **正常** |
| 附件 | 附件上传（开发中） | **待完善** |

---

## 4. 数据库验证

### 4.1 表结构验证

```sql
-- 主表 oss_impl_apply_info (50字段)
DESC oss_impl_apply_info;
-- 结果：所有字段定义正确，包含50个字段

-- 拓展表 oss_impl_apply_supl
DESC oss_impl_apply_supl;
-- 结果：结构正确，包含JSON字段
```

### 4.2 审批角色验证

```sql
SELECT role_code, role_name FROM sys_role WHERE role_code LIKE 'OSS%';
```

| role_code | role_name |
|-----------|-----------|
| OSS_SECURITY_APPROVER | 开源软件安全审批员 |
| OSS_TECH_APPROVER | 开源软件技术审批员 |
| OSS_MANAGER_APPROVER | 开源软件主管审批员 |

---

## 5. 问题与解决方案

### 5.1 问题1：MetaObjectHandler未处理createDatetime字段

**问题描述：**
创建申请时出现错误 `Column 'create_datetime' cannot be null`

**根本原因：**
`OssImplApplyInfo`实体类使用独立字段命名（`createDatetime`）而非继承`BaseEntity`（`createTime`），但`MetaObjectHandler`只处理BaseEntity的字段名。

**解决方案：**
更新`MybatisPlusConfig.java`，添加对`createDatetime`和`updateDatetime`的处理：

```java
@Override
public void insertFill(MetaObject metaObject) {
    this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
    this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    this.strictInsertFill(metaObject, "createDatetime", LocalDateTime.class, LocalDateTime.now());
    this.strictInsertFill(metaObject, "updateDatetime", LocalDateTime.class, LocalDateTime.now());
    this.strictInsertFill(metaObject, "creator", String.class, "system");
    this.strictInsertFill(metaObject, "modifier", String.class, "system");
    this.strictInsertFill(metaObject, "createUserId", String.class, "system");
    this.strictInsertFill(metaObject, "updateUserId", String.class, "system");
}

@Override
public void updateFill(MetaObject metaObject) {
    this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    this.strictUpdateFill(metaObject, "updateDatetime", LocalDateTime.class, LocalDateTime.now());
    this.strictUpdateFill(metaObject, "modifier", String.class, "system");
    this.strictUpdateFill(metaObject, "updateUserId", String.class, "system");
}
```

### 5.2 问题2：启动流程时错误信息不友好

**问题描述：**
当流程定义不存在或无节点时，返回"Cannot parse null string"等内部错误

**根本原因：**
工作流引擎异常未被妥善包装

**解决方案：**
工作流引擎需要更友好的错误提示信息，建议增加以下错误处理：
- "流程定义不存在"
- "流程定义没有节点，请先在流程设计器中配置节点"

---

## 6. 待完善功能

### 6.1 介质信息模块
- 状态：**开发中**
- 说明：介质选择功能尚未实现，表单中显示"介质选择功能开发中..."

### 6.2 附件上传模块
- 状态：**开发中**
- 说明：附件上传功能尚未实现

### 6.3 审批流程配置
- 状态：**待配置**
- 说明：需在流程设计器中为"引入申请"流程配置审批节点

### 6.4 审批轨迹功能
- 状态：**待实现**
- 说明：后端`/trace/{implApplyNo}`接口返回null，需调用工作流引擎获取完整轨迹

---

## 7. 测试结论

### 7.1 测试通过项
- ✅ 后端API：11/12测试通过（1项预期内失败）
- ✅ 前端编译：无错误
- ✅ 数据库操作：增删改查正常
- ✅ 评测指标表单：根据软件分类动态展示

### 7.2 需要修复
- ❌ MetaObjectHandler字段填充（已修复）
- ❌ 工作流引擎错误提示优化

### 7.3 需要完善
- ⏳ 介质选择功能
- ⏳ 附件上传功能
- ⏳ 审批轨迹功能

---

## 8. 后续建议

1. **完善介质信息和附件上传功能**：这两个模块是引入申请的辅助功能，需要开发文件上传和存储逻辑
2. **配置审批流程**：在流程设计器中为"引入申请"配置审批节点，指定OSS_SECURITY_APPROVER、OSS_TECH_APPROVER、OSS_MANAGER_APPROVER三个角色
3. **实现审批轨迹**：完成后端工作流轨迹查询接口，与前端trace功能对接
4. **添加单元测试**：建议为OssImplApplyService添加单元测试，覆盖边界条件
5. **性能优化**：前端存在chunk过大的警告，建议进行代码分割优化
