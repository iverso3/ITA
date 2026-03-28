<template>
  <div class="wf-done animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">已办任务</h1>
      <p class="page-header__desc">您已处理过的审批任务</p>
    </div>
    <div class="page-card">
      <el-table :data="taskList" v-loading="loading" stripe>
        <el-table-column type="index" width="50" />
        <el-table-column prop="taskName" label="任务名称" min-width="200" />
        <el-table-column prop="processType" label="流程类型" width="120">
          <template #default="{ row }"><el-tag size="small">{{ row.processType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column prop="completeTime" label="处理时间" width="160" />
        <el-table-column prop="action" label="审批动作" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'APPROVED' ? 'success' : 'danger'">{{ row.status === 'APPROVED' ? '通过' : '拒绝' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="opinion" label="审批意见" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;"><el-pagination v-model:current-page="pagination.page" :total="pagination.total" layout="total, prev, pager, next" @current-change="loadData" /></div>
    </div>

    <!-- 任务详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="任务详情" width="600px">
      <div v-if="viewTask">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称" :span="2">{{ viewTask.taskName }}</el-descriptions-item>
          <el-descriptions-item label="流程类型">{{ viewTask.processType }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ viewTask.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ viewTask.departmentName }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ viewTask.completeTime }}</el-descriptions-item>
          <el-descriptions-item label="审批结果">
            <el-tag size="small" :type="viewTask.status === 'APPROVED' ? 'success' : 'danger'">{{ viewTask.status === 'APPROVED' ? '通过' : '拒绝' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审批意见" :span="2">{{ viewTask.opinion || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer><el-button @click="viewDialogVisible = false">关闭</el-button></template>
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
const viewDialogVisible = ref(false)
const viewTask = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await wfApi.doneList({ page: pagination.page, pageSize: pagination.pageSize })
    taskList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const handleView = (row) => { viewTask.value = row; viewDialogVisible.value = true }

onMounted(() => { loadData() })
</script>
