<template>
  <div class="wf-todo animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">待办任务</h1>
      <p class="page-header__desc">需要您审批的任务列表</p>
    </div>
    <div class="page-card">
      <el-table :data="taskList" v-loading="loading" stripe>
        <el-table-column type="index" width="50" />
        <el-table-column prop="taskName" label="任务名称" min-width="200">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-icon :color="row.priority === 'URGENT' ? 'var(--danger)' : 'var(--warning)'">
                <WarningFilled v-if="row.priority === 'URGENT'" /><Clock v-else />
              </el-icon>
              <span style="color: var(--text-primary); font-weight: 500;">{{ row.taskName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="processType" label="流程类型" width="120">
          <template #default="{ row }"><el-tag size="small">{{ row.processType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column prop="nodeName" label="当前节点" width="140">
          <template #default="{ row }"><el-tag size="small" type="warning">{{ row.nodeName }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.priority === 'URGENT' ? 'danger' : row.priority === 'HIGH' ? 'warning' : 'info'">{{ row.priorityText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleApprove(row)">通过</el-button>
            <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            <el-button size="small" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;"><el-pagination v-model:current-page="pagination.page" :total="pagination.total" layout="total, prev, pager, next" @current-change="loadData" /></div>
    </div>

    <!-- 审批对话框 -->
    <el-dialog v-model="approveDialogVisible" :title="approveAction === 'approve' ? '审批通过' : '审批拒绝'" width="400px">
      <el-input v-model="opinion" type="textarea" :rows="3" placeholder="请输入审批意见" />
      <template #footer><el-button @click="approveDialogVisible = false">取消</el-button><el-button type="primary" @click="submitApproval">确定</el-button></template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="任务详情" width="600px">
      <div v-if="viewTask" class="task-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称" :span="2">{{ viewTask.taskName }}</el-descriptions-item>
          <el-descriptions-item label="流程类型">{{ viewTask.processType }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ viewTask.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ viewTask.departmentName }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ viewTask.createTime }}</el-descriptions-item>
          <el-descriptions-item label="当前节点">{{ viewTask.nodeName }}</el-descriptions-item>
          <el-descriptions-item label="优先级">{{ viewTask.priorityText }}</el-descriptions-item>
          <el-descriptions-item label="申请说明" :span="2">{{ viewTask.description || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="() => { viewDialogVisible = false; handleApprove(viewTask) }">通过</el-button>
        <el-button type="danger" @click="() => { viewDialogVisible = false; handleReject(viewTask) }">拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { wfApi } from '@/api'

const loading = ref(false)
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const taskList = ref([])
const approveDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const currentTask = ref(null)
const viewTask = ref(null)
const opinion = ref('')
const approveAction = ref('approve')

const loadData = async () => {
  loading.value = true
  try {
    const res = await wfApi.todoList({ page: pagination.page, pageSize: pagination.pageSize })
    taskList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const handleApprove = (row) => { currentTask.value = row; approveAction.value = 'approve'; opinion.value = ''; approveDialogVisible.value = true }
const handleReject = (row) => { currentTask.value = row; approveAction.value = 'reject'; opinion.value = ''; approveDialogVisible.value = true }
const submitApproval = async () => {
  try {
    if (approveAction.value === 'approve') await wfApi.approve(currentTask.value.id, { opinion: opinion.value })
    else await wfApi.reject(currentTask.value.id, { opinion: opinion.value })
    ElMessage.success('操作成功'); approveDialogVisible.value = false; loadData()
  } catch { ElMessage.error('操作失败') }
}
const handleView = (row) => { viewTask.value = row; viewDialogVisible.value = true }

onMounted(() => { loadData() })
</script>
