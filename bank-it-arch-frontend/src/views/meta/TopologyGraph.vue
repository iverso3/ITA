<template>
  <div class="topology-graph">
    <!-- 顶部工具栏 -->
    <div class="graph-toolbar">
      <div class="toolbar-left">
        <el-select v-model="selectedEntityId" placeholder="选择起始节点" @change="handleEntityChange" filterable>
          <el-option v-for="e in entityList" :key="e.id" :label="e.entityName" :value="e.id" />
        </el-select>
        <el-select v-model="graphType" placeholder="查询类型">
          <el-option label="递归查询下游" value="downstream" />
          <el-option label="递归查询上游" value="upstream" />
          <el-option label="双向查询" value="bidirectional" />
          <el-option label="最短路径" value="shortestPath" />
          <el-option label="环检测" value="cycleDetection" />
        </el-select>
        <el-input-number v-model="maxDepth" :min="1" :max="10" placeholder="最大深度" />
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="handleQuery" :loading="loading">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="handleExport">导出</el-button>
        <el-button @click="handleZoomIn">放大</el-button>
        <el-button @click="handleZoomOut">缩小</el-button>
        <el-button @click="handleFitView">适应视图</el-button>
      </div>
    </div>

    <!-- 布局方向 -->
    <div class="layout-options">
      <el-radio-group v-model="layoutDirection" size="small">
        <el-radio-button label="TB">从上到下</el-radio-button>
        <el-radio-button label="LR">从左到右</el-radio-button>
        <el-radio-button label="BT">从下到上</el-radio-button>
        <el-radio-button label="RL">从右到左</el-radio-button>
      </el-radio-group>
      <el-checkbox v-model="showLabels" style="margin-left: 16px">显示关系标签</el-checkbox>
      <el-checkbox v-model="showNodeType">显示节点类型</el-checkbox>
      <el-checkbox v-model="highlightCycles" v-if="graphType === 'cycleDetection'">高亮环</el-checkbox>
    </div>

    <!-- 拓扑图容器 -->
    <div class="graph-container" ref="graphContainer">
      <div v-if="!selectedEntityId" class="empty-tip">
        <el-empty description="请选择起始节点并查询" />
      </div>
      <div v-else-if="loading" class="loading-tip">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在加载拓扑数据...</span>
      </div>
      <div v-else-if="graphData.nodes.length === 0" class="empty-tip">
        <el-empty description="未查询到相关数据" />
      </div>
    </div>

    <!-- 图例 -->
    <div class="graph-legend" v-if="graphData.nodes.length > 0">
      <div class="legend-title">图例</div>
      <div class="legend-items">
        <div class="legend-item" v-for="type in nodeTypes" :key="type">
          <span class="legend-color" :style="{ background: getNodeColor(type) }"></span>
          <span class="legend-label">{{ type }}</span>
        </div>
      </div>
    </div>

    <!-- 节点信息面板 -->
    <el-drawer v-model="nodePanelVisible" title="节点详情" size="400px" direction="rtl">
      <div v-if="selectedNode" class="node-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="节点名称">{{ selectedNode.name }}</el-descriptions-item>
          <el-descriptions-item label="节点类型">{{ selectedNode.type }}</el-descriptions-item>
          <el-descriptions-item label="节点编码">{{ selectedNode.code }}</el-descriptions-item>
          <el-descriptions-item label="实体ID">{{ selectedNode.id }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>关联关系</el-divider>

        <div class="relation-list">
          <div v-for="rel in selectedNodeRelations" :key="rel.id" class="relation-item">
            <el-tag :type="rel.direction === 'OUT' ? 'success' : 'warning'" size="small">
              {{ rel.direction === 'OUT' ? '流出' : '流入' }}
            </el-tag>
            <span class="rel-name">{{ rel.relationName }}</span>
            <span class="rel-target">{{ rel.targetName || rel.sourceName }}</span>
          </div>
          <div v-if="selectedNodeRelations.length === 0" class="no-relation">
            暂无关联关系
          </div>
        </div>

        <el-divider>操作</el-divider>
        <div class="node-actions">
          <el-button type="primary" @click="handleViewNode(selectedNode)">查看详情</el-button>
          <el-button @click="handleExpandNode(selectedNode)">展开邻居</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 路径信息面板 -->
    <el-drawer v-model="pathPanelVisible" title="路径信息" size="400px" direction="ltr">
      <div v-if="pathData.length > 0" class="path-list">
        <el-card v-for="(path, index) in pathData" :key="index" class="path-card">
          <template #header>
            <span>路径 {{ index + 1 }} (长度: {{ path.length - 1 }})</span>
          </template>
          <div class="path-nodes">
            <template v-for="(node, nodeIndex) in path" :key="node.id">
              <span class="path-node" :class="{ 'cycle-node': node.cycle }">{{ node.name }}</span>
              <span v-if="nodeIndex < path.length - 1" class="path-arrow">→</span>
            </template>
          </div>
        </el-card>
      </div>
      <div v-else class="no-path">
        <el-empty description="未找到路径" />
      </div>
    </el-drawer>

    <!-- 环检测结果 -->
    <el-dialog v-model="cycleDialogVisible" title="环检测结果" width="600px">
      <div v-if="cycles.length > 0">
        <el-alert type="warning" :closable="false">
          检测到 {{ cycles.length }} 个环
        </el-alert>
        <div class="cycle-list">
          <div v-for="(cycle, index) in cycles" :key="index" class="cycle-item">
            <el-tag type="danger" size="small">环 {{ index + 1 }}</el-tag>
            <span class="cycle-path">{{ cycle.join(' → ') }}</span>
          </div>
        </div>
      </div>
      <el-alert v-else type="success" :closable="false">
        未检测到环
      </el-alert>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const graphContainer = ref(null)
const loading = ref(false)
const selectedEntityId = ref(null)
const graphType = ref('downstream')
const maxDepth = ref(3)
const layoutDirection = ref('TB')
const showLabels = ref(true)
const showNodeType = ref(true)
const highlightCycles = ref(true)

const entityList = ref([])
const nodePanelVisible = ref(false)
const pathPanelVisible = ref(false)
const cycleDialogVisible = ref(false)

const selectedNode = ref(null)
const selectedNodeRelations = ref([])
const pathData = ref([])
const cycles = ref([])

const graphData = reactive({
  nodes: [],
  edges: []
})

const nodeTypes = computed(() => {
  const types = new Set(graphData.nodes.map(n => n.type))
  return Array.from(types)
})

const NODE_COLORS = {
  'APPLICATION': '#409EFF',
  'SERVER': '#67C23A',
  'DATABASE': '#E6A23C',
  'MIDDLEWARE': '#F56C6C',
  'NETWORK': '#909399',
  'STORAGE': '#C0C0C0',
  'CLOUD': '#9B59B6',
  'DEFAULT': '#409EFF'
}

onMounted(() => {
  loadEntities()
})

const loadEntities = async () => {
  try {
    const res = await fetch('/api/v1/meta/entities/list')
    entityList.value = await res.json()
  } catch (error) {
    console.error('加载实体列表失败:', error)
  }
}

const handleEntityChange = () => {
  graphData.nodes = []
  graphData.edges = []
  selectedNode.value = null
}

const handleQuery = async () => {
  if (!selectedEntityId.value) {
    ElMessage.warning('请选择起始节点')
    return
  }

  loading.value = true
  try {
    const query = {
      startEntityId: selectedEntityId.value,
      queryType: graphType.value,
      maxDepth: maxDepth.value
    }

    const res = await fetch('/api/v1/meta/graph/query', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(query)
    })

    const result = await res.json()
    if (result.code === 200) {
      const data = result.data
      graphData.nodes = data.nodes || []
      graphData.edges = data.edges || []

      if (graphType.value === 'shortestPath') {
        pathData.value = data.paths || []
        pathPanelVisible.value = true
      } else if (graphType.value === 'cycleDetection') {
        cycles.value = data.cycles || []
        cycleDialogVisible.value = true
      }
    }
  } catch (error) {
    console.error('查询拓扑失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  selectedEntityId.value = null
  graphType.value = 'downstream'
  maxDepth.value = 3
  layoutDirection.value = 'TB'
  graphData.nodes = []
  graphData.edges = []
  selectedNode.value = null
  pathData.value = []
  cycles.value = []
}

const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

const handleZoomIn = () => {
  ElMessage.info('放大功能开发中')
}

const handleZoomOut = () => {
  ElMessage.info('缩小功能开发中')
}

const handleFitView = () => {
  ElMessage.info('适应视图功能开发中')
}

const handleViewNode = (node) => {
  selectedNode.value = node
  loadNodeRelations(node.id)
  nodePanelVisible.value = true
}

const handleExpandNode = async (node) => {
  const originalEntityId = selectedEntityId.value
  selectedEntityId.value = node.id
  await handleQuery()
  selectedEntityId.value = originalEntityId
}

const loadNodeRelations = async (entityId) => {
  try {
    const res = await fetch(`/api/v1/meta/graph/relations/${entityId}`)
    const result = await res.json()
    if (result.code === 200) {
      selectedNodeRelations.value = result.data || []
    }
  } catch (error) {
    console.error('加载节点关系失败:', error)
  }
}

const getNodeColor = (type) => {
  return NODE_COLORS[type] || NODE_COLORS.DEFAULT
}
</script>

<style scoped>
.topology-graph {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
}

.graph-toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 8px;
  align-items: center;
}

.layout-options {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 8px 16px;
  background: #f5f5f5;
  border-radius: 4px;
}

.graph-container {
  flex: 1;
  min-height: 400px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fafafa;
  position: relative;
}

.empty-tip,
.loading-tip {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.graph-legend {
  position: absolute;
  bottom: 16px;
  left: 16px;
  background: white;
  padding: 12px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.legend-title {
  font-weight: bold;
  margin-bottom: 8px;
}

.legend-items {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 2px;
}

.node-detail {
  padding: 16px;
}

.relation-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.relation-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
}

.rel-name {
  flex: 1;
}

.node-actions {
  display: flex;
  gap: 8px;
}

.path-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.path-nodes {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

.path-node {
  padding: 4px 8px;
  background: #409eff;
  color: white;
  border-radius: 4px;
  font-size: 12px;
}

.path-node.cycle-node {
  background: #f56c6c;
}

.path-arrow {
  color: #909399;
}

.cycle-list {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cycle-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #fef0f0;
  border-radius: 4px;
}

.cycle-path {
  font-family: monospace;
}
</style>
