# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在本仓库中工作时提供指导。

## 项目概述

银行IT架构管理平台 - 用于管理银行IT基础设施、应用架构、技术栈、数据流和审批流程的综合管理平台。

**项目代码位于仓库根目录：**
- 后端：`bank-it-arch-backend/`
- 前端：`bank-it-arch-frontend/`
- 数据库：`database/`
- 文档：`docs/`

## 构建与开发命令

### 后端 (Spring Boot 3.2.0 + Java 17)
```bash
cd bank-it-arch-backend
mvn compile                    # 编译
mvn spring-boot:run            # 本地运行
```

### 前端 (Vue 3 + Vite)
```bash
cd bank-it-arch-frontend
npm install                    # 安装依赖
npm run dev                    # 开发服务器 (http://localhost:5173)
npm run build                  # 生产构建
```

### 数据库
```bash
# MySQL 8.0 初始化
mysql -u root -p < database/init.sql
```

## 架构

### 后端 (Spring Boot)
```
com.bank.itarch/
├── common/           # 通用：Result<T>, PageQuery, PageResult, BusinessException
├── config/           # 配置类
├── controller/       # REST控制器 (10个控制器)
├── mapper/           # MyBatis-Plus映射器
├── model/
│   ├── entity/       # 实体类，继承BaseEntity
│   ├── dto/          # 数据传输对象
│   └── vo/           # 视图对象
├── service/          # 业务逻辑 (Impl后缀模式)
└── util/             # 工具类
```

**关键模式：**
- 所有实体继承 `BaseEntity`（id, createTime, updateTime, creator, modifier, deleted）
- 逻辑删除通过 `@TableLogic` 注解实现
- Service实现类使用 `ServiceImpl<Mapper, Entity>` 模式
- 控制器返回 `Result<T>` 包装器 `{code: 200, message: "success", data: ...}`
- 密码加密使用 `cn.hutool.crypto.SecureUtil.md5()`
- 树形结构（部门、菜单、技术栈）使用 `parentId` 实现层级

**API基础路径：** `/api/v1`

### 前端 (Vue 3 + Element Plus)
```
src/
├── api/index.js      # 所有API客户端 (基于axios)
├── router/           # Vue Router配置
├── store/            # Pinia状态管理
├── views/            # 页面组件，按模块分类
│   ├── arch/         # 应用架构
│   ├── cmdb/         # 基础设施管理
│   ├── data/         # 数据架构
│   ├── report/       # 报表与全景图
│   ├── system/       # 用户、角色、部门、团队、菜单、权限、字典、日志
│   ├── tech/         # 技术架构
│   └── wf/           # 工作流 (待办、已办)
└── utils/            # 工具函数
```

**API响应格式：**
```javascript
// 成功响应
{ code: 200, message: "success", data: {...} }
// 前端axios拦截器自动提取.data
```

**分页：** 使用 `PageQuery`（page, size, keyword）和 `PageResult`（records, total, pages）

## 模块

| 模块 | 后端 | 前端 |
|------|------|------|
| CMDB (服务器/网络/存储/云资源) | Cmdb*Controller | Servers.vue, Network.vue, Storage.vue, Cloud.vue |
| 应用架构 | ArchApplicationController | Application.vue |
| 技术架构 | TechStackController | TechOverview.vue |
| 数据架构 | DataArchController | Entity.vue |
| 工作流 | WorkflowController | Todo.vue, Done.vue |
| 系统管理 | SystemController | User, Role, Department, Team, Menu, Permission, Dict, Log |
| 报表中心 | ReportController | Statistics.vue, Panorama.vue |

## 技术栈

- **后端：** Spring Boot 3.2.0, MyBatis-Plus 3.5.5, MySQL 8.0, Redis, JWT (jjwt 0.12.3), Knife4j (API文档), Hutool 5.8.23
- **前端：** Vue 3.4, Element Plus 2.5.5, ECharts 5.4.3, Pinia 2.1.7, Vue Router 4.2.5, Axios 1.13.6

## 开发注意事项

### 修改代码后必须验证现有功能
**修改任何代码后，必须确保不影响现有功能。** 每次修改后需要：
1. 执行 `npm run build`（前端）或 `mvn compile`（后端）确保编译通过
2. 启动应用并测试相关功能路径，特别是：
   - 登录/登出流程
   - 首页/Dashboard 加载
   - 侧边栏菜单切换和 Tab 切换
   - 列表页的增删改查操作
   - 详情页的打开和关闭
3. 如果修复了一个 bug 而引入了新的 bug，必须立即回滚并重新分析

### 动态路由匹配注意
Vue Router 4 中 `router.resolve()` 对动态路由（如 `/arch/application/:id`）的行为不稳定。推荐使用自定义的正则匹配函数来查找路由：
