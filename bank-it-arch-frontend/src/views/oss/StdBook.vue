<template>
  <div class="standing-book-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">使用台账管理</h2>
    </div>

    <!-- 查询区域 - 卡片式 -->
    <div class="query-card">
      <div class="query-row">
        <div class="query-item">
          <label class="query-label">开源软件名称</label>
          <el-input v-model="queryForm.swName" placeholder="请输入" clearable class="query-input" />
        </div>
        <div class="query-item">
          <label class="query-label">开源软件版本</label>
          <el-input v-model="queryForm.swVersion" placeholder="请输入" clearable class="query-input" />
        </div>
        <div class="query-item">
          <label class="query-label">软件分类</label>
          <el-select v-model="queryForm.swCategory" placeholder="请选择" clearable class="query-input">
            <el-option label="开源组件框架" value="开源组件框架" />
            <el-option label="开源基础软件" value="开源基础软件" />
          </el-select>
        </div>
        <div class="query-item">
          <label class="query-label">应用名称</label>
          <el-select
            v-model="queryForm.appName"
            placeholder="请选择"
            clearable
            filterable
            remote
            :remote-method="searchApps"
            :loading="appLoading"
            class="query-input"
          >
            <el-option v-for="app in appList" :key="app.appNo" :label="app.appName" :value="app.appName" />
          </el-select>
        </div>
      </div>
      <div class="query-row">
        <div class="query-item">
          <label class="query-label">所属环境</label>
          <el-select v-model="queryForm.environment" placeholder="请选择" clearable class="query-input">
            <el-option label="生产环境" value="PROD" />
            <el-option label="开发测试环境" value="DEVTEST" />
          </el-select>
        </div>
        <div class="query-item query-item--wide">
          <label class="query-label">开源软件全称</label>
          <el-input v-model="queryForm.swFullName" placeholder="请输入" clearable class="query-input" />
        </div>
        <div class="query-actions">
          <el-button @click="handleReset" class="btn-reset">重置</el-button>
          <el-button type="primary" @click="handleQuery" class="btn-search">
            <el-icon><Search /></el-icon> 查询
          </el-button>
        </div>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <span class="data-count">共 <em>{{ pagination.total }}</em> 条</span>
      </div>
      <div class="action-right">
        <el-button type="primary" plain>手工同步</el-button>
        <el-dropdown trigger="click" @command="handleExport">
          <el-button type="warning" plain>
            导出<el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="main">导出主要信息表</el-dropdown-item>
              <el-dropdown-item command="detail">导出详情信息表</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="danger" plain :loading="importLoading" @click="handleImportClick">导入</el-button>
        <input ref="importRef" type="file" accept=".xlsx,.xls" style="display:none" @change="handleImportChange" />
      </div>
    </div>

    <!-- 列表区域 -->
    <div class="table-wrapper">
      <el-table :data="tableData" border stripe v-loading="loading" class="data-table" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="45" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="swName" label="开源软件名称" min-width="120" />
        <el-table-column prop="swVersion" label="版本" width="100" align="center" />
        <el-table-column prop="swFullName" label="开源软件全称" min-width="160" />
        <el-table-column prop="swCategory" label="分类" width="120" align="center">
          <template #default="{ row }">
            <span class="category-tag" :class="row.swCategory === '开源组件框架' ? 'tag-green' : 'tag-blue'">
              {{ row.swCategory }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="licAbbr" label="许可证" width="100" align="center" />
        <el-table-column prop="environment" label="环境" width="100" align="center">
          <template #default="{ row }">
            <span class="env-tag" :class="row.environment === 'PROD' ? 'env-prod' : 'env-dev'">
              {{ row.environment === 'PROD' ? '生产环境' : '开发测试环境' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="appName" label="应用名称" min-width="140" />
        <el-table-column prop="scanDate" label="扫描时间" width="110" align="center" />
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog :title="detailDialogTitle" v-model="detailVisible" width="900px" :close-on-click-modal="false">
      <el-table :data="detailData" border stripe>
        <el-table-column prop="installPath" label="项目路径" min-width="200" show-overflow-tooltip />
        <el-table-column prop="scanTime" label="扫描时间" width="160" align="center" />
        <template v-if="currentMain.swCategory === '开源组件框架'">
          <el-table-column prop="projectName" label="项目" width="120" align="center" />
          <el-table-column prop="fileType" label="文件类型" width="100" align="center" />
          <el-table-column prop="dependType" label="依赖类型" width="100" align="center" />
        </template>
        <template v-else>
          <el-table-column prop="ipOrHostName" label="IP/主机名称" min-width="140" />
          <el-table-column prop="command" label="启动命令" min-width="180" show-overflow-tooltip />
          <el-table-column prop="isCommerc" label="是否第三方" width="100" align="center">
            <template #default="{ row }">
              {{ row.isCommerc === '是' ? '是' : '否' }}
            </template>
          </el-table-column>
          <el-table-column prop="commercProductName" label="产品名称" width="130" align="center" />
          <el-table-column prop="commercProductVersion" label="产品版本" width="130" align="center" />
        </template>
      </el-table>
      <el-pagination
        v-model:current-page="detailPagination.page"
        v-model:page-size="detailPagination.pageSize"
        :total="detailPagination.total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadDetails"
        @current-change="loadDetails"
        style="margin-top: 16px; display: flex; justify-content: flex-end;"
      />
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ossUseStandingBookApi, archAppApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'

const loading = ref(false)
const detailVisible = ref(false)
const detailDialogTitle = ref('使用详情')
const appLoading = ref(false)
const appList = ref([])
const importLoading = ref(false)
const importRef = ref(null)
const importFile = ref(null)

const queryForm = reactive({
  swName: '',
  swVersion: '',
  swCategory: '',
  appName: '',
  environment: '',
  swFullName: ''
})

const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const tableData = ref([])
const currentMain = ref({})
const detailData = ref([])
const detailPagination = reactive({ page: 1, pageSize: 20, total: 0 })
const selectedRows = ref([])

function handleSelectionChange(selection) {
  selectedRows.value = selection
}

onMounted(() => {
  loadData()
  loadApps()
})

async function searchApps(keyword) {
  if (!keyword) { loadApps(); return }
  appLoading.value = true
  try { appList.value = (await archAppApi.searchApps(keyword)).data || [] }
  catch (e) { console.error('搜索应用失败', e) }
  finally { appLoading.value = false }
}

async function loadApps() {
  try { appList.value = ((await archAppApi.list({ page: 1, pageSize: 100 })).data.records || []) }
  catch (e) { console.error('加载应用列表失败', e) }
}

function handleReset() {
  Object.assign(queryForm, { swName: '', swVersion: '', swCategory: '', appName: '', environment: '', swFullName: '' })
  pagination.page = 1
  loadData()
}

function handleQuery() { pagination.page = 1; loadData() }

async function loadData() {
  loading.value = true
  try {
    const res = await ossUseStandingBookApi.listMain({ page: pagination.page, pageSize: pagination.pageSize, ...queryForm })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) { ElMessage.error('加载数据失败') }
  finally { loading.value = false }
}

async function handleDetail(row) {
  currentMain.value = row
  detailDialogTitle.value = '使用详情'
  detailVisible.value = true
  detailPagination.page = 1
  await loadDetails()
}

async function loadDetails() {
  try {
    const res = await ossUseStandingBookApi.listDetails({ parentId: currentMain.value.id, page: detailPagination.page, pageSize: detailPagination.pageSize })
    detailData.value = res.data.records || []
    detailPagination.total = res.data.total || 0
  } catch (e) { ElMessage.error('加载详情失败') }
}

function handleImportClick() {
  importFile.value = null
  importRef.value?.click()
}

async function handleImportChange(e) {
  const file = e.target.files[0]
  if (!file) return
  importLoading.value = true
  try {
    const res = await ossUseStandingBookApi.importMain(file)
    const data = res.data || {}
    const msg = `导入完成：共 ${data.total || 0} 行，成功 ${data.success || 0} 行，失败 ${data.failed || 0} 行`
    if (data.failed > 0 && data.errors && data.errors.length > 0) {
      const errList = data.errors.slice(0, 5).map(e => `第${e.row}行: ${e.reason}`).join('\n')
      ElMessage.warning({ message: msg + '\n' + errList, duration: 0, showClose: true })
    } else {
      ElMessage.success(msg)
    }
    await loadData()
  } catch (e) {
    ElMessage.error(e?.message || '导入失败')
  } finally {
    importLoading.value = false
    e.target.value = ''
  }
}

// 导出处理函数
async function handleExport(command) {
  if (command === 'main') {
    await handleExportMain()
  } else if (command === 'detail') {
    await handleExportDetail()
  }
}

// 导出主要信息表（Main表）
async function handleExportMain() {
  loading.value = true
  try {
    const params = { ...queryForm, page: 1, pageSize: 10000 }
    const res = await ossUseStandingBookApi.exportMain(params)
    const data = res.data || []

    const exportData = data.map(item => ({
      '开源软件名称': item.swName || '',
      '版本': item.swVersion || '',
      '开源软件全称': item.swFullName || '',
      '分类': item.swCategory || '',
      '许可证': item.licAbbr || '',
      '环境': item.environment === 'PROD' ? '生产环境' : '开发测试环境',
      '应用名称': item.appName || '',
      '应用编号': item.appNo || '',
      '扫描日期': item.scanDate || '',
      '同步时间': item.syncDatetime || '',
      '是否第三方': item.isCommerc === '是' ? '是' : '否'
    }))

    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '使用台账主表')
    XLSX.writeFile(workbook, `使用台账主表_${new Date().toISOString().slice(0, 10)}.xlsx`)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

// 导出详情信息表（Detail表）
async function handleExportDetail() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择一条主表记录后再导出详情')
    return
  }
  if (selectedRows.value.length > 1) {
    ElMessage.warning('只能选择一条记录导出详情，请重新选择')
    return
  }
  const selectedRow = selectedRows.value[0]
  loading.value = true
  try {
    const params = { parentId: selectedRow.id, page: 1, pageSize: 10000 }
    const res = await ossUseStandingBookApi.listDetails(params)
    const data = res.data?.records || []

    const isComponent = selectedRow.swCategory === '开源组件框架'

    const exportData = data.map(item => {
      const row = {
        '开源软件名称': item.swName || '',
        '版本': item.swVersion || '',
        '开源软件全称': item.swFullName || '',
        '应用名称': item.appName || '',
        '许可证': item.licAbbr || '',
        '环境': item.environment === 'PROD' ? '生产环境' : '开发测试环境',
        '项目路径': item.installPath || '',
        '扫描时间': item.scanTime ? item.scanTime.slice(0, 19) : ''
      }
      if (isComponent) {
        row['项目'] = item.projectName || ''
        row['文件类型'] = item.fileType || ''
        row['依赖类型'] = item.dependType || ''
      } else {
        row['IP/主机名称'] = item.ipOrHostName || ''
        row['启动命令'] = item.command || ''
        row['是否第三方'] = item.isCommerc === '是' ? '是' : '否'
        row['产品名称'] = item.commercProductName || ''
        row['产品版本'] = item.commercProductVersion || ''
      }
      return row
    })

    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    const sheetName = isComponent ? '组件框架详情' : '基础软件详情'
    XLSX.utils.book_append_sheet(workbook, worksheet, sheetName)
    XLSX.writeFile(workbook, `使用台账详情_${new Date().toISOString().slice(0, 10)}.xlsx`)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.standing-book-container {
  padding: 24px;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

// 页面标题
.page-header {
  margin-bottom: 20px;
  .page-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0;
  }
}

// 查询卡片
.query-card {
  background: var(--bg-card);
  border-radius: 8px;
  padding: 20px 24px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.query-row {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
  & + .query-row { margin-top: 12px; }
}

.query-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 160px;
  &--wide { flex: 1; min-width: 200px; }
}

.query-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
  white-space: nowrap;
}

.query-input {
  width: 100%;
  :deep(.el-input__wrapper) {
    background: var(--bg-card) !important;
    box-shadow: 0 0 0 1px var(--border) inset !important;
  }
  :deep(.el-select__wrapper) {
    background: var(--bg-card) !important;
    box-shadow: 0 0 0 1px var(--border) inset !important;
  }
}

.query-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

// 操作栏
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.action-left {
  .data-count {
    font-size: 13px;
    color: var(--text-secondary);
    em { color: var(--accent); font-style: normal; font-weight: 600; }
  }
}

.action-right {
  display: flex;
  gap: 8px;
}

// 表格区域
.table-wrapper {
  flex: 1;
  background: var(--bg-card);
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
}

.data-table {
  width: 100%;
  :deep(.el-table__header th) { background: var(--bg-dark-2) !important; }
}

// 分类标签
.category-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  &.tag-green { background: rgba(103, 194, 58, 0.12); color: #67c23a; }
  &.tag-blue { background: rgba(64, 158, 255, 0.12); color: #409eff; }
}

// 环境标签
.env-tag {
  font-size: 12px;
  font-weight: 500;
  &.env-prod { color: #f56c6c; }
  &.env-dev { color: #67c23a; }
}

// 分页
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}
</style>
