<template>
  <div class="oss-impl-apply-page">
    <div class="page-card">
      <!-- Page Header -->
      <div class="page-header">
        <h2 class="page-title">开源软件引入申请</h2>
      </div>

      <!-- Query Form -->
      <div class="query-section">
        <div class="query-row">
          <div class="query-item">
            <label class="query-label">申请单号</label>
            <el-input v-model="queryForm.implApplyNo" placeholder="请输入" clearable class="query-input" />
          </div>
          <div class="query-item">
            <label class="query-label">申请类型</label>
            <el-select v-model="queryForm.implApplyType" placeholder="请选择" clearable class="query-input">
              <el-option label="首次引入" value="0" />
              <el-option label="新版本引入" value="1" />
            </el-select>
          </div>
          <div class="query-item">
            <label class="query-label">软件分类</label>
            <el-select v-model="queryForm.swCategory" placeholder="请选择" clearable class="query-input">
              <el-option label="开源基础软件" value="BASE" />
              <el-option label="开源工具软件" value="TOOL" />
              <el-option label="开源组件" value="CMPNT" />
            </el-select>
          </div>
          <div class="query-item">
            <label class="query-label">开源软件名称</label>
            <el-input v-model="queryForm.swName" placeholder="请输入" clearable class="query-input" />
          </div>
          <div class="query-actions">
            <el-button @click="handleReset" class="btn-reset">重置</el-button>
            <el-button type="primary" @click="handleQuery" class="btn-search">
              <el-icon><Search /></el-icon> 查询
            </el-button>
          </div>
        </div>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="data-count">共 <em>{{ pagination.total }}</em> 条</span>
        </div>
      </div>

      <!-- Table -->
      <el-table :data="tableData" v-loading="loading" stripe class="data-table">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="implApplyNo" label="申请单号" width="200" show-overflow-tooltip />
        <el-table-column prop="flowTitle" label="流程标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="implApplyType" label="申请类型" width="100" align="center">
          <template #default="{ row }">
            <span class="tag" :class="row.implApplyType === '0' ? 'tag-new' : 'tag-version'">
              {{ row.implApplyType === '0' ? '首次引入' : '新版本引入' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="swCategory" label="软件分类" width="120" align="center">
          <template #default="{ row }">
            <span class="tag" :class="'tag-' + row.swCategory">
              {{ getSwCategoryText(row.swCategory) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="swName" label="开源软件名称" width="150" show-overflow-tooltip />
        <el-table-column prop="swVersion" label="版本" width="120" align="center" />
        <el-table-column prop="implUserName" label="申请人" width="90" align="center" />
        <el-table-column prop="implTeamName" label="申请团队" width="150" show-overflow-tooltip />
        <el-table-column prop="procInstId" label="审批状态" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.procInstId" class="status-approved">已提交</span>
            <span v-else class="status-draft">草稿</span>
          </template>
        </el-table-column>
        <el-table-column prop="createDatetime" label="创建时间" width="160" align="center" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <div class="operation-btns">
              <el-button type="primary" link @click.stop="handleView(row)">查看</el-button>
              <el-button type="primary" link @click.stop="handleEdit(row)" v-if="!row.procInstId">编辑</el-button>
              <el-button type="primary" link @click.stop="handleSubmit(row)" v-if="!row.procInstId">提交</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { ossImplApplyApi } from '@/api'

const router = useRouter()

const loading = ref(false)
const activeTab = ref('list')
const tableData = ref([])
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const queryForm = reactive({
  implApplyNo: '',
  implApplyType: '',
  swCategory: '',
  swName: ''
})

const swCategoryOptions = {
  BASE: '开源基础软件',
  TOOL: '开源工具软件',
  CMPNT: '开源组件'
}

const getSwCategoryText = (category) => {
  return swCategoryOptions[category] || category
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...queryForm
    }
    const res = await ossImplApplyApi.list(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('Failed to load data:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  queryForm.implApplyNo = ''
  queryForm.implApplyType = ''
  queryForm.swCategory = ''
  queryForm.swName = ''
  handleQuery()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  loadData()
}

const handlePageChange = (val) => {
  pagination.page = val
  loadData()
}

const handleCreate = () => {
  router.push('/oss/impl-apply/form')
}

const handleView = (row) => {
  router.push(`/oss/impl-apply/form?id=${row.id}&mode=view`)
}

const handleEdit = (row) => {
  router.push(`/oss/impl-apply/form?id=${row.id}&mode=edit`)
}

const handleSubmit = async (row) => {
  try {
    await ElMessageBox.confirm('确认提交该申请？提交后将启动审批流程。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 调用启动审批流程接口
    ElMessage.success('申请已提交')
    loadData()
  } catch (e) {
    // cancelled
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.oss-impl-apply-page {
  padding: 16px;
}

.page-card {
  background: #fff;
  border-radius: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.query-section {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.query-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.query-item {
  display: flex;
  align-items: center;
  min-width: 200px;
}

.query-label {
  width: 100px;
  font-size: 14px;
  color: #606266;
}

.query-input {
  flex: 1;
  min-width: 120px;
}

.query-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
}

.data-count {
  font-size: 14px;
  color: #909399;
}

.data-count em {
  color: #409eff;
  font-style: normal;
}

.data-table {
  margin: 0 20px;
}

.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.tag-BASE { background: #ecf5ff; color: #409eff; }
.tag-TOOL { background: #f0f9ff; color: #1989fa; }
.tag-CMPNT { background: #fdf6ec; color: #e6a23c; }
.tag-new { background: #ecf5ff; color: #409eff; }
.tag-version { background: #f0f9ff; color: #1989fa; }

.status-approved {
  color: #67c23a;
}

.status-draft {
  color: #909399;
}

.operation-btns {
  display: flex;
  gap: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
}
</style>
