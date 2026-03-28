<template>
  <div class="data-flow animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">数据流向管理</h1>
      <p class="page-header__desc">数据在应用间的流向与分布</p>
    </div>
    <div class="page-card">
      <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
        <div style="display: flex; gap: 12px;">
          <el-input v-model="searchKeyword" placeholder="搜索流向名称" style="width: 240px;" clearable />
        </div>
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增流向</el-button>
      </div>
      <el-table :data="flowList" v-loading="loading" stripe>
        <el-table-column prop="flowName" label="流向名称" min-width="160" />
        <el-table-column prop="flowCode" label="流向编码" width="140">
          <template #default="{ row }">
            <code style="color: var(--accent);">{{ row.flowCode }}</code>
          </template>
        </el-table-column>
        <el-table-column label="源 -> 目标" min-width="200">
          <template #default="{ row }">
            <span>{{ row.sourceAppName || '-' }}</span>
            <el-icon><Right /></el-icon>
            <span>{{ row.targetAppName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="flowType" label="流向类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.flowType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="transferType" label="传输类型" width="120" />
        <el-table-column prop="frequency" label="更新频率" width="100" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="pagination.page"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </div>
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增数据流向' : '编辑数据流向'" width="550px" destroy-on-close>
      <el-form :model="flowForm" label-width="100px">
        <el-form-item label="流向名称"><el-input v-model="flowForm.flowName" /></el-form-item>
        <el-form-item label="源应用">
          <el-select v-model="flowForm.sourceAppId" placeholder="选择源应用" filterable style="width:100%;">
            <el-option v-for="app in apps" :key="app.id" :label="app.appName" :value="app.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标应用">
          <el-select v-model="flowForm.targetAppId" placeholder="选择目标应用" filterable style="width:100%;">
            <el-option v-for="app in apps" :key="app.id" :label="app.appName" :value="app.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="流向类型">
          <el-select v-model="flowForm.flowType" style="width:100%;">
            <el-option label="同步" value="同步" />
            <el-option label="异步" value="异步" />
            <el-option label="批量" value="批量" />
            <el-option label="实时" value="实时" />
          </el-select>
        </el-form-item>
        <el-form-item label="传输类型"><el-input v-model="flowForm.transferType" /></el-form-item>
        <el-form-item label="更新频率"><el-input v-model="flowForm.frequency" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="flowForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dataFlowApi, archAppApi } from '@/api'

const searchKeyword = ref('')
const loading = ref(false)
const apps = ref([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const dialogVisible = ref(false)
const dialogMode = ref('add')
const flowForm = reactive({
  id: null,
  flowName: '',
  sourceAppId: null,
  sourceAppName: '',
  targetAppId: null,
  targetAppName: '',
  flowType: '同步',
  transferType: '',
  frequency: '',
  description: ''
})

const flowList = ref([])

const loadApps = async () => {
  try {
    const res = await archAppApi.list({ page: 1, pageSize: 100 })
    apps.value = res.data.records || []
  } catch {
    ElMessage.error('加载应用列表失败')
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await dataFlowApi.list({ page: pagination.page, pageSize: pagination.pageSize, keyword: searchKeyword.value })
    flowList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogMode.value = 'add'
  Object.assign(flowForm, {
    id: null,
    flowName: '',
    sourceAppId: null,
    sourceAppName: '',
    targetAppId: null,
    targetAppName: '',
    flowType: '同步',
    transferType: '',
    frequency: '',
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  Object.assign(flowForm, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  // 设置应用名称
  const sourceApp = apps.value.find(a => a.id === flowForm.sourceAppId)
  const targetApp = apps.value.find(a => a.id === flowForm.targetAppId)
  if (sourceApp) flowForm.sourceAppName = sourceApp.appName
  if (targetApp) flowForm.targetAppName = targetApp.appName

  try {
    if (dialogMode.value === 'add') {
      await dataFlowApi.create(flowForm)
    } else {
      await dataFlowApi.update(flowForm.id, flowForm)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除 ${row.flowName}？`, '确认', { type: 'warning' })
    await dataFlowApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

onMounted(() => {
  loadApps()
  loadData()
})
</script>
