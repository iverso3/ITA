<template>
  <div class="flow-role animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">流程角色管理</h1>
      <p class="page-header__desc">流程角色信息维护</p>
    </div>
    <div class="page-card">
      <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
        <div style="display: flex; gap: 12px;">
          <el-select v-model="searchForm.flowRoleName" placeholder="选择角色名称" style="width: 200px;" clearable>
            <el-option v-for="item in flowRoleOptions" :key="item.flowRoleId" :label="item.flowRoleName" :value="item.flowRoleName" />
          </el-select>
          <el-input v-model="searchForm.flowRoleDesc" placeholder="角色描述" style="width: 200px;" clearable />
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 查询</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon> 重置</el-button>
        </div>
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="flowRoleId" label="角色编号" width="100" align="center" />
        <el-table-column prop="flowRoleName" label="角色名称" min-width="160" />
        <el-table-column prop="flowRoleDesc" label="角色描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="bindInstitutionFlag" label="绑定机构" width="100" align="center">
          <template #default="{ row }">{{ row.bindInstitutionFlag === '1' ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="bindBranchFlag" label="绑定部门" width="100" align="center">
          <template #default="{ row }">{{ row.bindBranchFlag === '1' ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="bindTeamFlag" label="绑定团队" width="100" align="center">
          <template #default="{ row }">{{ row.bindTeamFlag === '1' ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="bindAppFlag" label="绑定应用" width="100" align="center">
          <template #default="{ row }">{{ row.bindAppFlag === '1' ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="checkBranchFlag" label="部门一致性校验" width="120" align="center">
          <template #default="{ row }">{{ row.checkBranchFlag === '1' ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <span class="total-count">共 {{ pagination.total }} 条</span>
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增流程角色' : '编辑流程角色'" width="600px" destroy-on-close>
      <el-form :model="form" label-width="120px">
        <el-form-item label="角色名称">
          <el-input v-model="form.flowRoleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色描述">
          <el-input v-model="form.flowRoleDesc" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.flowRowSeq" :min="0" />
        </el-form-item>
        <el-form-item label="绑定机构">
          <el-radio-group v-model="form.bindInstitutionFlag">
            <el-radio label="1">是</el-radio>
            <el-radio label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="绑定部门">
          <el-radio-group v-model="form.bindBranchFlag">
            <el-radio label="1">是</el-radio>
            <el-radio label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="绑定团队">
          <el-radio-group v-model="form.bindTeamFlag">
            <el-radio label="1">是</el-radio>
            <el-radio label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="绑定应用">
          <el-radio-group v-model="form.bindAppFlag">
            <el-radio label="1">是</el-radio>
            <el-radio label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="部门一致性校验">
          <el-radio-group v-model="form.checkBranchFlag">
            <el-radio label="1">是</el-radio>
            <el-radio label="0">否</el-radio>
          </el-radio-group>
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
import { flowRoleApi } from '@/api'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('add')
const tableData = ref([])
const flowRoleOptions = ref([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const searchForm = reactive({ flowRoleName: '', flowRoleDesc: '' })
const form = reactive({
  flowRoleId: '',
  flowRoleName: '',
  flowRoleDesc: '',
  flowRowSeq: 1,
  bindInstitutionFlag: '0',
  bindBranchFlag: '0',
  bindTeamFlag: '0',
  bindAppFlag: '0',
  checkBranchFlag: '0'
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await flowRoleApi.list({
      page: pagination.page,
      pageSize: pagination.pageSize,
      flowRoleName: searchForm.flowRoleName || null,
      flowRoleDesc: searchForm.flowRoleDesc || null
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const loadFlowRoleOptions = async () => {
  try {
    const res = await flowRoleApi.listAll()
    flowRoleOptions.value = res.data || []
  } catch { console.error('加载角色选项失败') }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.flowRoleName = ''
  searchForm.flowRoleDesc = ''
  pagination.page = 1
  loadData()
}

const handleAdd = () => {
  dialogMode.value = 'add'
  Object.assign(form, {
    flowRoleId: '',
    flowRoleName: '',
    flowRoleDesc: '',
    flowRowSeq: 1,
    bindInstitutionFlag: '0',
    bindBranchFlag: '0',
    bindTeamFlag: '0',
    bindAppFlag: '0',
    checkBranchFlag: '0'
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (dialogMode.value === 'add') {
      await flowRoleApi.create(form)
    } else {
      await flowRoleApi.update(form.flowRoleId, form)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
    loadFlowRoleOptions()
  } catch { ElMessage.error('操作失败') }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除角色「${row.flowRoleName}」？`, '确认', { type: 'warning' })
    await flowRoleApi.delete(row.flowRoleId)
    ElMessage.success('删除成功')
    loadData()
    loadFlowRoleOptions()
  } catch {}
}

onMounted(() => {
  loadData()
  loadFlowRoleOptions()
})
</script>
