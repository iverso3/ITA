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
        <el-table-column prop="definitionName" label="流程类型" width="120">
          <template #default="{ row }"><el-tag size="small">{{ row.definitionName }}</el-tag></template>
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
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;"><el-pagination v-model:current-page="pagination.page" :total="pagination.total" layout="total, prev, pager, next" @current-change="loadData" /></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { wfApi } from '@/api'

const router = useRouter()

const loading = ref(false)
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const taskList = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await wfApi.todoList({ page: pagination.page, pageSize: pagination.pageSize })
    taskList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const handleView = (row) => {
  router.push(`/wf/task/${row.id}`)
}

onMounted(() => { loadData() })
</script>
