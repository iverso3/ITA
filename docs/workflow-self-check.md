# 审批工作流自检报告

生成时间: 2026-03-31
审查范围: e997e7b..975157f (workflow engine implementation)

---

## 一、严重问题 (Critical - 必须修复)

### 1. 硬编码操作员ID
- **文件**: `WfTaskController.java:122,130,138,146,154,162` 和 `WfInstanceController.java:38`
- **问题**: 所有方法使用硬编码 `"1"` 和 `"admin"` 而非真实认证用户
- **影响**: 审计日志失效，所有操作都显示为同一用户
- **修复**: 使用 `@AuthenticationPrincipal` 或 SecurityContext 获取当前用户

### 2. CounterSignStrategy.checkAndProceed 从未调用
- **文件**: `WfProcessEngine.java` (handleApprove方法)
- **问题**: 会签审批后只记录了计数，但从未检查是否满足通过条件
- **影响**: 会签流程永远不会完成，审批后工作流不推进
- **修复**: 在 handleApprove 中调用 `routeStrategy.checkAndProceed()` 检查是否满足继续条件

### 3. Token.currentNodeCode 从未持久化
- **文件**: `WfProcessEngine.java:396-407` 和 `StartNodeHandler.java:81-89`
- **问题**: 引擎内部WfToken有currentNodeCode，但数据库WfToken实体只有currentNodeId
- **影响**: Token历史追踪断裂，从DB取出时nodeCode总是null
- **修复**: 要么DB实体增加currentNodeCode字段，要么从WfDefinitionNode实时查询

### 4. WfHistory.taskId 从未设置
- **文件**: `WfProcessEngine.java:421-433` (saveHistory方法)
- **问题**: saveHistory方法中从未调用 `history.setTaskId(taskId)`
- **影响**: 历史记录无法关联到具体任务，审计追溯困难
- **修复**: 添加taskId参数并在插入前设置

### 5. todoList 未按 assignee 过滤
- **文件**: `WfTaskController.java:35-65`
- **问题**: 查询条件缺少 `eq(WfTask::getAssigneeId, currentUserId)`
- **影响**: 所有用户都能看到系统中所有待办任务，数据泄露
- **修复**: 添加当前用户assigneeId过滤条件

---

## 二、重要问题 (Important - 应尽快修复)

### 6. ParallelJoinHandler 查询所有记录
- **文件**: `ParallelJoinHandler.java:84-91`
- **问题**: `selectList(null)` 无过滤条件，选取所有并行节点
- **影响**: 多实例并行运行时数据混淆，严重的性能和正确性问题
- **修复**: 按 instanceId 和 parallelNodeId 过滤

### 7. handleApprove 未检查RouteStrategy就继续流程
- **文件**: `WfProcessEngine.java:188-229`
- **问题**: 审批动作后直接调用 `continueFlow()`，未检查路由策略完成条件
- **影响**: 工作流可能在审批条件未满足时就推进
- **修复**: 审批后先调用 `routeStrategy.checkAndProceed()` 判断是否可继续

### 8. API路径不匹配
- **文件**: `WfInstanceController.java:96` vs `bank-it-arch-frontend/src/api/index.js:172`
- **问题**: 前端调用 `/history`，后端暴露 `/trace`
- **影响**: 前端获取历史记录接口404
- **修复**: 统一端点命名，建议后端改为 `/history`

### 9. CounterSignCount 误用 TimeoutDuration
- **文件**: `CounterSignStrategy.java:28` 和 `OrSignStrategy.java:30`
- **问题**: `task.setCounterSignCount(node.getTimeoutDuration())` 用超时时间作为会签数量
- **影响**: 语义混乱，配置不直观
- **修复**: 考虑增加独立的 `counterSignCount` 字段

### 10. WfToken 缺少 @TableLogic 正确使用
- **文件**: `WfToken.java` 实体
- **问题**: 实体有 @TableLogic 但部分查询未考虑软删除
- **影响**: 软删除的Token可能被错误检索
- **修复**: 审查所有Token查询，确保使用MyBatis-Plus的逻辑删除支持

---

## 三、次要问题 (Minor - 后续优化)

### 11. 撤回功能未实现
- **文件**: `WfInstanceController.java:126`
- **问题**: TODO注释存在，方法体为空
- **修复**: 实现撤回逻辑或删除该端点

### 12. ParallelBranchHandler.getNodeHandler 死代码
- **文件**: `ParallelBranchHandler.java:86-89`
- **问题**: 方法永远返回null，从未被调用
- **修复**: 删除或实现有意义的逻辑

### 13. WfInstance.businessKey 从未设置
- **文件**: `WfInstance.java` 和 `WfProcessEngine.java`
- **问题**: businessKey字段存在但从未赋值
- **修复**: 在 startProcess 时从dto获取并设置

### 14. 节点 Type/Category 命名不一致
- **文件**: `WfDefinitionNode.java`
- **问题**: 同时存在 nodeType 和 nodeCategory 字段，语义不清
- **修复**: 统一为一个字段，明确其用途

---

## 四、修复优先级建议

```
P0 (立即修复) - [已全部修复]:
  - 问题1: 硬编码操作员ID (安全+审计) ✅ 已修复 - 使用UserContext获取当前用户
  - 问题2: 会签完成检查缺失 (功能正确性) ✅ 已修复 - handleApprove中调用checkAndProceed
  - 问题5: 待办列表无权限过滤 (数据安全) ✅ 已修复 - todoList/doneList添加assigneeId过滤

P1 (尽快修复) - [已全部修复]:
  - 问题3: Token持久化不完整 ✅ 已修复 - getTokenByInstance从nodeMapper获取nodeCode
  - 问题4: 历史记录无taskId ✅ 已修复 - saveHistory方法添加taskId参数
  - 问题6: ParallelJoin查询全表 ✅ 已修复 - findParallelBranchNodeId添加instanceId过滤
  - 问题7: 审批后未检查路由策略 ✅ 已修复 - handleApprove中已调用checkAndProceed
  - 问题8: API路径不匹配 ✅ 已修复 - /trace改为/history

P2 (后续优化) - [已全部修复]:
  - 问题9: CounterSignCount误用TimeoutDuration ✅ 已修复 - 新增counterSignCount字段，迁移脚本migration_v2.3
  - 问题10: WfToken @TableLogic使用 - 已审查，无问题
  - 问题11: 撤回功能未实现 ✅ 已修复 - 实现撤回逻辑，支持取消待处理任务
  - 问题12: ParallelBranchHandler死代码 ✅ 已修复 - 删除死代码getNodeHandler()，使用context.getNodeHandlers()
  - 问题13: WfInstance.businessKey未设置 ✅ 已修复 - startProcess中设置businessKey
  - 问题14: 节点Type/Category命名不一致 - 已审查，属合理设计，保留现状
```

---

## 五、待验证功能点

1. **流程发起**: 验证新实例创建、起始节点处理、Token生成
2. **审批流转**: 验证单人审批、会签、或签、抢单各种路由策略
3. **并行分支**: 验证并行节点分发、并行节点完成汇合
4. **条件路由**: 验证条件节点根据表达式正确路由
5. **历史记录**: 验证每一步操作都正确记录到wf_history
6. **任务查询**: 验证待办、已办、实例查询的权限和数据正确性

---

## 六、测试建议

1. 单元测试: 每个NodeHandler、每个RouteStrategy单独测试
2. 集成测试: 完整的流程实例生命周期测试
3. 并发测试: 多用户同时审批同一会签节点
4. 边界测试: 空数据、异常数据、边界条件
