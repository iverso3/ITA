<template>
  <div class="system-log animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">操作日志</h1>
      <p class="page-header__desc">系统操作审计与追踪</p>
    </div>
    <div class="page-card">
      <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
        <div style="display: flex; gap: 12px;">
          <el-input v-model="searchModule" placeholder="搜索模块" style="width: 160px;" clearable />
          <el-input v-model="searchOperator" placeholder="搜索操作人" style="width: 160px;" clearable />
          <el-button @click="loadData">搜索</el-button>
        </div>
      </div>
      <el-table :data="logList" v-loading="loading" stripe>
        <el-table-column prop="createTime" label="操作时间" width="160" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="operation" label="操作" width="120" />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="operatorIp" label="IP地址" width="140" />
        <el-table-column prop="requestMethod" label="请求方法" width="80" align="center">
          <template #default="{ row }"><el-tag size="small">{{ row.requestMethod }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="requestUrl" label="请求URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="executeTime" label="耗时" width="80" align="center">
          <template #default="{ row }"><span class="font-mono">{{ row.executeTime }}ms</span></template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { sysLogApi } from '@/api'

const loading = ref(false)
const searchModule = ref('')
const searchOperator = ref('')
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const logList = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await sysLogApi.list({
      page: pagination.page,
      pageSize: pagination.pageSize,
      module: searchModule.value || undefined,
      operatorName: searchOperator.value || undefined
    })
    logList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

onMounted(() => { loadData() })
</script>
