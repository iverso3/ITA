<template>
  <div class="version-manage">
    <!-- 模型选择 -->
    <div class="model-selector">
      <el-select v-model="selectedModelId" placeholder="选择模型" @change="handleModelChange">
        <el-option v-for="m in modelList" :key="m.id" :label="m.modelName" :value="m.id" />
      </el-select>
    </div>

    <!-- 版本列表 -->
    <el-table :data="versionList" border stripe>
      <el-table-column prop="version" label="版本号" width="100">
        <template #default="{ row }">
          <el-tag type="info">v{{ row.version }}.0</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="versionName" label="版本名称" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="changeSummary" label="变更摘要" show-overflow-tooltip />
      <el-table-column prop="creator" label="创建人" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column prop="publishedAt" label="发布时间" width="160">
        <template #default="{ row }">
          {{ row.publishedAt || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'DRAFT'">
            <el-button type="primary" size="small" @click="handlePublish(row)">发布</el-button>
            <el-button size="small" @click="handleViewDiff(row)">查看变更</el-button>
          </template>
          <template v-else-if="row.status === 'PUBLISHED'">
            <el-button type="warning" size="small" @click="handleArchive(row)">归档</el-button>
            <el-button type="info" size="small" @click="handleRollback(row)">回滚</el-button>
          </template>
          <template v-else-if="row.status === 'ARCHIVED'">
            <el-button type="info" size="small" @click="handleViewDiff(row)">查看</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <!-- 变更对比对话框 -->
    <el-dialog v-model="diffDialogVisible" title="版本变更对比" width="800px">
      <div v-if="diffData" class="diff-content">
        <el-alert type="info" :closable="false">
          对比: v{{ diffData.version1 }} vs v{{ diffData.version2 }}
        </el-alert>
        <pre>{{ JSON.stringify(diffData, null, 2) }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const modelList = ref([])
const selectedModelId = ref(null)
const versionList = ref([])
const diffDialogVisible = ref(false)
const diffData = ref(null)

onMounted(() => {
  loadModels()
})

const loadModels = async () => {
  try {
    const res = await fetch('/api/v1/meta/models/list')
    modelList.value = await res.json()
  } catch (error) {
    console.error('加载模型列表失败:', error)
  }
}

const handleModelChange = async (modelId) => {
  try {
    const res = await fetch(`/api/v1/meta/publish/history/${modelId}`)
    versionList.value = await res.json()
  } catch (error) {
    console.error('加载版本历史失败:', error)
  }
}

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确定发布该版本吗？', '提示', { type: 'warning' })
    await fetch(`/api/v1/meta/publish/submit/${selectedModelId.value}`, { method: 'POST' })
    ElMessage.success('发布成功')
    handleModelChange(selectedModelId.value)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败')
    }
  }
}

const handleArchive = async (row) => {
  try {
    await ElMessageBox.confirm('确定归档该版本吗？', '提示', { type: 'warning' })
    await fetch(`/api/v1/meta/publish/archive/${row.id}`, { method: 'POST' })
    ElMessage.success('归档成功')
    handleModelChange(selectedModelId.value)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('归档失败')
    }
  }
}

const handleRollback = async (row) => {
  try {
    await ElMessageBox.confirm('确定回滚到该版本吗？', '提示', { type: 'warning' })
    await fetch(`/api/v1/meta/publish/rollback/${selectedModelId.value}/${row.version}`, { method: 'POST' })
    ElMessage.success('回滚成功')
    handleModelChange(selectedModelId.value)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('回滚失败')
    }
  }
}

const handleViewDiff = async (row) => {
  // 获取当前版本和选定版本的对比
  ElMessage.info('版本对比功能开发中')
}

const getStatusType = (status) => {
  switch (status) {
    case 'PUBLISHED': return 'success'
    case 'DRAFT': return 'info'
    case 'TESTING': return 'warning'
    case 'ARCHIVED': return 'danger'
    default: return 'info'
  }
}
</script>

<style scoped>
.version-manage {
  padding: 16px;
}

.model-selector {
  margin-bottom: 16px;
}

.diff-content pre {
  background: #f5f5f5;
  padding: 16px;
  overflow: auto;
  max-height: 400px;
}
</style>
