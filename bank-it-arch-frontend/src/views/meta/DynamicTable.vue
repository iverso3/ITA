<template>
  <div class="dynamic-table">
    <!-- 表格配置工具栏 -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <el-select v-model="selectedModelId" placeholder="选择模型" @change="handleModelChange" filterable>
          <el-option v-for="m in modelList" :key="m.id" :label="m.modelName" :value="m.id" />
        </el-select>
        <el-select v-model="selectedVersion" placeholder="选择版本" :disabled="!selectedModelId">
          <el-option v-for="v in versionList" :key="v.id" :label="`v${v.version}.0 - ${v.versionName}`" :value="v.id" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="handleRefresh" :loading="loading">刷新</el-button>
        <el-button @click="handleExport">导出</el-button>
        <el-button @click="handlePrint">打印</el-button>
      </div>
    </div>

    <!-- 高级搜索 -->
    <div class="advanced-search" v-if="showAdvancedSearch">
      <el-collapse v-model="searchExpanded">
        <el-collapse-item title="高级搜索" name="search">
          <el-form :model="searchForm" label-width="100px" inline>
            <el-form-item v-for="field in searchableFields" :key="field.fieldCode" :label="field.fieldName">
              <el-input
                v-if="field.displayType === 'INPUT'"
                v-model="searchForm[field.fieldCode]"
                placeholder="模糊搜索"
                clearable
              />
              <el-select
                v-else-if="field.displayType === 'SELECT'"
                v-model="searchForm[field.fieldCode]"
                placeholder="请选择"
                clearable
              >
                <el-option v-for="opt in field.options" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
              <el-date-picker
                v-else-if="field.displayType === 'DATE'"
                v-model="searchForm[field.fieldCode]"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
              />
              <el-date-picker
                v-else-if="field.displayType === 'DATERANGE'"
                v-model="searchForm[field.fieldCode]"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleResetSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 数据表格 -->
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="tableData"
      :border="border"
      :stripe="stripe"
      :size="size"
      :height="tableHeight"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
    >
      <el-table-column v-if="showSelection" type="selection" width="55" fixed="left" />

      <el-table-column
        v-for="column in visibleColumns"
        :key="column.fieldCode"
        :prop="column.fieldCode"
        :label="column.fieldName"
        :width="column.width"
        :min-width="column.minWidth"
        :fixed="column.fixed"
        :align="column.align || 'left'"
        :sortable="column.sortable ? 'custom' : false"
        :show-overflow-tooltip="column.showOverflowTooltip !== false"
      >
        <template #default="{ row }">
          <!-- 格式化显示 -->
          <span v-if="column.maskType">{{ formatMaskValue(row[column.fieldCode], column) }}</span>
          <span v-else-if="column.displayType === 'SELECT'">{{ formatSelectValue(row[column.fieldCode], column) }}</span>
          <span v-else-if="column.displayType === 'SWITCH'">
            <el-tag :type="row[column.fieldCode] ? 'success' : 'info'">
              {{ row[column.fieldCode] ? '是' : '否' }}
            </el-tag>
          </span>
          <span v-else-if="column.displayType === 'DATE'">{{ formatDate(row[column.fieldCode]) }}</span>
          <span v-else-if="column.displayType === 'DATETIME'">{{ formatDateTime(row[column.fieldCode]) }}</span>
          <span v-else>{{ row[column.fieldCode] ?? '-' }}</span>
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column v-if="showOperation" label="操作" :width="operationWidth" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
          <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container" v-if="showPagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="详情" width="800px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item v-for="column in visibleColumns" :key="column.fieldCode" :label="column.fieldName">
          <span v-if="column.maskType">{{ formatMaskValue(currentRow[column.fieldCode], column) }}</span>
          <span v-else-if="column.displayType === 'SELECT'">{{ formatSelectValue(currentRow[column.fieldCode], column) }}</span>
          <span v-else>{{ currentRow[column.fieldCode] ?? '-' }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  modelCode: { type: String, default: '' },
  showTabs: { type: Boolean, default: false },
  showAdvancedSearch: { type: Boolean, default: true },
  showSelection: { type: Boolean, default: true },
  showOperation: { type: Boolean, default: true },
  showPagination: { type: Boolean, default: true },
  border: { type: Boolean, default: true },
  stripe: { type: Boolean, default: true },
  size: { type: String, default: 'default' },
  tableHeight: { type: [String, Number], default: null },
  operationWidth: { type: Number, default: 200 }
})

const emit = defineEmits(['view', 'edit', 'delete', 'selection-change', 'query'])

const tableRef = ref(null)
const loading = ref(false)
const modelList = ref([])
const selectedModelId = ref(null)
const versionList = ref([])
const selectedVersion = ref(null)
const tableConfig = ref({ columns: [] })
const tableData = ref([])
const currentRow = ref(null)
const detailDialogVisible = ref(false)
const searchExpanded = ref(false)
const searchForm = reactive({})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const sortableInfo = reactive({
  prop: '',
  order: ''
})

onMounted(() => {
  loadModels()
})

const loadModels = async () => {
  try {
    const res = await fetch('/api/v1/meta/models/list')
    modelList.value = await res.json()
  } catch (error) {
    console.error('加载模型列表失败:', error)
  }
}

const handleModelChange = async (modelId) => {
  selectedVersion.value = null
  pagination.page = 1
  await Promise.all([loadVersions(modelId), loadTableConfig(modelId)])
  await loadData()
}

const loadVersions = async (modelId) => {
  try {
    const res = await fetch(`/api/v1/meta/publish/history/${modelId}`)
    versionList.value = await res.json()
    if (versionList.value.length > 0) {
      const published = versionList.value.find(v => v.status === 'PUBLISHED')
      selectedVersion.value = published?.id || versionList.value[0].id
    }
  } catch (error) {
    console.error('加载版本列表失败:', error)
  }
}

const loadTableConfig = async (modelId) => {
  try {
    const res = await fetch(`/api/v1/meta/table-config/${modelId}`)
    tableConfig.value = await res.json()
  } catch (error) {
    console.error('加载表格配置失败:', error)
  }
}

const loadData = async () => {
  if (!selectedModelId.value) return

  loading.value = true
  try {
    const query = {
      modelId: selectedModelId.value,
      versionId: selectedVersion.value,
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm
    }

    if (sortableInfo.prop) {
      query.sortBy = sortableInfo.prop
      query.sortOrder = sortableInfo.order
    }

    const res = await fetch('/api/v1/meta/query', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(query)
    })

    const result = await res.json()
    tableData.value = result.data?.records || []
    pagination.total = result.data?.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const visibleColumns = computed(() => {
  if (!tableConfig.value.columns) return []
  return tableConfig.value.columns.filter(col => col.visible !== false)
})

const searchableFields = computed(() => {
  return visibleColumns.value.filter(col => col.searchable !== false)
})

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleResetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = null
  })
  pagination.page = 1
  loadData()
}

const handleRefresh = () => {
  loadData()
}

const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

const handlePrint = () => {
  window.print()
}

const handleSelectionChange = (selection) => {
  emit('selection-change', selection)
}

const handleSortChange = ({ prop, order }) => {
  sortableInfo.prop = prop
  sortableInfo.order = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  loadData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  loadData()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

const handleView = (row) => {
  currentRow.value = row
  detailDialogVisible.value = true
  emit('view', row)
}

const handleEdit = (row) => {
  emit('edit', row)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该数据吗？', '提示', { type: 'warning' })
    await fetch(`/api/v1/meta/entity/${row.id}`, { method: 'DELETE' })
    ElMessage.success('删除成功')
    loadData()
    emit('delete', row)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const formatMaskValue = (value, column) => {
  if (value == null) return '-'
  switch (column.maskType) {
    case 'PHONE': return value.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
    case 'ID_CARD': return value.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2')
    case 'EMAIL': return value.replace(/(.{2}).*@/, '$1***@')
    case 'BANK_CARD': return value.replace(/(\d{4})\d+(\d{4})/, '$1****$2')
    default: return value
  }
}

const formatSelectValue = (value, column) => {
  if (value == null) return '-'
  const option = column.options?.find(opt => opt.value === value)
  return option?.label || value
}

const formatDate = (value) => {
  if (!value) return '-'
  return value
}

const formatDateTime = (value) => {
  if (!value) return '-'
  return value
}

defineExpose({ loadData, refreshData: loadData })
</script>

<style scoped>
.dynamic-table {
  padding: 16px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 8px;
}

.advanced-search {
  margin-bottom: 16px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
