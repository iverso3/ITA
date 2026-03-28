<template>
  <div class="arch-application animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">应用架构管理</h1>
      <p class="page-header__desc">管理全行应用系统、服务模块和依赖关系</p>
    </div>

    <div style="display: grid; grid-template-columns: 1fr 360px; gap: 24px;">
      <!-- Main List -->
      <div class="page-card">
        <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
          <div style="display: flex; gap: 12px;">
            <el-input v-model="searchKeyword" placeholder="搜索应用名称或编码" style="width: 240px;" clearable />
            <el-select v-model="filterType" placeholder="应用类型" clearable style="width: 120px;">
              <el-option label="交易类" value="交易" />
              <el-option label="渠道类" value="渠道" />
              <el-option label="管理类" value="管理" />
              <el-option label="数据类" value="数据" />
              <el-option label="基础设施" value="基础设施" />
            </el-select>
            <el-select v-model="filterLevel" placeholder="重要级别" clearable style="width: 120px;">
              <el-option label="核心" value="核心" />
              <el-option label="重要" value="重要" />
              <el-option label="一般" value="一般" />
              <el-option label="辅助" value="辅助" />
            </el-select>
          </div>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增应用</el-button>
        </div>

        <el-table :data="appList" v-loading="loading" stripe highlight-current-row @row-click="handleRowClick">
          <el-table-column prop="appName" label="应用名称" min-width="160">
            <template #default="{ row }">
              <div style="display: flex; align-items: center; gap: 8px;">
                <el-icon color="var(--primary-lighter)"><Box /></el-icon>
                <span style="color: var(--text-primary); font-weight: 500;">{{ row.appName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="appCode" label="编码" width="160">
            <template #default="{ row }"><code style="color: var(--accent); font-size: 0.8rem;">{{ row.appCode }}</code></template>
          </el-table-column>
          <el-table-column prop="appType" label="类型" width="90">
            <template #default="{ row }"><el-tag size="small" type="info">{{ row.appType }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="importanceLevel" label="级别" width="80">
            <template #default="{ row }">
              <span :style="{ color: getLevelColor(row.importanceLevel), fontWeight: 600 }">{{ row.importanceLevel }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="lifecycle" label="生命周期" width="100">
            <template #default="{ row }">
              <span class="status-badge" :class="'status-badge--' + getLifecycleClass(row.lifecycle)">{{ row.lifecycle }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="departmentName" label="所属部门" width="100" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" text size="small" @click.stop="handleEdit(row)">编辑</el-button>
              <el-button type="danger" text size="small" @click.stop="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next"
            @size-change="loadData"
            @current-change="loadData"
          />
        </div>
      </div>

      <!-- Detail Panel -->
      <div class="page-card" style="height: fit-content;">
        <div class="page-card__header">
          <h3>应用详情</h3>
          <el-button type="primary" size="small" @click="handleEdit(selectedApp)">编辑</el-button>
        </div>
        <div v-if="selectedApp" class="app-detail">
          <div class="app-detail__header">
            <div class="app-detail__name">{{ selectedApp.appName || selectedApp.application?.appName }}</div>
            <div class="app-detail__code">{{ selectedApp.appCode || selectedApp.application?.appCode }}</div>
          </div>
          <div class="app-detail__tags">
            <el-tag :type="(selectedApp.appType || selectedApp.application?.appType) === '交易' ? 'danger' : 'primary'" size="small">{{ selectedApp.appType || selectedApp.application?.appType }}</el-tag>
            <el-tag :type="(selectedApp.importanceLevel || selectedApp.application?.importanceLevel) === '核心' ? 'danger' : (selectedApp.importanceLevel || selectedApp.application?.importanceLevel) === '重要' ? 'warning' : 'info'" size="small">{{ selectedApp.importanceLevel || selectedApp.application?.importanceLevel }}</el-tag>
            <el-tag type="success" size="small">{{ selectedApp.lifecycle || selectedApp.application?.lifecycle }}</el-tag>
          </div>
          <div class="app-detail__info">
            <div class="info-row"><span class="info-label">所属部门</span><span class="info-value">{{ selectedApp.departmentName || selectedApp.application?.departmentName }}</span></div>
            <div class="info-row"><span class="info-label">技术负责人</span><span class="info-value">{{ selectedApp.techLead || selectedApp.application?.techLead }}</span></div>
            <div class="info-row"><span class="info-label">部署方式</span><span class="info-value">{{ selectedApp.deploymentType || selectedApp.application?.deploymentType }}</span></div>
            <div class="info-row"><span class="info-label">访问方式</span><span class="info-value">{{ selectedApp.accessType || selectedApp.application?.accessType }}</span></div>
            <div class="info-row"><span class="info-label">SLA级别</span><span class="info-value">{{ selectedApp.slaLevel || selectedApp.application?.slaLevel }}</span></div>
            <div class="info-row"><span class="info-label">当前版本</span><span class="info-value">{{ selectedApp.version || selectedApp.application?.version }}</span></div>
            <div class="info-row"><span class="info-label">上线日期</span><span class="info-value">{{ selectedApp.goLiveDate || selectedApp.application?.goLiveDate }}</span></div>
          </div>
          <el-divider />
          <div class="app-detail__section">
            <h4 style="margin-bottom: 12px; color: var(--text-secondary); font-size: 0.875rem;">服务列表 ({{ (selectedApp.services || selectedApp.application?.modules || []).length }})</h4>
            <div v-for="svc in (selectedApp.services || [])" :key="svc.id" class="service-item">
              <span class="service-item__name">{{ svc.serviceName }}</span>
              <span class="service-item__protocol">{{ svc.protocol }}:{{ svc.port }}</span>
            </div>
            <el-empty v-if="!selectedApp.services || selectedApp.services.length === 0" description="暂无服务" :image-size="40" />
          </div>
          <el-divider />
          <div class="app-detail__section">
            <h4 style="margin-bottom: 12px; color: var(--text-secondary); font-size: 0.875rem;">依赖关系 ({{ (selectedApp.dependencies || []).length }})</h4>
            <div v-for="dep in selectedApp.dependencies" :key="dep.id" class="dep-item">
              <el-icon color="var(--info)"><Connection /></el-icon>
              <span>{{ dep.targetAppName }}</span>
              <el-tag size="small" type="info">{{ dep.dependencyType }}</el-tag>
            </div>
            <el-empty v-if="!selectedApp.dependencies || selectedApp.dependencies.length === 0" description="暂无依赖" :image-size="40" />
          </div>
        </div>
        <el-empty v-else description="请选择一个应用查看详情" />
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增应用' : '编辑应用'" width="700px" destroy-on-close>
      <el-form :model="appForm" :rules="formRules" ref="formRef" label-width="100px">
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
          <el-form-item label="应用名称" prop="appName"><el-input v-model="appForm.appName" placeholder="请输入应用名称" /></el-form-item>
          <el-form-item label="英文名称" prop="appNameEn"><el-input v-model="appForm.appNameEn" placeholder="请输入英文名称" /></el-form-item>
          <el-form-item label="应用类型" prop="appType">
            <el-select v-model="appForm.appType" style="width: 100%;">
              <el-option label="交易类" value="交易" />
              <el-option label="渠道类" value="渠道" />
              <el-option label="管理类" value="管理" />
              <el-option label="数据类" value="数据" />
              <el-option label="基础设施" value="基础设施" />
            </el-select>
          </el-form-item>
          <el-form-item label="重要级别" prop="importanceLevel">
            <el-select v-model="appForm.importanceLevel" style="width: 100%;">
              <el-option label="核心" value="核心" />
              <el-option label="重要" value="重要" />
              <el-option label="一般" value="一般" />
              <el-option label="辅助" value="辅助" />
            </el-select>
          </el-form-item>
          <el-form-item label="生命周期" prop="lifecycle">
            <el-select v-model="appForm.lifecycle" style="width: 100%;">
              <el-option label="规划中" value="PLANNING" />
              <el-option label="开发中" value="DEVELOPMENT" />
              <el-option label="测试中" value="TESTING" />
              <el-option label="生产运行" value="PRODUCTION" />
              <el-option label="已废弃" value="DEPRECATED" />
            </el-select>
          </el-form-item>
          <el-form-item label="业务域" prop="businessDomain"><el-input v-model="appForm.businessDomain" /></el-form-item>
          <el-form-item label="所属部门" prop="departmentId">
            <el-select v-model="appForm.departmentId" style="width: 100%;">
              <el-option label="信息科技部" :value="1" />
              <el-option label="运维部" :value="2" />
              <el-option label="数据中心" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="部署方式" prop="deploymentType">
            <el-select v-model="appForm.deploymentType" style="width: 100%;">
              <el-option label="单机" value="单机" />
              <el-option label="集群" value="集群" />
              <el-option label="分布式" value="分布式" />
              <el-option label="云原生" value="云原生" />
            </el-select>
          </el-form-item>
          <el-form-item label="访问方式" prop="accessType">
            <el-select v-model="appForm.accessType" style="width: 100%;">
              <el-option label="内网" value="内网" />
              <el-option label="外网" value="外网" />
              <el-option label="内外网" value="内外网" />
            </el-select>
          </el-form-item>
          <el-form-item label="SLA级别" prop="slaLevel"><el-input v-model="appForm.slaLevel" /></el-form-item>
          <el-form-item label="当前版本" prop="version"><el-input v-model="appForm.version" /></el-form-item>
          <el-form-item label="上线日期" prop="goLiveDate"><el-input v-model="appForm.goLiveDate" placeholder="如: 2026-06-01" /></el-form-item>
          <el-form-item label="产品经理" prop="pmName"><el-input v-model="appForm.pmName" /></el-form-item>
          <el-form-item label="技术负责人" prop="techLead"><el-input v-model="appForm.techLead" /></el-form-item>
        </div>
        <el-form-item label="应用描述" prop="description"><el-input v-model="appForm.description" type="textarea" :rows="3" /></el-form-item>
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
import { archAppApi } from '@/api'

const searchKeyword = ref('')
const filterType = ref('')
const filterLevel = ref('')
const selectedApp = ref(null)
const loading = ref(false)
const formRef = ref(null)
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const dialogVisible = ref(false)
const dialogMode = ref('add')
const appForm = reactive({
  id: null, appName: '', appNameEn: '', appType: '交易', importanceLevel: '一般',
  lifecycle: 'PLANNING', deploymentType: '单机', accessType: '内网', businessDomain: '',
  techLead: '', pmName: '', description: '', departmentId: null, slaLevel: '', version: '', goLiveDate: ''
})

const formRules = {
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  appType: [{ required: true, message: '请选择应用类型', trigger: 'change' }]
}

const appList = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterType.value) params.status = filterType.value
    if (filterLevel.value) params.lifecycle = filterLevel.value
    const res = await archAppApi.list(params)
    appList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) { ElMessage.error('加载失败') } finally { loading.value = false }
}

const handleAdd = () => {
  dialogMode.value = 'add'
  Object.assign(appForm, {
    id: null, appName: '', appNameEn: '', appType: '交易', importanceLevel: '一般',
    lifecycle: 'PLANNING', deploymentType: '单机', accessType: '内网', businessDomain: '',
    techLead: '', pmName: '', description: '', departmentId: null, slaLevel: '', version: '', goLiveDate: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  if (row && row.application) {
    Object.assign(appForm, row.application)
  } else if (row) {
    Object.assign(appForm, row)
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (dialogMode.value === 'add') {
      await archAppApi.create(appForm)
    } else {
      await archAppApi.update(appForm.id, appForm)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (e) { ElMessage.error('操作失败') }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除 ${row.appName}？`, '确认', { type: 'warning' })
    await archAppApi.delete(row.id)
    ElMessage.success('删除成功')
    if (selectedApp.value && selectedApp.value.id === row.id) selectedApp.value = null
    loadData()
  } catch {}
}

const handleRowClick = async (row) => {
  selectedApp.value = { ...row, services: [], dependencies: [], application: row }
  try {
    const [svcRes, depRes] = await Promise.all([archAppApi.services(row.id), archAppApi.dependencies(row.id)])
    selectedApp.value.services = svcRes.data || []
    selectedApp.value.dependencies = depRes.data || []
  } catch {}
}

const getLevelColor = (level) => level === '核心' ? 'var(--danger)' : level === '重要' ? 'var(--warning)' : 'var(--text-secondary)'
const getLifecycleClass = (lifecycle) => lifecycle === 'PRODUCTION' ? 'online' : lifecycle === 'DEVELOPMENT' ? 'warning' : 'offline'

onMounted(() => { loadData() })
</script>

<style scoped lang="scss">
.app-detail {
  &__header {
    margin-bottom: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  &__name {
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 4px;
  }
  &__code {
    font-family: var(--font-mono);
    font-size: 0.875rem;
    color: var(--accent);
  }
  &__tags {
    display: flex;
    gap: 8px;
    margin-bottom: 20px;
  }
  &__info {
    .info-row {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px solid var(--border);
      &:last-child { border-bottom: none; }
    }
    .info-label { color: var(--text-secondary); }
    .info-value { color: var(--text-primary); font-weight: 500; }
  }
  &__section { margin-top: 16px; }
}

.service-item, .dep-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--bg-dark-3);
  border-radius: 6px;
  margin-bottom: 8px;
  font-size: 0.875rem;
  .service-item__name, .dep-item span:first-of-type { flex: 1; color: var(--text-primary); }
  .service-item__protocol { color: var(--text-muted); font-family: var(--font-mono); font-size: 0.75rem; }
}
</style>
