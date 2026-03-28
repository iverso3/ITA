<template>
  <div class="cmdb-servers animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">服务器管理</h1>
      <p class="page-header__desc">管理全行服务器资产，支持批量导入导出</p>
    </div>

    <!-- Actions Bar -->
    <div class="page-card" style="margin-bottom: 24px;">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div style="display: flex; gap: 12px; align-items: center;">
          <el-input v-model="searchKeyword" placeholder="搜索主机名、IP或资产编码" style="width: 280px;" clearable @keyup.enter="loadData">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px;" @change="loadData">
            <el-option label="运行中" value="ONLINE" />
            <el-option label="离线" value="OFFLINE" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="库存" value="STOCK" />
          </el-select>
          <el-select v-model="filterDepartment" placeholder="部门" clearable style="width: 140px;" @change="loadData">
            <el-option label="科技部" value="1" />
            <el-option label="运维部" value="2" />
            <el-option label="数据中心" value="3" />
          </el-select>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </div>
        <div style="display: flex; gap: 12px;">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增服务器
          </el-button>
          <el-button @click="handleImport">
            <el-icon><Upload /></el-icon> 批量导入
          </el-button>
          <el-button @click="handleExport">
            <el-icon><Download /></el-icon> 导出
          </el-button>
        </div>
      </div>
    </div>

    <!-- Server Table -->
    <div class="page-card">
      <el-table :data="serverList" v-loading="loading" stripe style="width: 100%">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="assetCode" label="资产编码" width="180">
          <template #default="{ row }">
            <code style="color: var(--accent); font-size: 0.8125rem;">{{ row.assetCode }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="hostname" label="主机名" min-width="140">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px;">
              <span class="status-dot" :class="'status-dot--' + getStatusClass(row.status)"></span>
              <span style="color: var(--text-primary);">{{ row.hostname }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="140">
          <template #default="{ row }">
            <span class="font-mono">{{ row.ipAddress }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="os" label="操作系统" width="120" />
        <el-table-column prop="cpuCount" label="CPU" width="70" align="center">
          <template #default="{ row }">
            <span class="text-secondary">{{ row.cpuCount }}核</span>
          </template>
        </el-table-column>
        <el-table-column prop="memorySize" label="内存" width="80" align="center">
          <template #default="{ row }">
            <span class="text-secondary">{{ row.memorySize }}G</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="'status-badge--' + getStatusClass(row.status)">{{ getStatusText(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="departmentName" label="所属部门" width="120" />
        <el-table-column prop="idc" label="机房" width="120" />
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleView(row)">详情</el-button>
            <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <el-form :model="serverForm" :rules="formRules" ref="formRef" label-width="100px">
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
          <el-form-item label="主机名" prop="hostname">
            <el-input v-model="serverForm.hostname" placeholder="请输入主机名" />
          </el-form-item>
          <el-form-item label="资产编码" prop="assetCode">
            <el-input v-model="serverForm.assetCode" placeholder="自动生成" disabled />
          </el-form-item>
          <el-form-item label="IP地址" prop="ipAddress">
            <el-input v-model="serverForm.ipAddress" placeholder="请输入IP地址" />
          </el-form-item>
          <el-form-item label="内网IP" prop="innerIp">
            <el-input v-model="serverForm.innerIp" placeholder="请输入内网IP" />
          </el-form-item>
          <el-form-item label="服务器类型" prop="serverType">
            <el-select v-model="serverForm.serverType" style="width: 100%;">
              <el-option label="物理机" value="物理机" />
              <el-option label="虚拟机" value="虚拟机" />
              <el-option label="容器" value="容器" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="serverForm.status" style="width: 100%;">
              <el-option label="运行中" value="ONLINE" />
              <el-option label="离线" value="OFFLINE" />
              <el-option label="维护中" value="MAINTENANCE" />
              <el-option label="库存" value="STOCK" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作系统" prop="os">
            <el-input v-model="serverForm.os" placeholder="如 CentOS 7.9" />
          </el-form-item>
          <el-form-item label="系统版本" prop="osVersion">
            <el-input v-model="serverForm.osVersion" placeholder="如 7.9" />
          </el-form-item>
          <el-form-item label="CPU" prop="cpu">
            <el-input v-model="serverForm.cpu" placeholder="如 Intel Xeon" />
          </el-form-item>
          <el-form-item label="CPU核数" prop="cpuCount">
            <el-input-number v-model="serverForm.cpuCount" :min="1" :max="256" />
          </el-form-item>
          <el-form-item label="内存" prop="memory">
            <el-input v-model="serverForm.memory" placeholder="如 DDR4" />
          </el-form-item>
          <el-form-item label="内存大小" prop="memorySize">
            <el-input-number v-model="serverForm.memorySize" :min="1" :max="1024" suffix-icon="G" />
          </el-form-item>
          <el-form-item label="磁盘" prop="disk">
            <el-input v-model="serverForm.disk" placeholder="如 500GB SSD" />
          </el-form-item>
          <el-form-item label="磁盘大小" prop="diskSize">
            <el-input-number v-model="serverForm.diskSize" :min="1" :max="10000" suffix-icon="G" />
          </el-form-item>
          <el-form-item label="所属部门" prop="departmentId">
            <el-select v-model="serverForm.departmentId" style="width: 100%;">
              <el-option label="科技部" :value="1" />
              <el-option label="运维部" :value="2" />
              <el-option label="数据中心" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属团队" prop="teamId">
            <el-select v-model="serverForm.teamId" style="width: 100%;">
              <el-option label="基础架构组" :value="1" />
              <el-option label="应用运维组" :value="2" />
              <el-option label="云平台组" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="机房" prop="idc">
            <el-input v-model="serverForm.idc" placeholder="如 主数据中心-A" />
          </el-form-item>
          <el-form-item label="机柜" prop="cabinet">
            <el-input v-model="serverForm.cabinet" placeholder="如 A-01-05" />
          </el-form-item>
          <el-form-item label="厂商" prop="manufacturer">
            <el-input v-model="serverForm.manufacturer" placeholder="如 Dell" />
          </el-form-item>
          <el-form-item label="型号" prop="model">
            <el-input v-model="serverForm.model" placeholder="如 PowerEdge R740" />
          </el-form-item>
        </div>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="serverForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchKeyword = ref('')
const filterStatus = ref('')
const filterDepartment = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增服务器')
const dialogMode = ref('add')
const formRef = ref(null)

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const serverForm = reactive({
  id: null,
  hostname: '',
  assetCode: '',
  ipAddress: '',
  innerIp: '',
  serverType: '物理机',
  status: 'OFFLINE',
  os: '',
  osVersion: '',
  cpu: '',
  cpuCount: 2,
  memory: '',
  memorySize: 64,
  disk: '',
  diskSize: 500,
  departmentId: null,
  teamId: null,
  idc: '',
  cabinet: '',
  manufacturer: '',
  model: '',
  remark: ''
})

const formRules = {
  hostname: [{ required: true, message: '请输入主机名', trigger: 'blur' }],
  ipAddress: [{ required: true, message: '请输入IP地址', trigger: 'blur' }]
}

const serverList = ref([])

const getStatusClass = (status) => {
  const map = { ONLINE: 'online', OFFLINE: 'offline', MAINTENANCE: 'warning' }
  return map[status] || 'offline'
}

const getStatusText = (status) => {
  const map = { ONLINE: '运行中', OFFLINE: '离线', MAINTENANCE: '维护中', STOCK: '库存' }
  return map[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    params.append('page', pagination.page)
    params.append('pageSize', pagination.pageSize)
    if (searchKeyword.value) params.append('keyword', searchKeyword.value)
    if (filterStatus.value) params.append('status', filterStatus.value)
    if (filterDepartment.value) params.append('departmentId', filterDepartment.value)

    const res = await fetch(`/api/v1/cmdb/servers?${params.toString()}`)
    const json = await res.json()
    if (json.code === 200) {
      serverList.value = json.data.records || []
      pagination.total = json.data.total || 0
    }
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogMode.value = 'add'
  dialogTitle.value = '新增服务器'
  Object.keys(serverForm).forEach(key => {
    if (key !== 'serverType' && key !== 'status' && key !== 'cpuCount' && key !== 'memorySize' && key !== 'diskSize') {
      serverForm[key] = ''
    }
  })
  serverForm.id = null
  dialogVisible.value = true
}

const handleView = (row) => {
  dialogMode.value = 'view'
  dialogTitle.value = '服务器详情'
  Object.assign(serverForm, row)
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑服务器'
  Object.assign(serverForm, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除服务器 ${row.hostname} 吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await fetch(`/api/v1/cmdb/servers/${row.id}`, { method: 'DELETE' })
    const json = await res.json()
    if (json.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(json.message || '删除失败')
    }
  } catch {
    // cancelled
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    const method = dialogMode.value === 'add' ? 'POST' : 'PUT'
    const url = dialogMode.value === 'add'
      ? '/api/v1/cmdb/servers'
      : `/api/v1/cmdb/servers/${serverForm.id}`

    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(serverForm)
    })
    const json = await res.json()
    if (json.code === 200) {
      ElMessage.success(dialogMode.value === 'add' ? '创建成功' : '更新成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(json.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleImport = () => {
  ElMessage.info('批量导入功能开发中')
}

const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.el-input-number {
  width: 100%;
  :deep(.el-input__inner) {
    text-align: left;
  }
}
</style>
