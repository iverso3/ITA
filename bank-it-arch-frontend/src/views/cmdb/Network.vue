<template>
  <div class="cmdb-network animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">网络设备管理</h1>
      <p class="page-header__desc">管理交换机、路由器、防火墙、负载均衡等网络设备</p>
    </div>
    <div class="page-card">
      <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
        <el-input v-model="searchKeyword" placeholder="搜索设备名称或IP" style="width: 280px;" clearable />
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增设备</el-button>
      </div>
      <el-table :data="networkList" v-loading="loading" stripe>
        <el-table-column prop="assetCode" label="资产编码" width="180">
          <template #default="{ row }"><code style="color: var(--accent);">{{ row.assetCode }}</code></template>
        </el-table-column>
        <el-table-column prop="deviceName" label="设备名称" min-width="140" />
        <el-table-column prop="deviceType" label="设备类型" width="120">
          <template #default="{ row }"><el-tag size="small">{{ row.deviceType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="管理IP" width="140">
          <template #default="{ row }"><span class="font-mono">{{ row.ipAddress }}</span></template>
        </el-table-column>
        <el-table-column prop="portCount" label="端口数" width="80" align="center" />
        <el-table-column prop="usedPortCount" label="已用端口" width="80" align="center" />
        <el-table-column prop="bandwidth" label="带宽" width="100" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }"><span class="status-badge" :class="'status-badge--' + getStatusClass(row.status)">{{ getStatusText(row.status) }}</span></template>
        </el-table-column>
        <el-table-column prop="manufacturer" label="厂商" width="120" />
        <el-table-column prop="idc" label="机房" width="120" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize" :total="pagination.total" layout="total, prev, pager, next" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增网络设备' : '编辑网络设备'" width="600px" destroy-on-close>
      <el-form :model="networkForm" label-width="100px">
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
          <el-form-item label="设备名称"><el-input v-model="networkForm.deviceName" /></el-form-item>
          <el-form-item label="设备类型"><el-select v-model="networkForm.deviceType" style="width:100%;"><el-option label="交换机" value="交换机" /><el-option label="路由器" value="路由器" /><el-option label="防火墙" value="防火墙" /><el-option label="负载均衡" value="负载均衡" /></el-select></el-form-item>
          <el-form-item label="管理IP"><el-input v-model="networkForm.ipAddress" /></el-form-item>
          <el-form-item label="带宽"><el-input v-model="networkForm.bandwidth" /></el-form-item>
          <el-form-item label="端口数"><el-input-number v-model="networkForm.portCount" :min="0" style="width:100%;" /></el-form-item>
          <el-form-item label="厂商"><el-input v-model="networkForm.manufacturer" /></el-form-item>
          <el-form-item label="状态"><el-select v-model="networkForm.status" style="width:100%;"><el-option label="运行中" value="ONLINE" /><el-option label="离线" value="OFFLINE" /><el-option label="维护中" value="MAINTENANCE" /></el-select></el-form-item>
          <el-form-item label="机房"><el-input v-model="networkForm.idc" /></el-form-item>
        </div>
        <el-form-item label="备注"><el-input v-model="networkForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cmdbNetworkApi } from '@/api'

const searchKeyword = ref('')
const filterStatus = ref('')
const loading = ref(false)
const networkList = ref([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const dialogVisible = ref(false)
const dialogMode = ref('add')
const formRef = ref(null)
const networkForm = reactive({
  id: null, deviceName: '', deviceType: '交换机', ipAddress: '', mgmtVlan: '',
  portCount: 48, usedPortCount: 0, bandwidth: '', manufacturer: '', model: '',
  status: 'OFFLINE', idc: '', cabinet: '', remark: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await cmdbNetworkApi.list({ page: pagination.page, pageSize: pagination.pageSize, keyword: searchKeyword.value, status: filterStatus.value })
    networkList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) { ElMessage.error('加载数据失败') } finally { loading.value = false }
}

const handleAdd = () => {
  dialogMode.value = 'add'
  Object.assign(networkForm, { id: null, deviceName: '', deviceType: '交换机', ipAddress: '', mgmtVlan: '', portCount: 48, usedPortCount: 0, bandwidth: '', manufacturer: '', model: '', status: 'OFFLINE', idc: '', cabinet: '', remark: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  Object.assign(networkForm, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (dialogMode.value === 'add') { await cmdbNetworkApi.create(networkForm) }
    else { await cmdbNetworkApi.update(networkForm.id, networkForm) }
    ElMessage.success('操作成功'); dialogVisible.value = false; loadData()
  } catch { ElMessage.error('操作失败') }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除 ${row.deviceName} 吗？`, '删除确认', { type: 'warning' })
    await cmdbNetworkApi.delete(row.id); ElMessage.success('删除成功'); loadData()
  } catch {}
}

const getStatusClass = (status) => status === 'ONLINE' ? 'online' : 'offline'
const getStatusText = (status) => ({ ONLINE: '运行中', OFFLINE: '离线', MAINTENANCE: '维护中' }[status] || status)

onMounted(() => { loadData() })
</script>
