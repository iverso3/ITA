# 银行IT架构管理平台 — 详细设计方案

**版本**：V1.2
**日期**：2026-03-26
**状态**：待审核
**修订说明**：
- V1.1：修正字段命名规范、补充缺失审计字段、新增6张表、修正资源关联表设计、添加索引设计
- V1.2：去掉服务器/云资源冗余应用字段；新增sys_team团队表；新增sys_menu菜单表；相关表增加team_id；补充arch_application_module、arch_service、arch_dependency、arch_data_flow审计字段

---

## 一、模块设计总览

```
┌─────────────────────────────────────────────────────────────────────┐
│                        银行IT架构管理平台                             │
├─────────────┬─────────────┬─────────────┬─────────────┬─────────────┤
│   CMDB管理   │  应用架构   │  技术架构   │  数据架构   │   审批流程   │
│  服务器/网络 │  应用/服务  │  技术栈/部署│  数据实体   │  多级审批   │
│  存储/云资源 │  模块/依赖  │             │  流向/分布  │             │
├─────────────┼─────────────┼─────────────┼─────────────┼─────────────┤
│                        报表中心                                     │
│              统计报表  │  三全景图  │  合规报告  │  趋势分析           │
├─────────────┼─────────────┼─────────────┼─────────────┤
│                        权限管理                       │
│              用户  │  角色  │  权限配置  │  操作日志               │
└─────────────┴─────────────────────────────────────────┘
```

---

## 二、公共设计

### 2.1 通用字段

所有表包含以下审计字段（已在下方各表字段中省略重复列出）：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键，自增 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除标记（0-未删，1-已删） |

> **命名规范说明**：
> - API请求/响应：驼峰命名（assetCode、appName）
> - 数据库字段：蛇形命名（asset_code、app_name）
> - 转换由后端API层统一处理

### 2.2 枚举值约定

| 枚举 | 值 | 说明 |
|------|-----|------|
| AssetStatus | ONLINE | 运行中 |
| AssetStatus | OFFLINE | 已下线 |
| AssetStatus | MAINTENANCE | 维护中 |
| AssetStatus | STOCK | 库存 |
| lifecycle | PLANNING | 规划中 |
| Lifecycle | DEVELOPMENT | 开发中 |
| Lifecycle | TESTING | 测试中 |
| Lifecycle | PRODUCTION | 生产运行 |
| Lifecycle | DEPRECATED | 已废弃 |
| sensitivity | PUBLIC | 公开 |
| Sensitivity | INTERNAL | 内部 |
| Sensitivity | CONFIDENTIAL | 机密 |
| Sensitivity | HIGHLY_CONFIDENTIAL | 高度机密 |

### 2.3 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2026-03-26 10:00:00",
  "traceId": "uuid"
}
```

**分页响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

### 2.4 统一分页请求

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码，默认1 |
| pageSize | int | 每页条数，默认20 |
| sortField | string | 排序字段 |
| sortOrder | string | 排序方向（asc/desc） |

---

## 三、CMDB管理模块

### 3.1 功能设计

**设计逻辑**：
- CMDB是整个平台的基础模块，管理服务器、网络设备、存储设备、云资源
- 支持手工单条录入和Excel批量导入
- 所有资产有唯一的资源编码（格式：`ASSET-{TYPE}-{SEQ}`，如`ASSET-SERVER-00001`）
- 资产有完整的生命周期管理（采购→入库→分配→运行→下线）
- 资产变更记录自动生成审计日志

**功能清单**：
| 功能 | 说明 |
|------|------|
| 资产列表 | 支持条件筛选、分页、排序 |
| 资产详情 | 查看资产完整信息 |
| 资产新增 | 单条手工录入 |
| 资产编辑 | 修改资产信息 |
| 资产删除 | 逻辑删除 |
| 批量导入 | Excel模板导入 |
| 批量导出 | 导出Excel/CSV |
| 资产变更记录 | 记录每次变更前后对比 |

### 3.2 数据库表设计

#### cmdb_server（服务器表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| asset_code | varchar(64) | 资源编码（唯一索引） |
| hostname | varchar(128) | 主机名 |
| ip_address | varchar(64) | IP地址 |
| inner_ip | varchar(64) | 内网IP |
| cpu | varchar(64) | CPU规格 |
| cpu_count | int | CPU核数 |
| memory | varchar(64) | 内存规格 |
| memory_size | int | 内存大小(GB) |
| disk | varchar(256) | 磁盘信息 |
| disk_size | int | 磁盘大小(GB) |
| os | varchar(128) | 操作系统 |
| os_version | varchar(64) | 操作系统版本 |
| server_type | varchar(32) | 服务器类型（物理机/虚拟机/容器） |
| status | varchar(32) | 状态 |
| department_id | bigint | 所属部门 |
| department_name | varchar(128) | 所属部门名称 |
| team_id | bigint | 所属团队 |
| team_name | varchar(128) | 所属团队名称 |
| idc | varchar(64) | 机房 |
| cabinet | varchar(64) | 机柜 |
| manufacturer | varchar(128) | 厂商 |
| model | varchar(128) | 型号 |
| serial_number | varchar(128) | 序列号 |
| purchase_date | date | 采购日期 |
| warranty_expire | date | 保修到期 |
| remark | text | 备注 |
| extra_attrs | text | 扩展属性（JSON） |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除（0-未删，1-已删） |

#### cmdb_network（网络设备表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| asset_code | varchar(64) | 资源编码（唯一索引） |
| device_name | varchar(128) | 设备名称 |
| device_type | varchar(32) | 设备类型（交换机/路由器/防火墙/负载均衡） |
| ip_address | varchar(64) | 管理IP |
| mgmt_vlan | varchar(32) | 管理VLAN |
| port_count | int | 端口数 |
| used_port_count | int | 已用端口数 |
| bandwidth | varchar(64) | 带宽 |
| manufacturer | varchar(128) | 厂商 |
| model | varchar(128) | 型号 |
| serial_number | varchar(128) | 序列号 |
| status | varchar(32) | 状态 |
| department_id | bigint | 所属部门 |
| department_name | varchar(128) | 所属部门名称 |
| team_id | bigint | 所属团队 |
| team_name | varchar(128) | 所属团队名称 |
| idc | varchar(64) | 机房 |
| cabinet | varchar(64) | 机柜 |
| purchase_date | date | 采购日期 |
| warranty_expire | date | 保修到期 |
| remark | text | 备注 |
| extra_attrs | text | 扩展属性（JSON） |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除（0-未删，1-已删） |

#### cmdb_storage（存储设备表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| asset_code | varchar(64) | 资源编码（唯一索引） |
| device_name | varchar(128) | 设备名称 |
| device_type | varchar(32) | 设备类型（NAS/SAN/对象存储） |
| total_capacity | bigint | 总容量(TB) |
| used_capacity | bigint | 已用容量(TB) |
| manufacturer | varchar(128) | 厂商 |
| model | varchar(128) | 型号 |
| serial_number | varchar(128) | 序列号 |
| status | varchar(32) | 状态 |
| department_id | bigint | 所属部门 |
| department_name | varchar(128) | 所属部门名称 |
| team_id | bigint | 所属团队 |
| team_name | varchar(128) | 所属团队名称 |
| idc | varchar(64) | 机房 |
| purchase_date | date | 采购日期 |
| warranty_expire | date | 保修到期 |
| remark | text | 备注 |
| extra_attrs | text | 扩展属性（JSON） |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除（0-未删，1-已删） |

#### cmdb_cloud_resource（云资源表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| resource_code | varchar(64) | 资源编码（唯一索引） |
| resource_name | varchar(128) | 资源名称 |
| resource_type | varchar(32) | 资源类型（云主机/云数据库/云存储/负载均衡） |
| cloud_provider | varchar(64) | 云厂商（阿里云/华为云/腾讯云/行内云） |
| region | varchar(64) | 地域 |
| zone | varchar(64) | 可用区 |
| specification | varchar(128) | 规格 |
| status | varchar(32) | 状态 |
| ip_address | varchar(64) | 公网IP |
| inner_ip | varchar(64) | 内网IP |
| cpu | int | CPU核数 |
| memory | int | 内存(GB) |
| disk_size | int | 磁盘大小(GB) |
| bandwidth | int | 带宽(Mbps) |
| department_id | bigint | 所属部门 |
| department_name | varchar(128) | 所属部门名称 |
| team_id | bigint | 所属团队 |
| team_name | varchar(128) | 所属团队名称 |
| cost_per_month | decimal(10,2) | 月费用 |
| expire_date | date | 到期日期 |
| remark | text | 备注 |
| extra_attrs | text | 扩展属性（JSON） |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除（0-未删，1-已删） |

#### cmdb_asset_change_log（资产变更日志表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| asset_type | varchar(32) | 资产类型（server/network/storage/cloud） |
| asset_id | bigint | 资产ID |
| asset_code | varchar(64) | 资源编码 |
| operate_type | varchar(32) | 操作类型（CREATE/UPDATE/DELETE） |
| before_value | text | 变更前值（JSON） |
| after_value | text | 变更后值（JSON） |
| operator | varchar(64) | 操作人 |
| operate_time | datetime | 操作时间 |
| operate_ip | varchar(64) | 操作IP |
| remark | varchar(256) | 备注 |

### 3.3 API接口设计

#### 3.3.1 服务器管理

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页查询 | GET | `/api/v1/cmdb/servers` | 列表查询 |
| 详情 | GET | `/api/v1/cmdb/servers/{id}` | 获取详情 |
| 新增 | POST | `/api/v1/cmdb/servers` | 创建服务器 |
| 编辑 | PUT | `/api/v1/cmdb/servers/{id}` | 更新服务器 |
| 删除 | DELETE | `/api/v1/cmdb/servers/{id}` | 删除服务器 |
| 批量导入 | POST | `/api/v1/cmdb/servers/import` | Excel导入 |
| 导出 | GET | `/api/v1/cmdb/servers/export` | 导出Excel |

**列表查询** `GET /api/v1/cmdb/servers`

Request Parameters:
```
page=1&pageSize=20&keyword=xxx&status=ONLINE&departmentId=1
```

Response:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "assetCode": "ASSET-SERVER-00001",
        "hostname": "app-server-01",
        "ipAddress": "192.168.1.100",
        "cpu": "Intel Xeon Gold 6248",
        "cpuCount": 2,
        "memorySize": 128,
        "diskSize": 500,
        "os": "CentOS",
        "osVersion": "7.9",
        "status": "ONLINE",
        "departmentName": "信息科技部",
        "idc": "主数据中心-A",
        "applicationName": "核心银行系统"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

**新增** `POST /api/v1/cmdb/servers`

Request Body:
```json
{
  "hostname": "app-server-02",
  "ipAddress": "192.168.1.101",
  "cpu": "Intel Xeon Gold 6248",
  "cpuCount": 2,
  "memorySize": 128,
  "diskSize": 500,
  "os": "CentOS",
  "osVersion": "7.9",
  "status": "OFFLINE",
  "departmentId": 1,
  "applicationId": 5,
  "idc": "主数据中心-A",
  "manufacturer": "Dell",
  "model": "PowerEdge R740",
  "serialNumber": "SN12345678",
  "remark": ""
}
```

---

## 四、应用架构模块

### 4.1 功能设计

**设计逻辑**：
- 应用架构是最核心的模块，管理全行所有应用系统
- 每个应用系统包含多个服务和模块
- 应用之间有依赖关系（依赖/被依赖），形成依赖图
- 应用的变更通过审批流程管控
- 应用有完整的生命周期

**功能清单**：
| 功能 | 说明 |
|------|------|
| 应用列表 | 树形结构/列表视图，支持搜索筛选 |
| 应用详情 | 基本信息、关联资源、架构图 |
| 服务管理 | 应用下的服务定义 |
| 模块管理 | 应用下的模块划分 |
| 依赖关系 | 添加/移除依赖关系 |
| 依赖关系图 | 可视化展示依赖拓扑 |
| 关联资源 | 关联的服务器、云资源 |

### 4.2 数据库表设计

#### arch_application（应用系统表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| app_code | varchar(64) | 应用编码（唯一） |
| app_name | varchar(128) | 应用名称 |
| app_name_en | varchar(128) | 英文名称 |
| app_type | varchar(32) | 应用类型（交易/渠道/管理/数据/基础设施） |
| importance_level | varchar(32) | 重要级别（核心/重要/一般/辅助） |
| lifecycle | varchar(32) | 生命周期阶段 |
| business_domain | varchar(64) | 业务域 |
| department_id | bigint | 所属部门ID |
| department_name | varchar(128) | 所属部门名称 |
| team_id | bigint | 所属团队ID |
| team_name | varchar(128) | 所属团队名称 |
| pm_name | varchar(64) | 产品经理 |
| pm_email | varchar(128) | 产品经理邮箱 |
| tech_lead | varchar(64) | 技术负责人 |
| tech_lead_email | varchar(128) | 技术负责人邮箱 |
| description | text | 应用描述 |
| business_description | text | 业务描述 |
| deployment_type | varchar(32) | 部署方式（单机/集群/分布式/云原生） |
| access_type | varchar(32) | 访问方式（内网/外网/内外网） |
| sla_level | varchar(32) | SLA级别 |
| version | varchar(32) | 当前版本 |
| go_live_date | date | 上线日期 |
| retire_date | date | 计划下线日期 |
| status | varchar(32) | 状态 |
| remark | text | 备注 |

#### arch_application_module（应用模块表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| app_id | bigint | 所属应用ID |
| module_code | varchar(64) | 模块编码 |
| module_name | varchar(128) | 模块名称 |
| module_type | varchar(32) | 模块类型（前端/后端/公共/数据） |
| description | text | 模块描述 |
| tech_stack | varchar(256) | 技术栈 |
| parent_module_id | bigint | 父模块ID |
| sort_order | int | 排序 |
| status | varchar(32) | 状态 |
| team_id | bigint | 所属团队 |
| team_name | varchar(128) | 所属团队名称 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除 |

#### arch_service（服务表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| app_id | bigint | 所属应用ID |
| module_id | bigint | 所属模块ID |
| service_code | varchar(64) | 服务编码 |
| service_name | varchar(128) | 服务名称 |
| service_type | varchar(32) | 服务类型（微服务/API/中间件服务） |
| protocol | varchar(32) | 协议（HTTP/gRPC/Dubbo/消息） |
| port | int | 端口 |
| context_path | varchar(128) | 上下文路径 |
| version | varchar(32) | 版本 |
| description | text | 描述 |
| status | varchar(32) | 状态 |
| team_id | bigint | 所属团队 |
| team_name | varchar(128) | 所属团队名称 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除 |

#### arch_dependency（依赖关系表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| source_app_id | bigint | 源应用ID |
| source_app_code | varchar(64) | 源应用编码 |
| source_app_name | varchar(128) | 源应用名称 |
| source_service_id | bigint | 源服务ID |
| source_service_name | varchar(128) | 源服务名称 |
| target_app_id | bigint | 目标应用ID |
| target_app_code | varchar(64) | 目标应用编码 |
| target_app_name | varchar(128) | 目标应用名称 |
| target_service_id | bigint | 目标服务ID |
| target_service_name | varchar(128) | 目标服务名称 |
| dependency_type | varchar(32) | 依赖类型（同步/异步/数据） |
| description | text | 依赖描述 |
| interface_type | varchar(64) | 接口类型（REST/gRPC/消息/数据库） |
| interface_path | varchar(256) | 接口地址 |
| remark | varchar(256) | 备注 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除 |

#### arch_application_server（应用-服务器关联表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| app_id | bigint | 应用ID |
| server_id | bigint | 服务器ID |
| usage | varchar(128) | 用途描述 |
| create_time | datetime | 创建时间 |
| creator | varchar(64) | 创建人 |

#### arch_application_cloud（应用-云资源关联表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| app_id | bigint | 应用ID |
| cloud_resource_id | bigint | 云资源ID |
| usage | varchar(128) | 用途描述 |
| create_time | datetime | 创建时间 |
| creator | varchar(64) | 创建人 |

#### arch_application_network（应用-网络设备关联表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| app_id | bigint | 应用ID |
| network_id | bigint | 网络设备ID |
| usage | varchar(128) | 用途描述 |
| create_time | datetime | 创建时间 |
| creator | varchar(64) | 创建人 |

#### arch_application_storage（应用-存储关联表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| app_id | bigint | 应用ID |
| storage_id | bigint | 存储设备ID |
| usage | varchar(128) | 用途描述 |
| create_time | datetime | 创建时间 |
| creator | varchar(64) | 创建人 |

### 4.3 API接口设计

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 应用列表 | GET | `/api/v1/arch/applications` | 分页列表 |
| 应用详情 | GET | `/api/v1/arch/applications/{id}` | 详情 |
| 新增应用 | POST | `/api/v1/arch/applications` | 创建应用 |
| 编辑应用 | PUT | `/api/v1/arch/applications/{id}` | 更新应用 |
| 删除应用 | DELETE | `/api/v1/arch/applications/{id}` | 删除应用 |
| 应用模块列表 | GET | `/api/v1/arch/applications/{id}/modules` | 模块列表 |
| 服务列表 | GET | `/api/v1/arch/applications/{id}/services` | 服务列表 |
| 依赖关系列表 | GET | `/api/v1/arch/applications/{id}/dependencies` | 依赖列表 |
| 添加依赖 | POST | `/api/v1/arch/dependencies` | 添加依赖 |
| 删除依赖 | DELETE | `/api/v1/arch/dependencies/{id}` | 删除依赖 |
| 依赖拓扑图 | GET | `/api/v1/arch/dependencies/graph` | 全局依赖拓扑 |

**应用详情** `GET /api/v1/arch/applications/{id}`

Response:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "appCode": "APP-CORE-BANK",
    "appName": "核心银行系统",
    "appType": "交易",
    "importanceLevel": "核心",
    "lifecycle": "PRODUCTION",
    "businessDomain": "银行业务",
    "departmentName": "信息科技部",
    "pmName": "张三",
    "techLead": "李四",
    "description": "银行核心业务系统",
    "deploymentType": "分布式",
    "status": "ONLINE",
    "servers": [
      {"id": 1, "hostname": "app-server-01", "ipAddress": "192.168.1.100"}
    ],
    "modules": [
      {"id": 1, "moduleName": "账户管理", "moduleType": "后端"},
      {"id": 2, "moduleName": "交易处理", "moduleType": "后端"}
    ],
    "services": [
      {"id": 1, "serviceName": "账户服务", "protocol": "gRPC", "port": 8080}
    ],
    "dependencies": [
      {
        "targetAppName": "数据总线",
        "dependencyType": "同步",
        "interfaceType": "REST"
      }
    ]
  }
}
```

---

## 五、技术架构模块

### 5.1 功能设计

**设计逻辑**：
- 管理全行技术栈标准化（语言、框架、中间件、数据库）
- 应用可关联技术栈，便于统计
- 展示部署架构（多层级：接入层、应用层、数据层）
- 技术选型规范：定义标准技术栈，限制随意引入新技术

**功能清单**：
| 功能 | 说明 |
|------|------|
| 技术栈管理 | 语言/框架/中间件/数据库类型 |
| 应用技术关联 | 应用关联的技术栈 |
| 部署架构 | 多层级部署拓扑 |
| 技术分布统计 | 各技术使用排行/分布 |

### 5.2 数据库表设计

#### arch_tech_stack（技术栈字典表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| stack_code | varchar(64) | 技术编码 |
| stack_name | varchar(128) | 技术名称 |
| stack_type | varchar(32) | 类型（语言/框架/中间件/数据库/操作系统/其他） |
| parent_id | bigint | 父级ID |
| tree_path | varchar(256) | 层级路径（如 `/1/3/5/`） |
| version | varchar(64) | 版本 |
| vendor | varchar(128) | 供应商 |
| license_type | varchar(64) | 许可证类型 |
| is_standard | tinyint | 是否标准技术（1-是，0-否） |
| is_active | tinyint | 是否启用 |
| description | text | 描述 |
| usage_guide | text | 使用指南 |
| remark | varchar(256) | 备注 |

#### arch_deployment_architecture（部署架构表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| app_id | bigint | 关联应用ID |
| layer | varchar(32) | 层级（接入层/应用层/数据层/公共层） |
| node_name | varchar(128) | 节点名称 |
| node_type | varchar(32) | 节点类型 |
| count | int | 节点数量 |
| specification | varchar(256) | 规格 |
| deployment_location | varchar(128) | 部署位置 |
| description | text | 描述 |
| sort_order | int | 排序 |

### 5.3 API接口设计

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 技术栈列表 | GET | `/api/v1/tech/stacks` | 分页查询 |
| 技术栈树 | GET | `/api/v1/tech/stacks/tree` | 树形结构 |
| 新增技术栈 | POST | `/api/v1/tech/stacks` | 创建 |
| 编辑技术栈 | PUT | `/api/v1/tech/stacks/{id}` | 更新 |
| 删除技术栈 | DELETE | `/api/v1/tech/stacks/{id}` | 删除 |
| 应用技术关联 | GET | `/api/v1/tech/applications/{appId}/stacks` | 应用的关联技术 |
| 设置应用技术 | POST | `/api/v1/tech/applications/{appId}/stacks` | 关联技术栈 |
| 部署架构 | GET | `/api/v1/tech/applications/{appId}/deployment` | 应用部署架构 |
| 保存部署架构 | POST | `/api/v1/tech/deployment` | 保存部署架构 |
| 技术分布统计 | GET | `/api/v1/tech/statistics/distribution` | 技术分布统计 |

---

## 六、数据架构模块

### 6.1 功能设计

**设计逻辑**：
- 数据架构管理数据实体、数据流向、数据分布
- 数据实体：应用的数据表/集合结构抽象
- 数据流向：数据在应用之间的流动关系（同步/异步/批量）
- 数据分布：数据存储在哪些数据库/存储中
- 敏感数据标识：标记敏感数据字段

**功能清单**：
| 功能 | 说明 |
|------|------|
| 数据实体管理 | 数据表/集合定义 |
| 数据流向管理 | 数据在系统间流动关系 |
| 数据分布视图 | 数据存储位置分布 |
| 敏感数据标识 | 敏感字段标记 |

### 6.2 数据库表设计

#### arch_data_entity（数据实体表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| app_id | bigint | 所属应用ID |
| entity_code | varchar(64) | 实体编码 |
| entity_name | varchar(128) | 实体名称 |
| entity_type | varchar(32) | 实体类型（表/集合/文件/消息） |
| db_type | varchar(32) | 存储类型（MySQL/Oracle/Redis/MongoDB/Kafka） |
| db_name | varchar(64) | 库名 |
| table_name | varchar(64) | 表名/集合名 |
| sensitivity | varchar(32) | 敏感等级 |
| description | text | 描述 |
| field_count | int | 字段数量 |
| record_count | bigint | 记录数 |
| data_volume | bigint | 数据量(GB) |
| status | varchar(32) | 状态 |

#### arch_data_entity_field（数据实体字段表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| entity_id | bigint | 所属实体ID |
| field_name | varchar(64) | 字段名 |
| field_type | varchar(32) | 字段类型 |
| field_length | int | 长度 |
| is_primary_key | tinyint | 是否主键 |
| is_nullable | tinyint | 是否可空 |
| is_sensitive | tinyint | 是否敏感 |
| default_value | varchar(128) | 默认值 |
| description | varchar(256) | 字段描述 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除 |

#### arch_data_flow（数据流向表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| flow_code | varchar(64) | 流向编码 |
| flow_name | varchar(128) | 流向名称 |
| source_app_id | bigint | 源应用ID |
| source_app_name | varchar(128) | 源应用名称 |
| source_entity_id | bigint | 源实体ID |
| source_entity_name | varchar(128) | 源实体名称 |
| target_app_id | bigint | 目标应用ID |
| target_app_name | varchar(128) | 目标应用名称 |
| target_entity_id | bigint | 目标实体ID |
| target_entity_name | varchar(128) | 目标实体名称 |
| flow_type | varchar(32) | 流向类型（同步/异步/批量） |
| transfer_type | varchar(32) | 传输方式（CDC/ETL/消息/接口） |
| frequency | varchar(32) | 频率（实时/定时/批量） |
| data_volume | bigint | 数据量(条/天) |
| description | text | 描述 |
| remark | varchar(256) | 备注 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除 |

### 6.3 API接口设计

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 数据实体列表 | GET | `/api/v1/data/entities` | 分页查询 |
| 实体详情 | GET | `/api/v1/data/entities/{id}` | 详情含字段 |
| 新增实体 | POST | `/api/v1/data/entities` | 创建 |
| 编辑实体 | PUT | `/api/v1/data/entities/{id}` | 更新 |
| 删除实体 | DELETE | `/api/v1/data/entities/{id}` | 删除 |
| 数据流向列表 | GET | `/api/v1/data/flows` | 分页查询 |
| 新增流向 | POST | `/api/v1/data/flows` | 创建 |
| 编辑流向 | PUT | `/api/v1/data/flows/{id}` | 更新 |
| 删除流向 | DELETE | `/api/v1/data/flows/{id}` | 删除 |
| 数据分布视图 | GET | `/api/v1/data/distribution` | 分布视图 |
| 数据流拓扑图 | GET | `/api/v1/data/flows/graph` | 流可视化 |

---

## 七、审批流程模块

### 7.1 功能设计

**设计逻辑**：
- 审批模块驱动新建应用和架构变更
- 流程定义：可配置多级审批链（审批节点、支持人、会签/或签）
- 流程实例：每个申请创建一条流程实例
- 审批节点：按顺序流转，每个节点可指定审批人/角色/部门负责人
- 审批历史：完整记录审批过程（通过/拒绝/转交/退回）
- 支持委托：审批人可将任务委托他人

**默认流程（新建应用）**：
```
申请人提交
    ↓
部门负责人审批（必须）
    ↓
架构委员会审批（核心系统必须，其他可选）
    ↓
科技管理层审批（重要/核心系统必须）
    ↓
完成
```

### 7.2 数据库表设计

#### wf_definition（流程定义表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| definition_code | varchar(64) | 流程定义编码 |
| definition_name | varchar(128) | 流程名称 |
| process_type | varchar(32) | 流程类型（NEW_APP/CHANGE_APP） |
| description | text | 描述 |
| version | varchar(32) | 版本 |
| is_active | tinyint | 是否启用 |
| config | text | 流程配置（JSON） |
| remark | varchar(256) | 备注 |

#### wf_definition_node（流程节点表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| definition_id | bigint | 所属流程定义ID |
| node_code | varchar(64) | 节点编码 |
| node_name | varchar(128) | 节点名称 |
| node_order | int | 节点顺序 |
| node_type | varchar(32) | 节点类型（APPROVAL/COPY/CONDITION） |
| approver_type | varchar(32) | 审批人类型（USER/ROLE/DEPT_HEAD/MANAGER） |
| approver_id | bigint | 审批人ID |
| approver_name | varchar(128) | 审批人姓名 |
| is_required | tinyint | 是否必须审批 |
| approve_type | varchar(32) | 审批方式（ANY/ALL） |
| timeout_hours | int | 超时时间(小时) |
| auto_pass | tinyint | 超时自动通过 |
| condition_rule | text | 条件规则 |
| remark | varchar(256) | 备注 |

#### wf_instance（流程实例表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| instance_code | varchar(64) | 实例编码 |
| definition_id | bigint | 流程定义ID |
| definition_name | varchar(128) | 流程名称 |
| process_type | varchar(32) | 流程类型 |
| business_type | varchar(32) | 业务类型 |
| business_id | bigint | 业务ID |
| business_key | varchar(128) | 业务标识 |
| title | varchar(256) | 标题 |
| applicant_id | bigint | 申请人ID |
| applicant_name | varchar(64) | 申请人姓名 |
| applicant_dept_id | bigint | 申请人部门ID |
| applicant_dept_name | varchar(128) | 申请人部门名称 |
| current_node_id | bigint | 当前节点ID |
| current_node_name | varchar(128) | 当前节点名称 |
| status | varchar(32) | 状态（DRAFT/RUNNING/SUSPENDED/COMPLETED/TERMINATED） |
| priority | varchar(32) | 优先级（LOW/NORMAL/HIGH/URGENT） |
| start_time | datetime | 开始时间 |
| end_time | datetime | 结束时间 |
| duration | bigint | 时长(秒) |
| result | varchar(32) | 结果（PASS/REJECT） |
| business_data | text | 业务数据（JSON） |
| remark | varchar(256) | 备注 |

#### wf_task（审批任务表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| instance_id | bigint | 所属实例ID |
| node_id | bigint | 节点定义ID |
| task_code | varchar(64) | 任务编码 |
| task_name | varchar(128) | 任务名称 |
| task_type | varchar(32) | 任务类型（APPROVE/COPY） |
| assignee_id | bigint | 指定审批人ID |
| assignee_name | varchar(64) | 指定审批人姓名 |
| status | varchar(32) | 状态（PENDING/APPROVED/REJECTED/TRANSFERRED/DELEGATED/RETURNED） |
| approve_type | varchar(32) | 审批方式（ANY/ALL） |
| approved_count | int | 已审批人数 |
| required_count | int | 需审批人数 |
| create_time | datetime | 创建时间 |
| expire_time | datetime | 期望完成时间 |
| complete_time | datetime | 实际完成时间 |
| delegate_from | bigint | 委托来源 |
| delegate_to | bigint | 委托去向 |
| opinion | text | 审批意见 |
| remark | varchar(256) | 备注 |

#### wf_task_candidate（审批任务候选人表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| task_id | bigint | 所属任务ID |
| candidate_id | bigint | 候选人ID |
| candidate_name | varchar(64) | 候选人姓名 |
| candidate_type | varchar(32) | 候选人类型（USER/ROLE/DEPT_HEAD） |
| is_approved | tinyint | 是否已审批（0-未审，1-已审） |
| approve_time | datetime | 审批时间 |
| create_time | datetime | 创建时间 |

#### wf_history（审批历史表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| instance_id | bigint | 所属实例ID |
| task_id | bigint | 关联任务ID |
| node_id | bigint | 节点定义ID |
| node_name | varchar(128) | 节点名称 |
| operator_id | bigint | 操作人ID |
| operator_name | varchar(64) | 操作人姓名 |
| operator_dept_name | varchar(128) | 操作人部门 |
| action | varchar(32) | 操作（SUBMIT/APPROVE/REJECT/RETURN/TRANSFER/DELEGATE/WITHDRAW） |
| opinion | text | 审批意见 |
| create_time | datetime | 操作时间 |
| duration | bigint | 处理时长(秒) |

### 7.3 API接口设计

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 流程定义列表 | GET | `/api/v1/wf/definitions` | 列表 |
| 流程定义详情 | GET | `/api/v1/wf/definitions/{id}` | 详情含节点 |
| 新建流程定义 | POST | `/api/v1/wf/definitions` | 创建 |
| 编辑流程定义 | PUT | `/api/v1/wf/definitions/{id}` | 更新 |
| 启用/停用 | PATCH | `/api/v1/wf/definitions/{id}/status` | 状态切换 |
| 申请列表 | GET | `/api/v1/wf/instances` | 我的申请 |
| 待办任务 | GET | `/api/v1/wf/tasks/todo` | 待我审批 |
| 已办任务 | GET | `/api/v1/wf/tasks/done` | 我已审批 |
| 审批通过 | POST | `/api/v1/wf/tasks/{id}/approve` | 通过 |
| 审批拒绝 | POST | `/api/v1/wf/tasks/{id}/reject` | 拒绝 |
| 退回 | POST | `/api/v1/wf/tasks/{id}/return` | 退回 |
| 转交 | POST | `/api/v1/wf/tasks/{id}/transfer` | 转交他人 |
| 委托 | POST | `/api/v1/wf/tasks/{id}/delegate` | 委托 |
| 撤回 | POST | `/api/v1/wf/instances/{id}/withdraw` | 撤回申请 |
| 流程历史 | GET | `/api/v1/wf/instances/{id}/history` | 审批历史 |
| 新建应用申请 | POST | `/api/v1/wf/applications` | 发起申请 |

**新建应用申请** `POST /api/v1/wf/applications`

Request Body:
```json
{
  "definitionId": 1,
  "priority": "NORMAL",
  "businessData": {
    "appName": "新信贷系统",
    "appType": "交易",
    "importanceLevel": "重要",
    "departmentId": 2,
    "description": "信贷业务管理系统",
    "techStacks": [1, 5, 8],
    "servers": [{"type": "server", "count": 3}]
  },
  "remark": ""
}
```

Response:
```json
{
  "code": 200,
  "message": "提交成功",
  "data": {
    "instanceId": 100,
    "instanceCode": "WF-APP-20260326-001",
    "currentNodeName": "部门负责人审批"
  }
}
```

**审批** `POST /api/v1/wf/tasks/{id}/approve`

Request Body:
```json
{
  "opinion": "同意，请继续"
}
```

---

## 八、权限管理模块

### 8.1 功能设计

**设计逻辑**：
- 基于RBAC权限模型（角色-权限-用户）
- 权限包含菜单权限（能看哪些菜单）、按钮权限（能操作哪些按钮）、数据权限（能看哪些部门的数据）
- 支持对接行内LDAP/AD同步用户
- 敏感操作记录操作日志
- 权限变更有完整审计记录

**角色说明**：
| 角色 | 权限描述 |
|------|----------|
| 超级管理员 | 系统全部权限 |
| 系统管理员 | 系统配置、用户管理、权限配置 |
| 架构师 | 架构资产管理、编辑 |
| 开发人员 | 查看架构、发起变更申请 |
| 项目经理 | 查看架构、了解系统归属 |
| 科技管理层 | 查看报表和总览 |
| 审计人员 | 只读、查看变更历史 |

### 8.2 数据库表设计

#### sys_user（用户表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| username | varchar(64) | 用户名（唯一） |
| password | varchar(128) | 密码（加密） |
| real_name | varchar(64) | 真实姓名 |
| email | varchar(128) | 邮箱 |
| phone | varchar(32) | 手机号 |
| avatar | varchar(256) | 头像URL |
| department_id | bigint | 部门ID |
| department_name | varchar(128) | 部门名称 |
| status | varchar(32) | 状态（ENABLED/DISABLED/LOCKED） |
| user_type | varchar(32) | 用户类型（LOCAL/LDAP） |
| ldap_dn | varchar(256) | LDAP DN |
| last_login_time | datetime | 最后登录时间 |
| last_login_ip | varchar(64) | 最后登录IP |
| password_expire_time | datetime | 密码过期时间 |
| is_first_login | tinyint | 首次登录 |
| remark | varchar(256) | 备注 |
| team_id | bigint | 所属团队 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除 |

#### sys_menu（菜单表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| parent_id | bigint | 父菜单ID |
| tree_path | varchar(256) | 层级路径（如 `/1/3/5/`） |
| menu_code | varchar(64) | 菜单编码 |
| menu_name | varchar(128) | 菜单名称 |
| menu_type | varchar(32) | 菜单类型（CATALOG/MENU/BUTTON） |
| icon | varchar(64) | 图标 |
| path | varchar(256) | 路由路径 |
| component | varchar(256) | 组件路径 |
| redirect | varchar(256) | 重定向路径 |
| is_visible | tinyint | 是否显示（0-隐藏，1-显示） |
| is_cache | tinyint | 是否缓存 |
| sort_order | int | 排序 |
| is_active | tinyint | 是否启用 |
| external_link | varchar(256) | 外部链接 |
| permission | varchar(128) | 权限标识 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除 |

#### sys_role（角色表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| role_code | varchar(64) | 角色编码（唯一） |
| role_name | varchar(128) | 角色名称 |
| role_type | varchar(32) | 角色类型（SYSTEM/BUSINESS） |
| data_scope | varchar(32) | 数据权限范围（ALL/DEPT/DEPT_CHILDREN/SELF） |
| description | text | 描述 |
| is_active | tinyint | 是否启用 |
| sort_order | int | 排序 |
| team_id | bigint | 所属团队 |
| remark | varchar(256) | 备注 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除 |

#### sys_permission（权限表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| permission_code | varchar(64) | 权限编码 |
| permission_name | varchar(128) | 权限名称 |
| permission_type | varchar(32) | 权限类型（BUTTON/DATA） |
| menu_id | bigint | 关联菜单ID（用于按钮权限关联到菜单） |
| resource_type | varchar(32) | 资源类型（用于数据权限） |
| resource_id | bigint | 资源ID（用于数据权限） |
| permission | varchar(64) | 按钮权限标识 |
| method | varchar(16) | 请求方法 |
| is_enabled | tinyint | 是否启用 |
| sort_order | int | 排序 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除 |

#### sys_user_role（用户角色关联表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户ID |
| role_id | bigint | 角色ID |
| create_time | datetime | 创建时间 |

#### sys_role_permission（角色权限关联表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| role_id | bigint | 角色ID |
| permission_id | bigint | 权限ID |
| create_time | datetime | 创建时间 |

#### sys_operation_log（操作日志表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| module | varchar(64) | 模块 |
| operation | varchar(64) | 操作 |
| operation_type | varchar(32) | 操作类型 |
| request_method | varchar(16) | 请求方法 |
| request_url | varchar(256) | 请求URL |
| request_params | text | 请求参数 |
| request_body | text | 请求体 |
| response | text | 响应结果 |
| operator_id | bigint | 操作人ID |
| operator_name | varchar(64) | 操作人姓名 |
| operator_ip | varchar(64) | 操作IP |
| operator_location | varchar(128) | 操作地点 |
| execute_time | int | 执行时间(ms) |
| create_time | datetime | 操作时间 |

#### sys_department（部门表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| parent_id | bigint | 父部门ID |
| tree_path | varchar(256) | 层级路径（如 `/1/3/5/`） |
| department_code | varchar(64) | 部门编码（唯一） |
| department_name | varchar(128) | 部门名称 |
| leader_id | bigint | 负责人ID |
| leader_name | varchar(64) | 负责人姓名 |
| sort_order | int | 排序 |
| is_active | tinyint | 是否启用 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除 |

#### sys_team（团队表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| department_id | bigint | 所属部门ID |
| team_code | varchar(64) | 团队编码（唯一） |
| team_name | varchar(128) | 团队名称 |
| leader_id | bigint | 团队负责人ID |
| leader_name | varchar(64) | 团队负责人姓名 |
| sort_order | int | 排序 |
| is_active | tinyint | 是否启用 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除 |

#### sys_dict（字典表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| dict_code | varchar(64) | 字典编码（唯一） |
| dict_name | varchar(128) | 字典名称 |
| dict_type | varchar(32) | 字典类型（SYSTEM/BUSINESS） |
| description | varchar(256) | 描述 |
| is_active | tinyint | 是否启用 |
| sort_order | int | 排序 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除 |

#### sys_dict_item（字典项表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| dict_id | bigint | 所属字典ID |
| item_code | varchar(64) | 字典项编码 |
| item_name | varchar(128) | 字典项名称 |
| item_value | varchar(256) | 字典项值 |
| sort_order | int | 排序 |
| is_default | tinyint | 是否默认值 |
| is_active | tinyint | 是否启用 |
| description | varchar(256) | 描述 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| creator | varchar(64) | 创建人 |
| modifier | varchar(64) | 更新人 |
| deleted | tinyint | 逻辑删除 |

#### sys_file_record（文件操作记录表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| file_name | varchar(256) | 文件名 |
| file_path | varchar(512) | 文件路径 |
| file_size | bigint | 文件大小(字节) |
| file_type | varchar(32) | 文件类型 |
| operate_type | varchar(32) | 操作类型（IMPORT/EXPORT） |
| business_type | varchar(32) | 业务类型 |
| status | varchar(32) | 状态（SUCCESS/FAIL） |
| error_msg | varchar(512) | 错误信息 |
| operator_id | bigint | 操作人ID |
| operator_name | varchar(64) | 操作人姓名 |
| create_time | datetime | 操作时间 |

#### sys_message（消息通知表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| msg_type | varchar(32) | 消息类型（APPROVE/TODO/SYSTEM） |
| title | varchar(256) | 消息标题 |
| content | text | 消息内容 |
| business_type | varchar(32) | 业务类型 |
| business_id | bigint | 业务ID |
| sender_id | bigint | 发送人ID |
| sender_name | varchar(64) | 发送人姓名 |
| receiver_id | bigint | 接收人ID |
| receiver_name | varchar(64) | 接收人姓名 |
| is_read | tinyint | 是否已读（0-未读，1-已读） |
| read_time | datetime | 阅读时间 |
| create_time | datetime | 创建时间 |

### 8.3 API接口设计

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户列表 | GET | `/api/v1/system/users` | 分页查询 |
| 用户详情 | GET | `/api/v1/system/users/{id}` | 详情 |
| 新增用户 | POST | `/api/v1/system/users` | 创建 |
| 编辑用户 | PUT | `/api/v1/system/users/{id}` | 更新 |
| 删除用户 | DELETE | `/api/v1/system/users/{id}` | 删除 |
| 重置密码 | POST | `/api/v1/system/users/{id}/reset-password` | 重置 |
| 修改密码 | POST | `/api/v1/system/users/{id}/change-password` | 修改密码 |
| 启用/禁用 | PATCH | `/api/v1/system/users/{id}/status` | 状态切换 |
| LDAP同步 | POST | `/api/v1/system/users/sync-ldap` | 同步LDAP用户 |
| 角色列表 | GET | `/api/v1/system/roles` | 列表 |
| 角色详情 | GET | `/api/v1/system/roles/{id}` | 详情含权限 |
| 新增角色 | POST | `/api/v1/system/roles` | 创建 |
| 编辑角色 | PUT | `/api/v1/system/roles/{id}` | 更新 |
| 删除角色 | DELETE | `/api/v1/system/roles/{id}` | 删除 |
| 分配权限 | POST | `/api/v1/system/roles/{id}/permissions` | 分配权限 |
| 菜单树 | GET | `/api/v1/system/menus/tree` | 菜单树 |
| 权限列表 | GET | `/api/v1/system/permissions` | 列表 |
| 操作日志 | GET | `/api/v1/system/logs` | 分页查询 |
| 当前用户 | GET | `/api/v1/system/users/me` | 当前用户信息 |
| 更新头像 | POST | `/api/v1/system/users/me/avatar` | 更新头像 |

---

## 九、报表中心模块

### 9.1 功能设计

**设计逻辑**：
- 统计报表：基于各模块数据做统计分析
- 三全景图：系统全景图、技术栈全景图、数据分布全景图（ECharts可视化）
- 合规报告：满足监管审计要求，支持导出PDF/Excel
- 趋势分析：资产增长趋势、技术演进趋势

**功能清单**：
| 功能 | 说明 |
|------|------|
| 系统统计 | 按部门/状态/生命周期统计 |
| 技术分布统计 | 各技术使用排行 |
| 资源统计 | 服务器/网络/存储/云资源统计 |
| 系统全景图 | 全行应用系统分布可视化 |
| 技术栈全景图 | 技术栈使用分布可视化 |
| 数据分布全景图 | 数据存储分布可视化 |
| 合规报告 | 变更记录、审计报告导出 |
| 趋势分析 | 架构演进历史趋势 |

### 9.2 API接口设计

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 系统统计 | GET | `/api/v1/report/statistics/systems` | 系统统计 |
| 技术分布 | GET | `/api/v1/report/statistics/tech-distribution` | 技术分布 |
| 资源统计 | GET | `/api/v1/report/statistics/resources` | 资源统计 |
| 部门分布 | GET | `/api/v1/report/statistics/department` | 部门分布 |
| 系统全景图 | GET | `/api/v1/report/panorama/systems` | 系统分布数据 |
| 技术栈全景图 | GET | `/api/v1/report/panorama/tech-stacks` | 技术分布数据 |
| 数据分布全景图 | GET | `/api/v1/report/panorama/data-distribution` | 数据分布数据 |
| 变更记录 | GET | `/api/v1/report/compliance/change-records` | 变更记录 |
| 审计报告 | GET | `/api/v1/report/compliance/audit-report` | 审计报告 |
| 导出报告 | POST | `/api/v1/report/export` | 导出Excel/PDF |
| 趋势数据 | GET | `/api/v1/report/trend/{type}` | 趋势数据 |

**系统统计** `GET /api/v1/report/statistics/systems`

Response:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 156,
    "byStatus": {
      "ONLINE": 120,
      "DEVELOPMENT": 20,
      "DEPRECATED": 16
    },
    "byImportance": {
      "核心": 25,
      "重要": 45,
      "一般": 60,
      "辅助": 26
    },
    "byLifecycle": {
      "PLANNING": 10,
      "DEVELOPMENT": 20,
      "TESTING": 15,
      "PRODUCTION": 105,
      "DEPRECATED": 6
    },
    "byDepartment": [
      {"departmentName": "信息科技部", "count": 80},
      {"departmentName": "渠道业务部", "count": 30}
    ]
  }
}
```

**系统全景图** `GET /api/v1/report/panorama/systems`

Response:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "nodes": [
      {"id": 1, "name": "核心银行系统", "group": "交易", "status": "ONLINE", "importance": "核心"},
      {"id": 2, "name": "渠道管理系统", "group": "渠道", "status": "ONLINE", "importance": "重要"}
    ],
    "links": [
      {"source": 1, "target": 2, "type": "dependency", "label": "同步调用"}
    ]
  }
}
```

---

## 十、二期功能（预告）

### 10.1 监控告警

- 基础设施状态监控（心跳检测、资源使用率）
- 架构变更监控（CMDB变更实时通知）
- 告警规则配置
- 告警通知（站内信、邮件）

### 10.2 变更全流程

- 变更申请（关联CMDB变更项）
- 变更审批（基于审批流程）
- 变更执行（执行记录、打回机制）
- 变更验证（通过/不通过）
- 变更完成归档

---

## 十一、附录

### 11.1 错误码规范

| 错误码区间 | 模块 |
|-----------|------|
| 1000-1999 | 公共/系统 |
| 2000-2999 | CMDB |
| 3000-3999 | 应用架构 |
| 4000-4999 | 技术架构 |
| 5000-5999 | 数据架构 |
| 6000-6999 | 审批流程 |
| 7000-7999 | 报表 |

### 11.2 命名规范

- 表名：`模块_实体`（如 `cmdb_server`、`arch_application`）
- 字段名：蛇形命名（如 `create_time`、`app_name`）
- API路径：`/api/v1/模块/资源`
- 角色编码：大写下划线（如 `ROLE_ADMIN`、`ROLE_ARCHITECT`）

### 11.3 索引设计

| 表名 | 索引字段 | 索引类型 | 说明 |
|------|---------|---------|------|
| cmdb_server | asset_code | UNIQUE | 资源编码唯一 |
| cmdb_server | ip_address | INDEX | IP查询 |
| cmdb_server | department_id | INDEX | 部门筛选 |
| cmdb_server | team_id | INDEX | 团队筛选 |
| cmdb_network | asset_code | UNIQUE | 资源编码唯一 |
| cmdb_network | department_id | INDEX | 部门筛选 |
| cmdb_network | team_id | INDEX | 团队筛选 |
| cmdb_storage | asset_code | UNIQUE | 资源编码唯一 |
| cmdb_storage | department_id | INDEX | 部门筛选 |
| cmdb_storage | team_id | INDEX | 团队筛选 |
| cmdb_cloud_resource | resource_code | UNIQUE | 资源编码唯一 |
| cmdb_cloud_resource | department_id | INDEX | 部门筛选 |
| cmdb_cloud_resource | team_id | INDEX | 团队筛选 |
| arch_application | app_code | UNIQUE | 应用编码唯一 |
| arch_application | department_id | INDEX | 部门筛选 |
| arch_application | team_id | INDEX | 团队筛选 |
| arch_application | lifecycle | INDEX | 生命周期筛选 |
| arch_application_module | app_id | INDEX | 应用查询 |
| arch_application_module | team_id | INDEX | 团队筛选 |
| arch_service | app_id | INDEX | 应用查询 |
| arch_service | team_id | INDEX | 团队筛选 |
| arch_dependency | source_app_id, target_app_id | INDEX | 依赖查询 |
| arch_tech_stack | stack_code | UNIQUE | 技术编码唯一 |
| arch_data_entity | app_id | INDEX | 应用查询 |
| arch_data_flow | source_app_id | INDEX | 源应用查询 |
| arch_data_flow | target_app_id | INDEX | 目标应用查询 |
| wf_instance | instance_code | UNIQUE | 实例编码唯一 |
| wf_instance | applicant_id | INDEX | 申请人查询 |
| wf_instance | status | INDEX | 状态筛选 |
| wf_task | instance_id | INDEX | 实例查询 |
| wf_task | assignee_id | INDEX | 审批人查询 |
| wf_task | status | INDEX | 状态筛选 |
| sys_user | username | UNIQUE | 用户名唯一 |
| sys_user | department_id | INDEX | 部门筛选 |
| sys_user | team_id | INDEX | 团队筛选 |
| sys_role | role_code | UNIQUE | 角色编码唯一 |
| sys_menu | menu_code | UNIQUE | 菜单编码唯一 |
| sys_department | department_code | UNIQUE | 部门编码唯一 |
| sys_team | team_code | UNIQUE | 团队编码唯一 |
| sys_team | department_id | INDEX | 部门筛选 |
| sys_dict | dict_code | UNIQUE | 字典编码唯一 |

---

### 11.4 完整数据表清单

| 序号 | 表名 | 说明 |
|------|------|------|
| 1 | cmdb_server | 服务器表 |
| 2 | cmdb_network | 网络设备表 |
| 3 | cmdb_storage | 存储设备表 |
| 4 | cmdb_cloud_resource | 云资源表 |
| 5 | cmdb_asset_change_log | 资产变更日志表 |
| 6 | arch_application | 应用系统表 |
| 7 | arch_application_module | 应用模块表 |
| 8 | arch_service | 服务表 |
| 9 | arch_dependency | 依赖关系表 |
| 10 | arch_application_server | 应用-服务器关联表 |
| 11 | arch_application_cloud | 应用-云资源关联表 |
| 12 | arch_application_network | 应用-网络设备关联表 |
| 13 | arch_application_storage | 应用-存储关联表 |
| 14 | arch_tech_stack | 技术栈字典表 |
| 15 | arch_deployment_architecture | 部署架构表 |
| 16 | arch_data_entity | 数据实体表 |
| 17 | arch_data_entity_field | 数据实体字段表 |
| 18 | arch_data_flow | 数据流向表 |
| 19 | wf_definition | 流程定义表 |
| 20 | wf_definition_node | 流程节点表 |
| 21 | wf_instance | 流程实例表 |
| 22 | wf_task | 审批任务表 |
| 23 | wf_task_candidate | 审批任务候选人表 |
| 24 | wf_history | 审批历史表 |
| 25 | sys_user | 用户表 |
| 26 | sys_role | 角色表 |
| 27 | sys_menu | 菜单表 |
| 28 | sys_permission | 权限表 |
| 29 | sys_user_role | 用户角色关联表 |
| 30 | sys_role_permission | 角色权限关联表 |
| 31 | sys_operation_log | 操作日志表 |
| 32 | sys_department | 部门表 |
| 33 | sys_team | 团队表 |
| 34 | sys_dict | 字典表 |
| 35 | sys_dict_item | 字典项表 |
| 36 | sys_file_record | 文件操作记录表 |
| 37 | sys_message | 消息通知表 |

**合计**：37张数据表

---

**审核意见**：

- [ ] 同意按此设计执行
- [ ] 需要修改（请注明）：__________________
