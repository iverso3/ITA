<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-header__title">流程实例</h1>
      <p class="page-header__desc">查看和管理流程实例</p>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-select v-model="queryParams.status" placeholder="流程状态" clearable style="width: 150px">
          <el-option label="全部" value="" />
          <el-option label="进行中" value="RUNNING" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已终止" value="TERMINATED" />
        </el-select>
        <el-button @click="loadData">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="instanceCode" label="实例编码" width="150" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="definitionName" label="流程名称" width="150" />
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="currentNodeName" label="当前节点" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160">
          <template #default="{ row }">
            {{ row.startTime ? formatTime(row.startTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">
              查看
            </el-button>
            <el-button link type="danger" @click="handleTerminate(row)" v-if="row.status === 'RUNNING'">
              终止
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 16px"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { wfApi } from '@/api'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  pageSize: 20,
  status: ''
})

function getStatusType(status) {
  const typeMap = {
    'RUNNING': 'primary',
    'COMPLETED': 'success',
    'REJECTED': 'danger',
    'TERMINATED': 'info',
    'WITHDRAWN': 'warning'
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

function formatTime(time) {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

async function loadData() {
  loading.value = true
  try {
    const res = await wfApi.instances(queryParams)
    tableData.value = res.data.records || res.data || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function handleDetail(row) {
  router.push(`/wf/instance/${row.id}`)
}

async function handleTerminate(row) {
  try {
    await ElMessageBox.confirm('确定要终止该流程吗？', '提示', {
      type: 'warning'
    })
    await wfApi.terminateInstance(row.id, '手动终止')
    ElMessage.success('终止成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('终止失败')
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
