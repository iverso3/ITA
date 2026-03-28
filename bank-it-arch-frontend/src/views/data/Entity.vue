<template>
  <div class="data-entity animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">数据架构管理</h1>
      <p class="page-header__desc">数据实体、数据流向和数据分布视图</p>
    </div>
    <div class="page-card">
      <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
        <div style="display: flex; gap: 12px;">
          <el-input v-model="searchKeyword" placeholder="搜索实体名称" style="width: 240px;" clearable />
          <el-select v-model="filterDbType" placeholder="数据库类型" clearable style="width: 140px;">
            <el-option label="MySQL" value="MySQL" />
            <el-option label="Oracle" value="Oracle" />
            <el-option label="MongoDB" value="MongoDB" />
          </el-select>
        </div>
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增实体</el-button>
      </div>
      <el-table :data="entityList" v-loading="loading" stripe>
        <el-table-column prop="entityName" label="实体名称" min-width="160" />
        <el-table-column prop="entityCode" label="实体编码" width="160"><template #default="{ row }"><code style="color: var(--accent);">{{ row.entityCode }}</code></template></el-table-column>
        <el-table-column prop="appName" label="所属应用" width="140" />
        <el-table-column prop="dbType" label="存储类型" width="100"><template #default="{ row }"><el-tag size="small">{{ row.dbType }}</el-tag></template></el-table-column>
        <el-table-column prop="tableName" label="表名" width="140"><template #default="{ row }"><span class="font-mono">{{ row.tableName }}</span></template></el-table-column>
        <el-table-column prop="sensitivity" label="敏感等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.sensitivity === 'HIGHLY_CONFIDENTIAL' ? 'danger' : row.sensitivity === 'CONFIDENTIAL' ? 'warning' : 'info'">{{ getSensitivityText(row.sensitivity) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fieldCount" label="字段数" width="80" align="center" />
        <el-table-column prop="recordCount" label="记录数" width="100" align="right">
          <template #default="{ row }"><span class="font-mono">{{ formatNumber(row.recordCount) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;"><el-pagination v-model:current-page="pagination.page" :total="pagination.total" layout="total, prev, pager, next" @current-change="loadData" /></div>
    </div>
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增数据实体' : '编辑数据实体'" width="550px" destroy-on-close>
      <el-form :model="entityForm" label-width="100px">
        <el-form-item label="实体名称"><el-input v-model="entityForm.entityName" /></el-form-item>
        <el-form-item label="实体编码"><el-input v-model="entityForm.entityCode" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="entityForm.entityType" style="width:100%;"><el-option label="表" value="表" /><el-option label="集合" value="集合" /><el-option label="文件" value="文件" /><el-option label="消息" value="消息" /></el-select></el-form-item>
        <el-form-item label="存储类型"><el-select v-model="entityForm.dbType" style="width:100%;"><el-option label="MySQL" value="MySQL" /><el-option label="Oracle" value="Oracle" /><el-option label="MongoDB" value="MongoDB" /><el-option label="Redis" value="Redis" /><el-option label="Kafka" value="Kafka" /></el-select></el-form-item>
        <el-form-item label="库名"><el-input v-model="entityForm.dbName" /></el-form-item>
        <el-form-item label="表名"><el-input v-model="entityForm.tableName" /></el-form-item>
        <el-form-item label="敏感等级"><el-select v-model="entityForm.sensitivity" style="width:100%;"><el-option label="公开" value="PUBLIC" /><el-option label="内部" value="INTERNAL" /><el-option label="机密" value="CONFIDENTIAL" /><el-option label="高度机密" value="HIGHLY_CONFIDENTIAL" /></el-select></el-form-item>
        <el-form-item label="描述"><el-input v-model="entityForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dataEntityApi } from '@/api'

const searchKeyword = ref('')
const filterDbType = ref('')
const loading = ref(false)
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const dialogVisible = ref(false)
const dialogMode = ref('add')
const entityForm = reactive({ id: null, entityName: '', entityCode: '', entityType: '表', dbType: 'MySQL', dbName: '', tableName: '', sensitivity: 'INTERNAL', description: '', appId: null })

const entityList = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await dataEntityApi.list({ page: pagination.page, pageSize: pagination.pageSize, keyword: searchKeyword.value })
    entityList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const handleAdd = () => { dialogMode.value = 'add'; Object.assign(entityForm, { id: null, entityName: '', entityCode: '', entityType: '表', dbType: 'MySQL', dbName: '', tableName: '', sensitivity: 'INTERNAL', description: '', appId: null }); dialogVisible.value = true }
const handleEdit = (row) => { dialogMode.value = 'edit'; Object.assign(entityForm, row); dialogVisible.value = true }
const handleSubmit = async () => { try { if (dialogMode.value === 'add') await dataEntityApi.create(entityForm); else await dataEntityApi.update(entityForm.id, entityForm); ElMessage.success('操作成功'); dialogVisible.value = false; loadData() } catch { ElMessage.error('操作失败') } }
const handleDelete = async (row) => { try { await ElMessageBox.confirm(`确定删除 ${row.entityName}？`, '确认', { type: 'warning' }); await dataEntityApi.delete(row.id); ElMessage.success('删除成功'); loadData() } catch {} }

const getSensitivityText = (s) => ({ HIGHLY_CONFIDENTIAL: '高度机密', CONFIDENTIAL: '机密', INTERNAL: '内部', PUBLIC: '公开' }[s] || s)
const formatNumber = (n) => n >= 1000000 ? (n / 1000000).toFixed(1) + 'M' : n >= 1000 ? (n / 1000).toFixed(1) + 'K' : n

onMounted(() => { loadData() })
</script>
