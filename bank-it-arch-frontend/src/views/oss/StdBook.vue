<template>
  <div class="standing-book-container">
    <!-- 查询区域 -->
    <el-collapse v-model="searchVisible">
      <el-collapse-item title="查询条件" name="search">
        <el-form :inline="true" :model="queryForm" class="search-form">
          <el-form-item label="开源软件名称">
            <el-input v-model="queryForm.swName" placeholder="请输入开源软件名称" clearable />
          </el-form-item>
          <el-form-item label="开源软件版本">
            <el-input v-model="queryForm.swVersion" placeholder="请输入开源软件版本" clearable />
          </el-form-item>
          <el-form-item label="软件分类">
            <el-select v-model="queryForm.swCategory" placeholder="请选择" clearable>
              <el-option label="开源组件框架" value="开源组件框架" />
              <el-option label="开源基础软件" value="开源基础软件" />
            </el-select>
          </el-form-item>
          <el-form-item label="应用名称">
            <el-input v-model="queryForm.appName" placeholder="请输入应用名称" clearable />
          </el-form-item>
          <el-form-item label="所属环境">
            <el-select v-model="queryForm.environment" placeholder="请选择" clearable>
              <el-option label="生产环境" value="PROD" />
              <el-option label="开发测试环境" value="DEVTEST" />
            </el-select>
          </el-form-item>
          <el-form-item label="开源软件全称">
            <el-input v-model="queryForm.swFullName" placeholder="请输入开源软件全称" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="default" @click="handleReset">重置</el-button>
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </el-form-item>
        </el-form>
      </el-collapse-item>
    </el-collapse>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary">手工同步</el-button>
      <el-button type="warning">导出</el-button>
      <el-button type="danger">导入</el-button>
    </div>

    <!-- 列表区域 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="swName" label="开源软件名称" min-width="120" />
      <el-table-column prop="swVersion" label="开源软件版本" width="120" />
      <el-table-column prop="swFullName" label="开源软件全称" min-width="180" />
      <el-table-column prop="swCategory" label="软件分类" width="120" />
      <el-table-column prop="licAbbr" label="开源许可证" width="120" />
      <el-table-column prop="environment" label="所属环境" width="120">
        <template #default="{ row }">
          {{ row.environment === 'PROD' ? '生产环境' : '开发测试环境' }}
        </template>
      </el-table-column>
      <el-table-column prop="appName" label="应用名称" min-width="150" />
      <el-table-column prop="scanDate" label="扫描时间" width="120" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      class="pagination"
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadData"
      @current-change="loadData"
    />

    <!-- 详情弹窗 -->
    <el-dialog
      :title="detailDialogTitle"
      v-model="detailVisible"
      width="900px"
      :close-on-click-modal="false"
    >
      <!-- 开源组件框架的详情字段 -->
      <el-table v-if="currentMain.swCategory === '开源组件框架'" :data="detailData" border stripe>
        <el-table-column prop="installPath" label="项目路径" min-width="200" />
        <el-table-column prop="scanTime" label="扫描时间" width="160" />
        <el-table-column prop="projectName" label="项目" width="120" />
        <el-table-column prop="fileType" label="文件类型" width="100" />
        <el-table-column prop="dependType" label="依赖类型" width="100" />
      </el-table>

      <!-- 开源基础软件的详情字段 -->
      <el-table v-else :data="detailData" border stripe>
        <el-table-column prop="installPath" label="项目路径" min-width="200" />
        <el-table-column prop="scanTime" label="扫描时间" width="160" />
        <el-table-column prop="ipOrHostName" label="IP/主机名称" min-width="150" />
        <el-table-column prop="command" label="启动命令" min-width="200" />
        <el-table-column prop="isCommerc" label="是否第三方产品" width="120">
          <template #default="{ row }">
            {{ row.isCommerc === '是' ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column prop="commercProductName" label="第三方产品名称" width="150" />
        <el-table-column prop="commercProductVersion" label="第三方产品版本" width="150" />
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="detailPagination.page"
        v-model:page-size="detailPagination.pageSize"
        :total="detailPagination.total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadDetails"
        @current-change="loadDetails"
        style="margin-top: 16px; justify-content: flex-end;"
      />

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ossUseStandingBookApi } from '@/api'
import { ElMessage } from 'element-plus'

const searchVisible = ref(true)
const loading = ref(false)
const detailVisible = ref(false)
const detailDialogTitle = ref('使用详情')

const queryForm = reactive({
  swName: '',
  swVersion: '',
  swCategory: '',
  appName: '',
  environment: '',
  swFullName: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const tableData = ref([])
const currentMain = ref({})
const detailData = ref([])

const detailPagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

onMounted(() => {
  loadData()
})

function handleReset() {
  Object.assign(queryForm, {
    swName: '',
    swVersion: '',
    swCategory: '',
    appName: '',
    environment: '',
    swFullName: ''
  })
  pagination.page = 1
  loadData()
}

function handleQuery() {
  pagination.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...queryForm
    }
    const res = await ossUseStandingBookApi.listMain(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
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
    const params = {
      parentId: currentMain.value.id,
      page: detailPagination.page,
      pageSize: detailPagination.pageSize
    }
    const res = await ossUseStandingBookApi.listDetails(params)
    detailData.value = res.data.records || []
    detailPagination.total = res.data.total || 0
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}
</script>

<style scoped>
.standing-book-container {
  padding: 16px;
}

.search-form {
  padding: 0 16px;
}

.toolbar {
  padding: 12px 16px;
  display: flex;
  gap: 8px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
