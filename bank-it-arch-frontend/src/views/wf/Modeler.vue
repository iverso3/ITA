<template>
  <div class="modeler-container">
    <!-- 左侧节点面板 -->
    <div class="palette-panel">
      <div class="panel-header">
        <h3>节点组件</h3>
      </div>
      <div class="palette-items">
        <div
          v-for="nodeType in workflowStore.nodeTypes"
          :key="nodeType.type"
          class="palette-item"
          :style="{ borderColor: nodeType.color }"
          draggable="true"
          @dragstart="onDragStart($event, nodeType)"
        >
          <el-icon :color="nodeType.color">
            <component :is="nodeType.icon" />
          </el-icon>
          <span>{{ nodeType.label }}</span>
        </div>
      </div>

      <!-- 流程操作 -->
      <div class="panel-actions">
        <el-button type="primary" @click="handleSave" :loading="saving">
          保存
        </el-button>
        <el-button type="success" @click="handleValidate">
          校验
        </el-button>
        <el-button type="warning" @click="handlePublish" :disabled="!isValid">
          发布
        </el-button>
      </div>
    </div>

    <!-- 中间画布 -->
    <div class="canvas-panel">
      <div class="canvas-toolbar">
        <el-button-group>
          <el-button @click="zoomIn">放大</el-button>
          <el-button @click="zoomOut">缩小</el-button>
          <el-button @click="fitView">适应画布</el-button>
        </el-button-group>
        <el-button @click="clearCanvas">清空画布</el-button>
      </div>

      <VueFlow
        v-model:nodes="nodes"
        v-model:edges="edges"
        :node-types="nodeTypes"
        :default-viewport="{ zoom: 1 }"
        fit-view-on-init
        @node-click="onNodeClick"
        @edge-click="onEdgeClick"
        @nodes-change="onNodesChange"
        @edges-change="onEdgesChange"
        @connect="handleConnect"
        @node-drag-stop="onNodeDragStop"
        @drop="handleDrop"
        @dragover.prevent
      >
        <template #node-START=" { data }">
          <StartNode :data="data" />
        </template>
        <template #node-END=" { data }">
          <EndNode :data="data" />
        </template>
        <template #node-APPROVAL=" { data }">
          <ApprovalNode :data="data" />
        </template>
        <template #node-CONDITION=" { data }">
          <ConditionNode :data="data" />
        </template>
        <template #node-PARALLEL_BRANCH=" { data }">
          <ParallelBranchNode :data="data" />
        </template>
        <template #node-PARALLEL_JOIN=" { data }">
          <ParallelJoinNode :data="data" />
        </template>

        <Background pattern-color="#aaa" :gap="16" />
        <Controls />
        <MiniMap />
      </VueFlow>
    </div>

    <!-- 右侧属性面板 -->
    <div class="property-panel" v-if="workflowStore.selectedNode || workflowStore.selectedLine">
      <div class="panel-header">
        <h3>属性配置</h3>
        <el-button text @click="workflowStore.clearSelection">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <!-- 节点属性 -->
      <div v-if="workflowStore.selectedNode" class="property-form">
        <el-form label-width="100px" size="small">
          <el-form-item label="节点编码">
            <el-input v-model="workflowStore.selectedNode.data.nodeCode" />
          </el-form-item>
          <el-form-item label="节点名称">
            <el-input v-model="workflowStore.selectedNode.data.nodeName" />
          </el-form-item>
          <el-form-item label="节点类型">
            <el-input :value="workflowStore.selectedNode.data.nodeCategory" disabled />
          </el-form-item>

          <!-- 审批节点特有属性 -->
          <template v-if="workflowStore.selectedNode.data.nodeCategory === 'APPROVAL'">
            <el-divider>审批配置</el-divider>
            <el-form-item label="审批方式">
              <el-select v-model="workflowStore.selectedNode.data.approvalType">
                <el-option
                  v-for="type in workflowStore.approvalTypes"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="审批角色">
              <el-select v-model="workflowStore.selectedNode.data.flowRoleId" placeholder="请选择流程角色">
                <el-option
                  v-for="role in flowRoles"
                  :key="role.flowRoleId"
                  :label="`${role.flowRoleId}-${role.flowRoleName}`"
                  :value="role.flowRoleId"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="超时时长">
              <el-input-number
                v-model="workflowStore.selectedNode.data.timeoutDuration"
                :min="1"
                :max="10000"
              />
              <span class="ml-2">分钟</span>
            </el-form-item>
            <el-form-item label="超时动作">
              <el-select v-model="workflowStore.selectedNode.data.timeoutAction">
                <el-option label="自动通过" value="AUTO_APPROVE" />
                <el-option label="自动跳过" value="SKIP" />
                <el-option label="转交他人" value="TRANSFER" />
              </el-select>
            </el-form-item>
          </template>

          <!-- 条件节点特有属性 -->
          <template v-if="workflowStore.selectedNode.data.nodeCategory === 'CONDITION'">
            <el-divider>条件配置</el-divider>
            <div class="condition-config">
              <div class="condition-tip">当流转到该节点时，根据条件表达式计算结果决定下一节点</div>

              <!-- 条件列表 -->
              <div class="condition-list">
                <div v-for="(cond, index) in conditionConfig.conditions" :key="index" class="condition-item">
                  <div class="condition-header">
                    <span class="condition-label">条件 {{ index + 1 }}</span>
                    <el-button type="danger" size="small" text @click="removeCondition(index)">删除</el-button>
                  </div>
                  <el-form-item label="标签" size="small">
                    <el-input v-model="cond.label" placeholder="如：金额大于1万" />
                  </el-form-item>
                  <el-form-item label="表达式" size="small">
                    <el-input v-model="cond.expression" placeholder="如：amount > 10000" />
                  </el-form-item>
                  <el-form-item label="目标节点" size="small">
                    <el-select v-model="cond.targetNodeId" placeholder="选择目标节点">
                      <el-option
                        v-for="node in availableTargetNodes"
                        :key="node.id"
                        :label="node.data.nodeName"
                        :value="node.id"
                      />
                    </el-select>
                  </el-form-item>
                </div>
              </div>

              <!-- 添加条件按钮 -->
              <el-button type="primary" plain size="small" @click="addCondition" :disabled="availableTargetNodes.length === 0">
                + 添加条件
              </el-button>

              <!-- 默认节点 -->
              <el-form-item label="默认节点" size="small" style="margin-top: 12px;">
                <el-select v-model="conditionConfig.defaultNodeId" placeholder="无匹配条件时走默认节点">
                  <el-option label="无（不流转）" :value="null" />
                  <el-option
                    v-for="node in availableTargetNodes"
                    :key="node.id"
                    :label="node.data.nodeName"
                    :value="node.id"
                  />
                </el-select>
              </el-form-item>

              <!-- 预览JSON -->
              <div class="condition-preview">
                <div class="preview-label">JSON预览：</div>
                <pre class="preview-code">{{ conditionRuleJson }}</pre>
              </div>
            </div>
          </template>
        </el-form>

        <div class="property-actions">
          <el-button type="danger" @click="deleteNode">删除节点</el-button>
        </div>
      </div>

      <!-- 连线属性 -->
      <div v-if="workflowStore.selectedLine" class="property-form">
        <el-form label-width="80px" size="small">
          <el-form-item label="连线名称">
            <el-input v-model="workflowStore.selectedLine.data.lineName" />
          </el-form-item>
          <el-form-item label="条件">
            <el-input
              v-model="workflowStore.selectedLine.data.conditionExpr"
              type="textarea"
              :rows="3"
              placeholder="如: amount > 10000"
            />
          </el-form-item>
        </el-form>

        <div class="property-actions">
          <el-button type="danger" @click="deleteEdge">删除连线</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useWorkflowStore } from '@/store/workflow'
import { VideoPlay, CircleClose, User, Switch, Connection, ArrowRight, Close } from '@element-plus/icons-vue'
import { flowRoleApi, wfApi } from '@/api'

// 导入节点组件
import StartNode from '@/components/wf/nodes/StartNode.vue'
import EndNode from '@/components/wf/nodes/EndNode.vue'
import ApprovalNode from '@/components/wf/nodes/ApprovalNode.vue'
import ConditionNode from '@/components/wf/nodes/ConditionNode.vue'
import ParallelBranchNode from '@/components/wf/nodes/ParallelBranchNode.vue'
import ParallelJoinNode from '@/components/wf/nodes/ParallelJoinNode.vue'

const workflowStore = useWorkflowStore()
const route = useRoute()
const router = useRouter()

const { zoomIn, zoomOut, fitView, addNodes, addEdges } = useVueFlow()

const nodes = ref([])
const edges = ref([])
const saving = ref(false)
const isValid = ref(false)
const flowRoles = ref([])

// 节点类型映射
const nodeTypes = {
  START: 'START',
  END: 'END',
  APPROVAL: 'APPROVAL',
  CONDITION: 'CONDITION',
  PARALLEL_BRANCH: 'PARALLEL_BRANCH',
  PARALLEL_JOIN: 'PARALLEL_JOIN'
}

// ===== 条件节点配置 =====
// 可作为条件分支目标节点的列表（排除自身和开始节点）
const availableTargetNodes = computed(() => {
  const selectedId = workflowStore.selectedNode?.id
  if (!selectedId) return []
  return nodes.value.filter(n =>
    n.id !== selectedId &&
    n.data.nodeCategory !== 'START' &&
    n.data.nodeCategory !== 'CONDITION'
  )
})

// 条件配置 - 使用独立的响应式对象
import { watch, reactive } from 'vue'

const conditionConfig = reactive({
  conditions: [],
  defaultNodeId: null
})

// 监听选中节点变化，加载条件规则
watch(
  () => workflowStore.selectedNode?.id,
  () => {
    const rule = workflowStore.selectedNode?.data?.conditionRule
    if (rule) {
      try {
        const parsed = JSON.parse(rule)
        conditionConfig.conditions = parsed.conditions || []
        conditionConfig.defaultNodeId = parsed.defaultNodeId || null
      } catch {
        conditionConfig.conditions = []
        conditionConfig.defaultNodeId = null
      }
    } else {
      conditionConfig.conditions = []
      conditionConfig.defaultNodeId = null
    }
  },
  { immediate: true }
)

// 监听条件配置变化，同步到节点数据
watch(
  () => ({ ...conditionConfig }),
  (val) => {
    if (workflowStore.selectedNode?.data) {
      workflowStore.selectedNode.data.conditionRule = JSON.stringify({
        conditions: val.conditions || [],
        defaultNodeId: val.defaultNodeId || null
      })
    }
  },
  { deep: true }
)

// JSON预览
const conditionRuleJson = computed(() => {
  try {
    return JSON.stringify({
      conditions: conditionConfig.conditions || [],
      defaultNodeId: conditionConfig.defaultNodeId || null
    }, null, 2)
  } catch {
    return '{}'
  }
})

// 添加条件
function addCondition() {
  conditionConfig.conditions.push({ label: '', expression: '', targetNodeId: null })
}

// 删除条件
function removeCondition(index) {
  conditionConfig.conditions.splice(index, 1)
}

// 修正conditionRule中的临时ID为真实ID
function correctConditionRule(conditionRule, nodeIdMap) {
  if (!conditionRule) return null
  try {
    const rule = JSON.parse(conditionRule)
    if (rule.conditions) {
      rule.conditions.forEach(cond => {
        if (cond.targetNodeId && nodeIdMap[cond.targetNodeId]) {
          console.log(`[correctConditionRule] Replacing targetNodeId ${cond.targetNodeId} -> ${nodeIdMap[cond.targetNodeId]}`)
          cond.targetNodeId = nodeIdMap[cond.targetNodeId]
        } else if (cond.targetNodeId) {
          console.log(`[correctConditionRule] targetNodeId ${cond.targetNodeId} NOT FOUND in nodeIdMap, keeping original`)
        }
      })
    }
    if (rule.defaultNodeId && nodeIdMap[rule.defaultNodeId]) {
      rule.defaultNodeId = nodeIdMap[rule.defaultNodeId]
    }
    return JSON.stringify(rule)
  } catch (e) {
    console.error('Failed to correct conditionRule:', e)
    return conditionRule
  }
}

// 拖拽开始
function onDragStart(event, nodeType) {
  event.dataTransfer.setData('nodeType', JSON.stringify(nodeType))
  event.dataTransfer.effectAllowed = 'move'
}

// 处理放置节点
function handleDrop(event) {
  event.preventDefault()

  const nodeTypeStr = event.dataTransfer.getData('nodeType')
  if (!nodeTypeStr) return

  try {
    const nodeType = JSON.parse(nodeTypeStr)

    // 获取画布位置
    const canvas = event.target.closest('.vue-flow')
    if (!canvas) return

    const rect = canvas.getBoundingClientRect()
    const position = {
      x: event.clientX - rect.left,
      y: event.clientY - rect.top
    }

    // 生成唯一ID
    const nodeId = `node_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`

    // 创建新节点
    const newNode = {
      id: nodeId,
      type: nodeType.type,
      position: position,
      data: {
        nodeCode: `${nodeType.type.toLowerCase()}_${Date.now()}`,
        nodeName: nodeType.label,
        nodeCategory: nodeType.type,
        approvalType: 'NORMAL',
        flowRoleId: null,
        timeoutDuration: 30,
        timeoutAction: 'AUTO_APPROVE'
      }
    }

    nodes.value = [...nodes.value, newNode]
    workflowStore.markDirty()
  } catch (error) {
    console.error('Failed to add node:', error)
  }
}

// 放置节点
function onNodeDragStop(event) {
  workflowStore.markDirty()
}

// 处理节点连接
function handleConnect(params) {
  const edgeId = `edge_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  const newEdge = {
    id: edgeId,
    source: params.source,
    target: params.target,
    sourceHandle: params.sourceHandle,
    targetHandle: params.targetHandle,
    type: 'default',
    data: {
      lineName: '',
      conditionExpr: ''
    }
  }

  edges.value = [...edges.value, newEdge]
  workflowStore.markDirty()
}

// 节点点击
function onNodeClick(event) {
  workflowStore.selectNode(event.node)
}

// 连线点击
function onEdgeClick(event) {
  workflowStore.selectLine(event.edge)
}

// 节点变化
function onNodesChange(changes) {
  changes.forEach(change => {
    if (change.type === 'position' || change.type === 'remove') {
      workflowStore.markDirty()
    }
  })
}

// 连线变化
function onEdgesChange(changes) {
  changes.forEach(change => {
    if (change.type === 'remove') {
      workflowStore.markDirty()
    }
  })
}

// 删除节点
function deleteNode() {
  const node = workflowStore.selectedNode
  if (!node) return

  ElMessageBox.confirm('确定要删除该节点吗？', '提示', {
    type: 'warning'
  }).then(() => {
    nodes.value = nodes.value.filter(n => n.id !== node.id)
    // 同时删除关联的连线
    edges.value = edges.value.filter(
      e => e.source !== node.id && e.target !== node.id
    )
    workflowStore.clearSelection()
    workflowStore.markDirty()
  })
}

// 删除连线
function deleteEdge() {
  const edge = workflowStore.selectedLine
  if (!edge) {
    console.error('没有选中的连线', workflowStore.selectedLine)
    return
  }

  ElMessageBox.confirm('确定要删除该连线吗？', '提示', {
    type: 'warning'
  }).then(() => {
    console.log('删除连线:', edge.id, '当前edges:', edges.value.map(e => e.id))
    const newEdges = edges.value.filter(e => e.id !== edge.id)
    console.log('删除后edges:', newEdges.map(e => e.id))
    edges.value = newEdges
    workflowStore.clearSelection()
    workflowStore.markDirty()
  }).catch(err => {
    console.error('删除失败:', err)
  })
}

// 清空画布
function clearCanvas() {
  ElMessageBox.confirm('确定要清空画布吗？', '提示', {
    type: 'warning'
  }).then(() => {
    nodes.value = []
    edges.value = []
    workflowStore.clearSelection()
    workflowStore.markDirty()
  })
}

// 保存
async function handleSave() {
  saving.value = true
  try {
    const definitionId = route.params.id || 0

    console.log('[handleSave] nodes.value:', nodes.value.map(n => ({
      id: n.id,
      name: n.data.nodeName,
      category: n.data.nodeCategory,
      conditionRule: n.data.conditionRule
    })))

    // 构建节点列表，保持与nodes.value相同的顺序
    const nodeList = nodes.value.map((n, index) => ({
      id: n.id.startsWith('node_') ? null : n.id,
      nodeCode: n.data.nodeCode,
      nodeName: n.data.nodeName,
      nodeCategory: n.data.nodeCategory,
      approvalType: n.data.approvalType,
      flowRoleId: n.data.flowRoleId,
      timeoutDuration: n.data.timeoutDuration,
      timeoutAction: n.data.timeoutAction,
      conditionRule: n.data.conditionRule,
      positionX: n.position.x,
      positionY: n.position.y,
      nodeOrder: n.data.nodeOrder !== undefined ? n.data.nodeOrder : index
    }))

    const saveNodesResult = await workflowStore.saveNodes(definitionId, nodeList)

    // 建立临时ID到真实ID的映射，并用真实ID更新节点
    const nodeIdMap = {}
    if (saveNodesResult && saveNodesResult.data) {
      saveNodesResult.data.forEach((savedNode, index) => {
        const originalNode = nodes.value[index]
        if (originalNode) {
          // 更新节点ID为真实ID
          const oldId = originalNode.id
          const newId = String(savedNode.id)
          nodeIdMap[oldId] = newId
          originalNode.id = newId
        }
      })
    }

    // 用真实ID更新conditionRule中的targetNodeId，并保存到数据库
    for (const n of nodes.value) {
      if (n.data.conditionRule) {
        const corrected = correctConditionRule(n.data.conditionRule, nodeIdMap)
        n.data.conditionRule = corrected
        // 调用API更新数据库中的conditionRule
        if (!n.id.startsWith('node_')) {
          await wfApi.modeler.updateNodeConditionRule(definitionId, n.id, corrected)
        }
      }
    }

    // 用真实ID更新连线的source和target

    // 用真实ID更新连线的source和target
    edges.value.forEach(e => {
      if (nodeIdMap[e.source]) {
        e.source = nodeIdMap[e.source]
      }
      if (nodeIdMap[e.target]) {
        e.target = nodeIdMap[e.target]
      }
    })

    // 使用真实ID保存连线，使用索引作为lineOrder保证顺序
    await workflowStore.saveLines(definitionId, edges.value.map((e, index) => ({
      sourceNodeId: e.source,
      targetNodeId: e.target,
      lineName: e.data?.lineName,
      conditionExpr: e.data?.conditionExpr,
      lineOrder: index
    })))

    ElMessage.success('保存成功')
  } catch (error) {
    console.error('Save failed:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 校验
async function handleValidate() {
  const model = {
    nodes: nodes.value.map(n => ({
      id: n.id,
      nodeCategory: n.data.nodeCategory
    })),
    lines: edges.value.map(e => ({
      source: e.source,
      target: e.target
    }))
  }

  try {
    await workflowStore.validateModel(model)
    ElMessage.success('校验通过')
    isValid.value = true
  } catch (error) {
    ElMessage.error('校验失败：' + (error.message || '模型不完整'))
    isValid.value = false
  }
}

// 发布
async function handlePublish() {
  try {
    const { value: changeLog } = await ElMessageBox.prompt('请输入变更日志', '发布流程', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    const definitionId = route.params.id
    await workflowStore.publishModel(definitionId, changeLog)
    ElMessage.success('发布成功')

    // 发布成功后返回流程定义列表
    router.push('/wf/definition')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
      ElMessage.error('发布失败')
    }
  }
}

// 加载模型
onMounted(async () => {
  // 加载流程角色列表
  try {
    const res = await flowRoleApi.listAll()
    if (res.code === 200) {
      flowRoles.value = res.data || []
    }
  } catch (error) {
    console.error('Failed to load flow roles:', error)
  }

  const definitionId = route.params.id
  if (definitionId) {
    try {
      const model = await workflowStore.loadModel(definitionId)
      // 转换为Vue Flow格式，根据nodeOrder自动排布节点
      nodes.value = model.nodes.map((n, index) => ({
        id: String(n.id),
        type: n.nodeCategory,
        position: {
          x: n.positionX !== null && n.positionX !== undefined ? n.positionX : 250 + (index % 3) * 200,
          y: n.positionY !== null && n.positionY !== undefined ? n.positionY : 100 + Math.floor(index / 3) * 150
        },
        data: {
          nodeCode: n.nodeCode,
          nodeName: n.nodeName,
          nodeCategory: n.nodeCategory,
          approvalType: n.approvalType,
          flowRoleId: n.flowRoleId,
          timeoutDuration: n.timeoutDuration,
          timeoutAction: n.timeoutAction,
          conditionRule: n.conditionRule,
          nodeOrder: n.nodeOrder !== undefined ? n.nodeOrder : index
        }
      }))

      edges.value = model.lines.map((l, index) => ({
        id: `edge_${l.id || index}`,
        source: String(l.sourceNodeId),
        target: String(l.targetNodeId),
        data: {
          lineName: l.lineName,
          conditionExpr: l.conditionExpr
        }
      }))
    } catch (error) {
      // 新建流程
    }
  }
})
</script>

<style scoped>
.modeler-container {
  display: flex;
  height: calc(100vh - 140px);
  background: #f5f5f5;
}

.palette-panel {
  width: 200px;
  background: white;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.palette-items {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.palette-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  margin-bottom: 8px;
  background: #fafafa;
  border: 2px solid #ddd;
  border-radius: 8px;
  cursor: move;
  transition: all 0.2s;
}

.palette-item:hover {
  background: #f0f0f0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.palette-item span {
  font-size: 13px;
}

.panel-actions {
  padding: 16px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.panel-actions .el-button {
  width: 100%;
}

.canvas-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.canvas-toolbar {
  padding: 8px 16px;
  background: white;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  gap: 8px;
}

.property-panel {
  width: 320px;
  background: white;
  border-left: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.property-form {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.property-actions {
  padding: 16px;
  border-top: 1px solid #e0e0e0;
}

.ml-2 {
  margin-left: 8px;
}

:deep(.vue-flow) {
  flex: 1;
}

:deep(.vue-flow__node) {
  cursor: pointer;
}

/* 条件节点配置样式 */
.condition-config {
  padding: 8px 0;
}

.condition-tip {
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
  line-height: 1.4;
}

.condition-list {
  margin-bottom: 12px;
}

.condition-item {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 8px;
}

.condition-item .el-form-item {
  margin-bottom: 8px;
}

.condition-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.condition-label {
  font-size: 12px;
  font-weight: 500;
  color: #303133;
}

.condition-preview {
  margin-top: 12px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

.preview-label {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.preview-code {
  font-size: 11px;
  color: #409eff;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 120px;
  overflow-y: auto;
}
</style>
