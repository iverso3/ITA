<template>
  <div class="tech-deployment animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">部署架构管理</h1>
      <p class="page-header__desc">应用部署架构与节点配置</p>
    </div>
    <div class="deploy-layout">
      <div class="deploy-sidebar">
        <h4 class="sidebar-title">选择应用</h4>
        <el-input v-model="appKeyword" placeholder="搜索应用" prefix-icon="Search" clearable />
        <el-scrollbar height="calc(100vh - 320px)" style="margin-top: 12px;">
          <div
            v-for="app in filteredApps"
            :key="app.id"
            class="app-item"
            :class="{ 'app-item--active': selectedAppId === app.id }"
            @click="selectApp(app.id)"
          >
            {{ app.appName }}
          </div>
        </el-scrollbar>
      </div>
      <div class="deploy-content">
        <div class="page-card" style="flex: 1;">
          <div class="page-card__header">
            <h3><el-icon color="var(--accent)"><Management /></el-icon> 部署节点</h3>
            <el-button type="primary" size="small" :disabled="!selectedAppId" @click="handleAdd">新增节点</el-button>
          </div>
          <el-table :data="deployList" v-loading="loading" stripe>
            <el-table-column prop="layer" label="层级" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ row.layer }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="nodeName" label="节点名称" min-width="160" />
            <el-table-column prop="nodeType" label="节点类型" width="120" />
            <el-table-column prop="count" label="节点数量" width="100" align="center" />
            <el-table-column prop="specification" label="规格配置" min-width="160" />
            <el-table-column prop="deploymentLocation" label="部署位置" width="140" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增部署节点' : '编辑部署节点'" width="500px" destroy-on-close>
      <el-form :model="deployForm" label-width="100px">
        <el-form-item label="层级">
          <el-select v-model="deployForm.layer" style="width:100%;">
            <el-option label="接入层" value="接入层" />
            <el-option label="应用层" value="应用层" />
            <el-option label="数据层" value="数据层" />
            <el-option label="公共层" value="公共层" />
          </el-select>
        </el-form-item>
        <el-form-item label="节点名称"><el-input v-model="deployForm.nodeName" /></el-form-item>
        <el-form-item label="节点类型"><el-input v-model="deployForm.nodeType" /></el-form-item>
        <el-form-item label="节点数量"><el-input-number v-model="deployForm.count" :min="1" /></el-form-item>
        <el-form-item label="规格配置"><el-input v-model="deployForm.specification" /></el-form-item>
        <el-form-item label="部署位置"><el-input v-model="deployForm.deploymentLocation" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="deployForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deploymentApi, archAppApi } from '@/api'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('add')
const selectedAppId = ref(null)
const appKeyword = ref('')
const apps = ref([])
const deployList = ref([])

const deployForm = ref({
  id: null,
  appId: null,
  layer: '应用层',
  nodeName: '',
  nodeType: '',
  count: 1,
  specification: '',
  deploymentLocation: '',
  description: ''
})

const filteredApps = computed(() => {
  if (!appKeyword.value) return apps.value
  return apps.value.filter(app =>
    app.appName?.includes(appKeyword.value) || app.appCode?.includes(appKeyword.value)
  )
})

const loadApps = async () => {
  try {
    const res = await archAppApi.list({ page: 1, pageSize: 100 })
    apps.value = res.data.records || []
  } catch {
    ElMessage.error('加载应用列表失败')
  }
}

const loadDeployList = async (appId) => {
  if (!appId) {
    deployList.value = []
    return
  }
  loading.value = true
  try {
    const res = await deploymentApi.listByApp(appId)
    deployList.value = res.data || []
  } catch {
    ElMessage.error('加载部署架构失败')
  } finally {
    loading.value = false
  }
}

const selectApp = (appId) => {
  selectedAppId.value = appId
  loadDeployList(appId)
}

const handleAdd = () => {
  dialogMode.value = 'add'
  deployForm.value = {
    id: null,
    appId: selectedAppId.value,
    layer: '应用层',
    nodeName: '',
    nodeType: '',
    count: 1,
    specification: '',
    deploymentLocation: '',
    description: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  deployForm.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    deployForm.value.appId = selectedAppId.value
    if (dialogMode.value === 'add') {
      await deploymentApi.create(deployForm.value)
    } else {
      await deploymentApi.update(deployForm.value.id, deployForm.value)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadDeployList(selectedAppId.value)
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除 ${row.nodeName}？`, '确认', { type: 'warning' })
    await deploymentApi.delete(row.id)
    ElMessage.success('删除成功')
    loadDeployList(selectedAppId.value)
  } catch {}
}

onMounted(() => {
  loadApps()
})
</script>

<style scoped>
.deploy-layout {
  display: flex;
  gap: 20px;
  min-height: calc(100vh - 200px);
}
.deploy-sidebar {
  width: 240px;
  background: var(--bg-card);
  border-radius: 8px;
  padding: 16px;
  flex-shrink: 0;
}
.sidebar-title {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: var(--text-secondary);
}
.app-item {
  padding: 10px 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 4px;
  transition: all 0.2s;
  color: var(--text-primary);
}
.app-item:hover {
  background: var(--bg-hover);
}
.app-item--active {
  background: var(--accent-light);
  color: var(--accent);
}
.deploy-content {
  flex: 1;
  min-width: 0;
}
</style>
