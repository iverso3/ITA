# 开源软件引入申请工作流 - 设计文档

## 1. 功能概述

### 1.1 业务背景
银行IT架构管理平台需要支持开源软件引入申请功能，实现从申请、评测到审批的全流程管理。

### 1.2 主要功能
- **申请提交**：用户可提交开源软件引入申请（首次引入或新版本引入）
- **评测指标**：根据软件分类展示不同的评测指标表单
- **审批流程**：通过可视化流程设计器配置审批节点，设定审批角色
- **流程追踪**：查看审批进度和历史轨迹

### 1.3 软件分类
| 分类代码 | 分类名称 | 说明 |
|---------|---------|------|
| BASE | 开源基础软件 | 基础架构类软件，如Flink、Kafka等 |
| TOOL | 开源工具软件 | 开发运维工具，如LibreOffice、Node.js等 |
| CMPNT | 开源组件 | 组件框架，如Milvus等 |

### 1.4 申请类型
| 类型代码 | 类型名称 | 说明 |
|---------|---------|------|
| 0 | 首次引入 | 新软件首次引入，需填写完整信息 |
| 1 | 新版本引入 | 从已入库软件中选择版本 |

---

## 2. 数据库设计

### 2.1 主表 `oss_impl_apply_info`

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | VARCHAR(32) | 主键 |
| impl_apply_no | VARCHAR(32) | 申请单号 |
| flow_title | VARCHAR(64) | 流程标题 |
| impl_apply_type | VARCHAR(8) | 申请类型：0-首次引入，1-新版本引入 |
| sw_id | VARCHAR(32) | 开源软件ID（关联oss_software_info） |
| sw_name | VARCHAR(64) | 开源软件名称 |
| sw_version | VARCHAR(24) | 开源软件版本 |
| sw_type | VARCHAR(12) | 软件类型：MAIN-主推，LIMIT-限制使用，QUIT-已退出 |
| rsp_user_id | VARCHAR(32) | 责任人ID |
| rsp_user_name | VARCHAR(100) | 责任人名称 |
| rsp_team_id | VARCHAR(32) | 责任团队ID |
| rsp_team_name | VARCHAR(128) | 责任团队名称 |
| sw_category | VARCHAR(12) | 软件分类：BASE/TOOL/CMPNT |
| impl_team_id | VARCHAR(32) | 引入团队ID |
| impl_team_name | VARCHAR(128) | 引入团队名称 |
| impl_user_id | VARCHAR(32) | 引入人ID |
| impl_user_name | VARCHAR(100) | 引入人名称 |
| lic_id | VARCHAR(256) | 开源许可协议ID |
| lic_abbr | VARCHAR(256) | 开源许可协议简称 |
| use_branch_id | VARCHAR(32) | 软件使用机构ID |
| use_branch_name | VARCHAR(128) | 软件使用机构名称 |
| sec_instrt | VARCHAR(8) | 是否安全工具相关：0-否，1-是 |
| os_type | VARCHAR(64) | 操作系统类型 |
| os_version | VARCHAR(64) | 操作系统版本 |
| os_digit | VARCHAR(32) | 操作系统位数 |
| apply_team_id | VARCHAR(32) | 申请研发团队ID |
| apply_team_name | VARCHAR(128) | 申请研发团队名称 |
| use_app_no | VARCHAR(32) | 应用编号 |
| launch_version | VARCHAR(32) | 投产版本 |
| launch_task_info | VARCHAR(100) | 任务编号及名称 |
| impl_cmnt | VARCHAR(200) | 申请说明 |
| contact_user_id | VARCHAR(32) | 联系人ID |
| contact_user_name | VARCHAR(100) | 联系人名称 |
| contact_tel_no | VARCHAR(32) | 联系人电话 |
| eval_background | TEXT | 评审背景 |
| system_env | TEXT | 系统环境 |
| function_intro | TEXT | 功能介绍 |
| eval_conclusion | TEXT | 评审结论 |
| approve_datetime | DATETIME | 审批时间 |
| proc_inst_id | BIGINT | 流程实例ID（关联wf_instance） |
| remark | VARCHAR(200) | 备注 |
| sync_datetime | DATETIME | 同步时间 |
| create_mode | INT | 创建方式：0-本系统创建，1-异地同步 |
| create_user_id | VARCHAR(32) | 创建者 |
| create_datetime | DATETIME | 创建时间 |
| update_user_id | VARCHAR(32) | 更新者 |
| update_datetime | DATETIME | 更新时间 |
| logic_status | INT | 逻辑状态：0-正常，1-删除 |

### 2.2 拓展表 `oss_impl_apply_supl`

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | VARCHAR(32) | 主键 |
| impl_apply_no | VARCHAR(32) | 申请单号 |
| eval_info_list_json | JSON | 评测信息列表 |
| eval_result_list_json | JSON | 评测结果列表 |
| eval_score_list_json | JSON | 评测得分列表 |
| eval_summ_list_json | JSON | 评测总结列表 |
| eval_atch_list_json | JSON | 评测附件 |
| media_pre_whs_url | JSON | 介质前置仓库地址 |
| proc_inst_id | BIGINT | 流程实例ID |

---

## 3. API接口设计

### 3.1 基础路径
`/api/v1/oss/impl/apply`

### 3.2 接口列表

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /list | 申请列表（分页） |
| GET | /{id} | 根据ID获取申请详情 |
| GET | /no/{implApplyNo} | 根据申请单号获取详情 |
| POST | / | 创建申请 |
| PUT | /{id} | 更新申请 |
| DELETE | /{id} | 删除申请 |
| GET | /software-list | 获取软件名称列表（用于新版本引入） |
| GET | /supplementary/{implApplyNo} | 获取拓展信息 |
| PUT | /supplementary/{implApplyNo} | 更新拓展信息 |
| POST | /start-process | 启动审批流程 |
| GET | /trace/{implApplyNo} | 获取审批轨迹 |

---

## 4. 前端页面设计

### 4.1 页面列表
| 页面 | 路径 | 说明 |
|------|------|------|
| 申请列表 | /oss/impl-apply | 展示所有申请记录 |
| 申请表单 | /oss/impl-apply/form | 新建/编辑/查看申请 |

### 4.2 申请表单结构

**Section 1: 基本信息**
- 申请类型（首次引入/新版本引入）
- 软件分类
- 开源软件名称
- 开源软件版本
- 开源许可协议
- 是否安全工具
- 操作系统类型
- 应用编号
- 投产版本
- 任务编号及名称
- 申请说明

**Section 2: 评审背景及结论**
- 评审背景
- 系统环境
- 功能介绍
- 评审结论

**Section 3: 介质信息**
- 介质选择表格

**Section 4: 评测指标及结果**
- 准入指标（所有类型通用）
- 质量指标（按软件分类不同）

**Section 5: 附件**
- 附件上传

---

## 5. 评测指标设计

### 5.1 准入指标（门槛性，不计入质量分）

| 一级指标 | 二级指标 | 评测标准 | 评测结果 | 交互 |
|---------|---------|---------|---------|------|
| 许可证准入 | 开源许可证准入情况 | 当前版本许可证属于准入许可证 | 不可选（仅展示） | 仅展示 |
| 安全风险准入 | 已暴露最高安全漏洞风险等级 | 当前版本无高危及以上漏洞 | 下拉：无高危/有高危 | 选"有高危"弹框警告 |
| | 软件涉恶意代码情况 | 当前版本未发现病毒或木马 | 下拉：未发现/有发现 | 选"有发现"弹框警告 |

### 5.2 质量指标

#### 开源基础软件（BASE）- 满分100分

| 一级指标 | 二级指标 | 分值权重 |
|---------|---------|---------|
| 行业认可度 | 同业使用的数量 | 15 |
| 产品活跃度 | 版本发布周期间隔 | 3 |
| | 代码提交次数 | 3 |
| | GitHub贡献者数量 | 3 |
| | GitHub Stars | 3 |
| | GitHub Forks | 3 |
| | GitHub Watching | 3 |
| 服务支持 | 商业支持情况 | 3 |
| | 官方或社区文档情况 | 4 |
| 功能特性 | 核心功能满足度 | 15 |
| | 扩展功能满足度 | 5 |
| 安全性 | 高危及以上漏洞数量 | 5 |
| | 中危漏洞数量 | 3 |
| | 低危漏洞数量 | 3 |
| | 安全漏洞修复积极性 | 3 |
| 可靠性 | 容错能力 | 2 |
| | 数据恢复能力 | 2 |
| 兼容性 | 国产硬件兼容 | 2 |
| | 国产OS兼容 | 2 |
| 性能效率 | 资源使用率 | 5 |
| 可维护性 | 在线变更能力 | 2 |
| | 运行时监控 | 2 |
| 可扩展性 | 水平扩展 | 2 |
| | 动态扩展 | 2 |
| 易用性 | 应用示例状态 | 2 |
| | 安装部署难易度 | 3 |

#### 开源工具软件（TOOL）- 满分100分

| 一级指标 | 二级指标 | 分值权重 |
|---------|---------|---------|
| 功能特性 | 核心功能满足度 | 40 |
| | 扩展功能满足度 | 10 |
| 安全性 | 高危及以上漏洞数量 | 15 |
| | 中危漏洞数量 | 10 |
| | 低危漏洞数量 | 5 |
| | 安全漏洞修复积极性 | 5 |
| 易用性 | 安装部署难易程度 | 15 |

#### 开源组件框架（CMPNT）- 满分100分

| 一级指标 | 二级指标 | 分值权重 | 评测结果枚举 |
|---------|---------|---------|------------|
| 功能特性 | 核心功能满足度 | 100 | 完全不满足(0)/20%满足(20)/40%满足(40)/60%满足(60)/80%满足(80)/100%满足(100) |

---

## 6. 工作流设计指南

### 6.1 审批角色

| 角色编码 | 角色名称 | 说明 |
|---------|---------|------|
| OSS_SECURITY_APPROVER | 开源软件安全审批员 | 安全审批 |
| OSS_TECH_APPROVER | 开源软件技术审批员 | 技术审批 |
| OSS_MANAGER_APPROVER | 开源软件主管审批员 | 主管审批 |

### 6.2 流程设计步骤

1. **进入流程设计器**：系统管理 → 流程管理 → 新建流程
2. **绘制流程图**：
   - 从左侧拖拽节点到画布
   - 连接节点形成流程
3. **配置节点属性**：
   - 选择节点类型（开始/审批/结束等）
   - 设定审批角色
   - 设置审批方式（单人/会签/或签等）
4. **发布流程**：保存并发布流程定义
5. **启动审批**：在申请表单中提交后自动触发流程

### 6.3 流程变量

启动流程时可传递以下变量：

| 变量名 | 说明 |
|-------|------|
| swName | 开源软件名称 |
| swVersion | 开源软件版本 |
| swCategory | 软件分类 |
| implApplyType | 申请类型 |
| implUserName | 申请人姓名 |

---

## 7. 文件结构

### 7.1 后端文件

```
bank-it-arch-backend/src/main/java/com/bank/itarch/
├── controller/
│   └── OssImplApplyController.java
├── service/
│   ├── OssImplApplyService.java
│   └── OssImplApplySuplService.java
├── mapper/
│   ├── OssImplApplyInfoMapper.java
│   └── OssImplApplySuplMapper.java
├── model/
│   ├── entity/
│   │   ├── OssImplApplyInfo.java
│   │   └── OssImplApplySupl.java
│   └── dto/
│       ├── OssImplApplyDTO.java
│       ├── OssImplApplySuplDTO.java
│       ├── OssImplApplyQueryDTO.java
│       ├── EvalIndicatorDTO.java
│       └── EvalOption.java
```

### 7.2 前端文件

```
bank-it-arch-frontend/src/
├── api/index.js (新增ossImplApplyApi)
├── router/index.js (新增路由)
├── views/oss/
│   ├── ImplApplyList.vue
│   └── ImplApplyForm.vue
└── components/oss/
    ├── EvalIndicatorsBase.vue
    ├── EvalIndicatorsTool.vue
    └── EvalIndicatorsCmpnt.vue
```

### 7.3 数据库文件

```
database/
├── migration_v2.0_oss_impl_apply.sql (主表和拓展表)
└── migration_v2.1_oss_approver_roles.sql (审批角色)
```

---

## 8. 数据库迁移步骤

1. 执行 `migration_v2.0_oss_impl_apply.sql` 创建表结构
2. 执行 `migration_v2.1_oss_approver_roles.sql` 创建审批角色
3. 在流程设计器中创建审批流程并发布
