<template>
  <div class="tech-stack animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">技术栈管理</h1>
      <p class="page-header__desc">全行技术栈分布与技术选型规范</p>
    </div>
    <div class="page-card">
      <div class="page-card__header">
        <h3><el-icon color="var(--accent)"><List /></el-icon> 技术栈清单</h3>
        <el-button type="primary" size="small" @click="handleAdd">新增技术栈</el-button>
      </div>
      <el-table :data="techStackList" v-loading="loading" stripe>
        <el-table-column prop="stackName" label="技术名称" min-width="160" />
        <el-table-column prop="stackType" label="类型" width="120">
          <template #default="{ row }"><el-tag size="small">{{ row.stackType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="100" />
        <el-table-column prop="vendor" label="供应商" width="140" />
        <el-table-column prop="appCount" label="应用数量" width="100" align="center" />
        <el-table-column prop="isStandard" label="标准技术" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isStandard ? 'success' : 'info'">{{ row.isStandard ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="80" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="'status-badge--' + (row.isActive ? 'online' : 'offline')">
              {{ row.isActive ? '启用' : '停用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增技术栈' : '编辑技术栈'" width="500px" destroy-on-close>
      <el-form :model="techForm" label-width="100px">
        <el-form-item label="编码"><el-input v-model="techForm.stackCode" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="techForm.stackName" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="techForm.stackType" style="width:100%;">
            <el-option label="语言" value="语言" />
            <el-option label="框架" value="框架" />
            <el-option label="中间件" value="中间件" />
            <el-option label="数据库" value="数据库" />
            <el-option label="操作系统" value="操作系统" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本"><el-input v-model="techForm.version" /></el-form-item>
        <el-form-item label="供应商"><el-input v-model="techForm.vendor" /></el-form-item>
        <el-form-item label="标准技术"><el-switch v-model="techForm.isStandard" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="techForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { techStackApi } from '@/api'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('add')
const techForm = ref({
  id: null,
  stackCode: '',
  stackName: '',
  stackType: '语言',
  version: '',
  vendor: '',
  isStandard: 0,
  isActive: 1,
  description: ''
})

const techStackList = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await techStackApi.list({ page: 1, pageSize: 100 })
    techStackList.value = res.data.records || []
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogMode.value = 'add'
  techForm.value = {
    id: null,
    stackCode: '',
    stackName: '',
    stackType: '语言',
    version: '',
    vendor: '',
    isStandard: 0,
    isActive: 1,
    description: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  techForm.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (dialogMode.value === 'add') {
      await techStackApi.create(techForm.value)
    } else {
      await techStackApi.update(techForm.value.id, techForm.value)
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
    await ElMessageBox.confirm(`确定删除 ${row.stackName}？`, '确认', { type: 'warning' })
    await techStackApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

onMounted(() => {
  loadData()
})
</script>
