<template>
  <div class="cmdb-cloud animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">云资源管理</h1>
      <p class="page-header__desc">管理阿里云、华为云、腾讯云、行内云等云资源</p>
    </div>
    <div class="page-card">
      <div style="display: flex; justify-content: flex-end; margin-bottom: 20px;"><el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增云资源</el-button></div>
      <el-table :data="cloudList" v-loading="loading" stripe>
        <el-table-column prop="resourceCode" label="资源编码" width="180"><template #default="{ row }"><code style="color: var(--accent);">{{ row.resourceCode }}</code></template></el-table-column>
        <el-table-column prop="resourceName" label="资源名称" min-width="140" />
        <el-table-column prop="resourceType" label="资源类型" width="120"><template #default="{ row }"><el-tag size="small">{{ row.resourceType }}</el-tag></template></el-table-column>
        <el-table-column prop="cloudProvider" label="云厂商" width="120" />
        <el-table-column prop="region" label="地域" width="100" />
        <el-table-column prop="specification" label="规格" min-width="160" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }"><span class="status-badge" :class="'status-badge--' + getStatusClass(row.status)">{{ getStatusText(row.status) }}</span></template>
        </el-table-column>
        <el-table-column prop="costPerMonth" label="月费用(元)" width="100" align="right">
          <template #default="{ row }"><span class="font-mono text-accent">{{ row.costPerMonth }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;"><el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize" :total="pagination.total" layout="total, prev, pager, next" @current-change="loadData" /></div>
    </div>
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增云资源' : '编辑云资源'" width="600px" destroy-on-close>
      <el-form :model="cloudForm" label-width="100px">
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
          <el-form-item label="资源名称"><el-input v-model="cloudForm.resourceName" /></el-form-item>
          <el-form-item label="资源类型"><el-select v-model="cloudForm.resourceType" style="width:100%;"><el-option label="云主机" value="云主机" /><el-option label="云数据库" value="云数据库" /><el-option label="云存储" value="云存储" /><el-option label="负载均衡" value="负载均衡" /></el-select></el-form-item>
          <el-form-item label="云厂商"><el-select v-model="cloudForm.cloudProvider" style="width:100%;"><el-option label="行内云" value="行内云" /><el-option label="阿里云" value="阿里云" /><el-option label="华为云" value="华为云" /><el-option label="腾讯云" value="腾讯云" /></el-select></el-form-item>
          <el-form-item label="地域"><el-input v-model="cloudForm.region" /></el-form-item>
          <el-form-item label="规格"><el-input v-model="cloudForm.specification" /></el-form-item>
          <el-form-item label="状态"><el-select v-model="cloudForm.status" style="width:100%;"><el-option label="运行中" value="ONLINE" /><el-option label="离线" value="OFFLINE" /></el-select></el-form-item>
        </div>
        <el-form-item label="备注"><el-input v-model="cloudForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cmdbCloudApi } from '@/api'

const loading = ref(false)
const cloudList = ref([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const dialogVisible = ref(false)
const dialogMode = ref('add')
const cloudForm = reactive({ id: null, resourceName: '', resourceType: '云主机', cloudProvider: '行内云', region: '', specification: '', cpu: 0, memory: 0, status: 'OFFLINE', costPerMonth: '', remark: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await cmdbCloudApi.list({ page: pagination.page, pageSize: pagination.pageSize })
    cloudList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const handleAdd = () => { dialogMode.value = 'add'; Object.assign(cloudForm, { id: null, resourceName: '', resourceType: '云主机', cloudProvider: '行内云', region: '', specification: '', cpu: 0, memory: 0, status: 'OFFLINE', costPerMonth: '', remark: '' }); dialogVisible.value = true }
const handleEdit = (row) => { dialogMode.value = 'edit'; Object.assign(cloudForm, row); dialogVisible.value = true }
const handleSubmit = async () => { try { if (dialogMode.value === 'add') await cmdbCloudApi.create(cloudForm); else await cmdbCloudApi.update(cloudForm.id, cloudForm); ElMessage.success('操作成功'); dialogVisible.value = false; loadData() } catch { ElMessage.error('操作失败') } }
const handleDelete = async (row) => { try { await ElMessageBox.confirm(`确定删除 ${row.resourceName}？`, '确认', { type: 'warning' }); await cmdbCloudApi.delete(row.id); ElMessage.success('删除成功'); loadData() } catch {} }

const getStatusClass = (status) => status === 'ONLINE' ? 'online' : 'offline'
const getStatusText = (status) => status === 'ONLINE' ? '运行中' : '离线'

onMounted(() => { loadData() })
</script>
