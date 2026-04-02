<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-header__title">流程实例详情</h1>
      <p class="page-header__desc">查看流程执行详情和审批历史</p>
    </div>

    <div class="page-card" v-loading="loading">
      <!-- 基本信息 -->
      <el-descriptions title="基本信息" :column="2" border>
        <el-descriptions-item label="实例编码">{{ instance.instanceCode }}</el-descriptions-item>
        <el-descriptions-item label="流程名称">{{ instance.definitionName }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ instance.title }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ instance.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="当前节点">
          <el-tag type="primary">{{ instance.currentNodeName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(instance.status)">
            {{ getStatusLabel(instance.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">
          {{ instance.startTime ? formatTime(instance.startTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间">
          {{ instance.endTime ? formatTime(instance.endTime) : '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 流程进度 -->
      <div class="section" v-if="histories.length > 0">
        <h3>审批历史</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in histories"
            :key="index"
            :timestamp="formatTime(item.createTime)"
            :type="getHistoryType(item.action)"
            hollow
          >
            <div class="history-item">
              <span class="action">{{ getActionLabel(item.action) }}</span>
              <span class="operator">{{ item.operatorName }}</span>
              <span class="node" v-if="item.nodeName"> - {{ item.nodeName }}</span>
            </div>
            <div class="opinion" v-if="item.opinion">{{ item.opinion }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 当前任务 -->
      <div class="section" v-if="tasks.length > 0">
        <h3>当前任务</h3>
        <el-table :data="tasks" stripe>
          <el-table-column prop="taskName" label="任务名称" width="150" />
          <el-table-column prop="assigneeName" label="处理人" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'PENDING' ? 'primary' : 'success'">
                {{ row.status === 'PENDING' ? '待处理' : '已完成' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="opinion" label="审批意见" show-overflow-tooltip />
        </el-table>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button @click="handleBack">返回</el-button>
        <el-button
          type="primary"
          @click="handleWithdraw"
          v-if="instance.status === 'RUNNING' && canWithdraw"
        >
          撤回
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { wfApi } from '@/api'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const instance = ref({})
const histories = ref([])
const tasks = ref([])

const canWithdraw = computed(() => {
  // 只有申请人才能撤回
  return true
})

function getStatusType(status) {
  const typeMap = {
    'RUNNING': 'primary',
    'COMPLETED': 'success',
    'REJECTED': 'danger',
    'TERMINATED': 'info'
  }
  return typeMap[status] || 'info'
}

function getStatusLabel(status) {
  const labelMap = {
    'RUNNING': '进行中',
    'COMPLETED': '已完成',
    'REJECTED': '已驳回',
    'TERMINATED': '已终止',
    'WITHDRAWN': '已撤回'
  }
  return labelMap[status] || status
}

function getHistoryType(action) {
  const typeMap = {
    'START': 'primary',
    'APPROVE': 'success',
    'REJECT': 'danger',
    'TRANSFER': 'warning',
    'DELEGATE': 'info',
    'CLAIM': 'primary'
  }
  return typeMap[action] || 'info'
}

function getActionLabel(action) {
  const labelMap = {
    'START': '发起',
    'APPROVE': '通过',
    'REJECT': '拒绝',
    'TRANSFER': '转办',
    'DELEGATE': '加签',
    'RETURN': '退回',
    'CLAIM': '签收',
    'WITHDRAW': '撤回'
  }
  return labelMap[action] || action
}

function formatTime(time) {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

async function loadData() {
  const id = route.params.id
  if (!id) return

  loading.value = true
  try {
    const [instanceRes, traceRes, tasksRes] = await Promise.all([
      wfApi.instanceDetail(id),
      wfApi.instanceTrace(id),
      wfApi.instanceTasks(id)
    ])

    instance.value = instanceRes.data || {}
    histories.value = traceRes.data || []
    tasks.value = tasksRes.data || []
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.back()
}

async function handleWithdraw() {
  try {
    await wfApi.withdrawInstance(route.params.id)
    ElMessage.success('撤回成功')
    loadData()
  } catch (error) {
    ElMessage.error('撤回失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.section {
  margin-top: 24px;
}

.section h3 {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 500;
}

.history-item {
  color: #606266;
}

.history-item .action {
  font-weight: 500;
}

.history-item .operator {
  color: #409EFF;
}

.history-item .node {
  color: #909399;
}

.opinion {
  margin-top: 8px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  font-size: 13px;
}

.actions {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 8px;
}
</style>
