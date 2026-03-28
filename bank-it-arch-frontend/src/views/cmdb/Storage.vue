<template>
  <div class="cmdb-storage animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">存储设备管理</h1>
      <p class="page-header__desc">管理NAS、SAN、对象存储等存储设备</p>
    </div>
    <div class="page-card">
      <div style="display: flex; justify-content: flex-end; margin-bottom: 20px;"><el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增存储</el-button></div>
      <el-table :data="storageList" v-loading="loading" stripe>
        <el-table-column prop="assetCode" label="资产编码" width="180"><template #default="{ row }"><code style="color: var(--accent);">{{ row.assetCode }}</code></template></el-table-column>
        <el-table-column prop="deviceName" label="设备名称" min-width="140" />
        <el-table-column prop="deviceType" label="设备类型" width="120"><template #default="{ row }"><el-tag size="small">{{ row.deviceType }}</el-tag></template></el-table-column>
        <el-table-column prop="totalCapacity" label="总容量(TB)" width="120" align="center" />
        <el-table-column prop="usedCapacity" label="已用容量(TB)" width="120" align="center">
          <template #default="{ row }">
            <el-progress :percentage="Math.round(row.usedCapacity / row.totalCapacity * 100)" :color="getProgressColor(row)" />
          </template>
        </el-table-column>
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
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;"><el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize" :total="pagination.total" layout="total, prev, pager, next" @current-change="loadData" /></div>
    </div>
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增存储设备' : '编辑存储设备'" width="500px" destroy-on-close>
      <el-form :model="storageForm" label-width="100px">
        <el-form-item label="设备名称"><el-input v-model="storageForm.deviceName" /></el-form-item>
        <el-form-item label="设备类型"><el-select v-model="storageForm.deviceType" style="width:100%;"><el-option label="NAS" value="NAS" /><el-option label="SAN" value="SAN" /><el-option label="对象存储" value="对象存储" /></el-select></el-form-item>
        <el-form-item label="总容量(TB)"><el-input-number v-model="storageForm.totalCapacity" :min="0" style="width:100%;" /></el-form-item>
        <el-form-item label="已用(TB)"><el-input-number v-model="storageForm.usedCapacity" :min="0" style="width:100%;" /></el-form-item>
        <el-form-item label="厂商"><el-input v-model="storageForm.manufacturer" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="storageForm.status" style="width:100%;"><el-option label="运行中" value="ONLINE" /><el-option label="离线" value="OFFLINE" /></el-select></el-form-item>
        <el-form-item label="机房"><el-input v-model="storageForm.idc" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="storageForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cmdbStorageApi } from '@/api'

const loading = ref(false)
const storageList = ref([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const dialogVisible = ref(false)
const dialogMode = ref('add')
const storageForm = reactive({ id: null, deviceName: '', deviceType: 'NAS', totalCapacity: 0, usedCapacity: 0, manufacturer: '', status: 'OFFLINE', idc: '', remark: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await cmdbStorageApi.list({ page: pagination.page, pageSize: pagination.pageSize })
    storageList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const handleAdd = () => { dialogMode.value = 'add'; Object.assign(storageForm, { id: null, deviceName: '', deviceType: 'NAS', totalCapacity: 0, usedCapacity: 0, manufacturer: '', status: 'OFFLINE', idc: '', remark: '' }); dialogVisible.value = true }
const handleEdit = (row) => { dialogMode.value = 'edit'; Object.assign(storageForm, row); dialogVisible.value = true }
const handleSubmit = async () => {
  try { if (dialogMode.value === 'add') await cmdbStorageApi.create(storageForm); else await cmdbStorageApi.update(storageForm.id, storageForm); ElMessage.success('操作成功'); dialogVisible.value = false; loadData() } catch { ElMessage.error('操作失败') }
}
const handleDelete = async (row) => { try { await ElMessageBox.confirm(`确定删除 ${row.deviceName}？`, '确认', { type: 'warning' }); await cmdbStorageApi.delete(row.id); ElMessage.success('删除成功'); loadData() } catch {} }

const getStatusClass = (status) => status === 'ONLINE' ? 'online' : 'offline'
const getStatusText = (status) => status === 'ONLINE' ? '运行中' : '离线'
const getProgressColor = (row) => {
  if (!row.totalCapacity) return '#2ecc71'
  const percent = row.usedCapacity / row.totalCapacity
  if (percent > 0.9) return '#e74c3c'
  if (percent > 0.7) return '#f39c12'
  return '#2ecc71'
}

onMounted(() => { loadData() })
</script>
