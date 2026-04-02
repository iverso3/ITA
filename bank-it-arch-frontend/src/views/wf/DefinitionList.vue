<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-header__title">流程定义</h1>
      <p class="page-header__desc">管理和设计审批流程</p>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建流程
        </el-button>
        <el-button @click="loadData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="definitionCode" label="流程编码" width="150" />
        <el-table-column prop="definitionName" label="流程名称" min-width="200" />
        <el-table-column prop="processType" label="流程类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.processType || '通用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80">
          <template #default="{ row }">
            <el-tag type="info">v{{ row.version || 1 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isActive === 1 ? 'success' : 'info'">
              {{ row.isActive === 1 ? '已启用' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleStart(row)">
              <el-icon><CaretRight /></el-icon>
              发起
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              设计
            </el-button>
            <el-button link type="primary" @click="handleModeler(row)">
              <el-icon><Setting /></el-icon>
              建模
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="流程编码">
          <el-input v-model="form.definitionCode" placeholder="如: leave_approval" />
        </el-form-item>
        <el-form-item label="流程名称">
          <el-input v-model="form.definitionName" placeholder="如: 请假审批流程" />
        </el-form-item>
        <el-form-item label="流程类型">
          <el-select v-model="form.processType" placeholder="请选择" style="width: 100%">
            <el-option label="通用审批" value="GENERAL" />
            <el-option label="请假审批" value="LEAVE" />
            <el-option label="采购审批" value="PURCHASE" />
            <el-option label="报销审批" value="EXPENSE" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 发起流程对话框 -->
    <el-dialog v-model="startDialogVisible" title="发起流程" width="500px">
      <el-form :model="startForm" label-width="100px">
        <el-form-item label="流程名称">
          <el-input v-model="startForm.definitionName" disabled />
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="startForm.title" placeholder="请输入流程标题" />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="startForm.businessType" placeholder="请选择" style="width: 100%">
            <el-option label="新建应用" value="NEW_APP" />
            <el-option label="请假" value="LEAVE" />
            <el-option label="采购" value="PURCHASE" />
            <el-option label="报销" value="EXPENSE" />
            <el-option label="通用" value="GENERAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务ID">
          <el-input v-model="startForm.businessId" placeholder="关联业务ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStartSubmit">发起</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Edit, Delete, Setting, CaretRight } from '@element-plus/icons-vue'
import { wfApi } from '@/api'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新建流程')
const startDialogVisible = ref(false)
const startForm = reactive({
  definitionId: null,
  definitionName: '',
  title: '',
  businessType: 'GENERAL',
  businessId: ''
})
const form = reactive({
  definitionCode: '',
  definitionName: '',
  processType: 'GENERAL',
  description: ''
})

async function loadData() {
  loading.value = true
  try {
    const res = await wfApi.definitions()
    tableData.value = res.data.records || res.data || []
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  dialogTitle.value = '新建流程'
  Object.assign(form, {
    definitionCode: '',
    definitionName: '',
    processType: 'GENERAL',
    description: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  // 编辑基本信息
  dialogTitle.value = '编辑流程'
  Object.assign(form, row)
  dialogVisible.value = true
}

function handleModeler(row) {
  // 打开建模器
  router.push(`/wf/modeler/${row.id}`)
}

function handleStart(row) {
  startForm.definitionId = row.id
  startForm.definitionName = row.definitionName
  startForm.title = row.definitionName + ' - 申请'
  startForm.businessType = row.processType || 'GENERAL'
  startForm.businessId = ''
  startDialogVisible.value = true
}

async function handleStartSubmit() {
  if (!startForm.title) {
    ElMessage.warning('请输入流程标题')
    return
  }
  try {
    await wfApi.startProcess({
      definitionId: startForm.definitionId,
      title: startForm.title,
      businessType: startForm.businessType,
      businessId: startForm.businessId || startForm.definitionId,
      variables: {}
    })
    ElMessage.success('发起成功')
    startDialogVisible.value = false
    router.push('/wf/instance')
  } catch (error) {
    ElMessage.error('发起失败')
  }
}

async function handleSubmit() {
  try {
    if (form.id) {
      await wfApi.definitionUpdate(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await wfApi.definitionCreate(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该流程定义吗？', '提示', {
      type: 'warning'
    })
    await wfApi.definitionDelete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 8px;
}
</style>
