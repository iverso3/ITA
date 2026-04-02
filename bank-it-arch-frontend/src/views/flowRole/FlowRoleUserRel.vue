<template>
  <div class="flow-role-user-rel animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">用户流程角色管理</h1>
      <p class="page-header__desc">用户流程角色设置</p>
    </div>
    <div class="page-card">
      <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
        <div style="display: flex; gap: 12px; flex-wrap: wrap;">
          <el-input v-model="searchForm.userName" placeholder="用户" style="width: 150px;" clearable />
          <el-input v-model="searchForm.updateUserId" placeholder="维护人" style="width: 150px;" clearable />
          <el-select v-model="searchForm.orgId" placeholder="机构" style="width: 150px;" clearable>
            <el-option v-for="item in orgOptions" :key="item.id" :label="item.orgName" :value="item.id" />
          </el-select>
          <el-select v-model="searchForm.branchId" placeholder="部门" style="width: 150px;" clearable>
            <el-option v-for="item in deptOptions" :key="item.id" :label="item.departmentName" :value="item.id" />
          </el-select>
          <el-select v-model="searchForm.teamId" placeholder="团队" style="width: 150px;" clearable>
            <el-option v-for="item in teamOptions" :key="item.id" :label="item.teamName" :value="item.id" />
          </el-select>
          <el-select v-model="searchForm.flowRoleId" placeholder="角色" style="width: 180px;" clearable>
            <el-option v-for="item in flowRoleOptions" :key="item.flowRoleId" :label="item.flowRoleName" :value="item.flowRoleId" />
          </el-select>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 查询</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon> 重置</el-button>
        </div>
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="orgName" label="机构" min-width="120" />
        <el-table-column prop="branchName" label="部门" min-width="120" />
        <el-table-column prop="teamName" label="团队" min-width="120" />
        <el-table-column prop="flowRoleName" label="角色" min-width="160" />
        <el-table-column prop="userName" label="用户" min-width="120" />
        <el-table-column prop="flowRoleType" label="角色类型" width="100" align="center">
          <template #default="{ row }">{{ row.flowRoleType === '0' ? 'A角' : 'B角' }}</template>
        </el-table-column>
        <el-table-column prop="updateUserId" label="维护人" min-width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleView(row)">查看</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="机构">
          <el-select v-model="form.orgId" placeholder="请选择机构" style="width: 100%;" clearable>
            <el-option v-for="item in orgOptions" :key="item.id" :label="item.orgName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="form.branchId" placeholder="请选择部门" style="width: 100%;" clearable>
            <el-option v-for="item in deptOptions" :key="item.id" :label="item.departmentName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="团队">
          <el-select v-model="form.teamId" placeholder="请选择团队" style="width: 100%;" clearable>
            <el-option v-for="item in teamOptions" :key="item.id" :label="item.teamName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.flowRoleId" placeholder="请选择角色" style="width: 100%;">
            <el-option v-for="item in flowRoleOptions" :key="item.flowRoleId" :label="item.flowRoleName" :value="item.flowRoleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户">
          <el-select v-model="form.userId" placeholder="请选择用户" style="width: 100%;" filterable clearable>
            <el-option v-for="item in userOptions" :key="item.id" :label="`${item.id}-${item.username}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色类型">
          <el-radio-group v-model="form.flowRoleType">
            <el-radio label="0">A角</el-radio>
            <el-radio label="1">B角</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="dialogMode !== 'view'" type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { flowRoleUserRelApi, flowRoleApi, sysDeptApi, sysTeamApi, sysUserApi } from '@/api'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('add')
const tableData = ref([])
const flowRoleOptions = ref([])
const deptOptions = ref([])
const teamOptions = ref([])
const orgOptions = ref([])
const userOptions = ref([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const searchForm = reactive({ userName: '', updateUserId: '', orgId: '', branchId: '', teamId: '', flowRoleId: '' })
const form = reactive({
  relId: '',
  orgId: '',
  orgName: '',
  branchId: '',
  branchName: '',
  teamId: '',
  teamName: '',
  flowRoleId: '',
  flowRoleName: '',
  userId: '',
  userName: '',
  flowRoleType: '0'
})

const dialogTitle = computed(() => {
  const titles = { add: '新增用户流程角色', edit: '编辑用户流程角色', view: '查看用户流程角色' }
  return titles[dialogMode.value] || ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await flowRoleUserRelApi.list({
      page: pagination.page,
      pageSize: pagination.pageSize,
      userName: searchForm.userName || null,
      flowRoleName: null,
      branchId: searchForm.branchId || null,
      teamId: searchForm.teamId || null,
      orgId: searchForm.orgId || null,
      updateUserId: searchForm.updateUserId || null
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

const loadDeptOptions = async () => {
  try {
    const res = await sysDeptApi.list()
    deptOptions.value = res.data || []
  } catch { console.error('加载部门选项失败') }
}

const loadTeamOptions = async () => {
  try {
    const res = await sysTeamApi.list({ page: 1, pageSize: 500 })
    teamOptions.value = res.data.records || []
  } catch { console.error('加载团队选项失败') }
}

const loadUserOptions = async () => {
  try {
    const res = await sysUserApi.listAll()
    userOptions.value = (res.data || []).map(u => ({
      id: u.id,
      username: u.realName || u.username
    }))
  } catch { console.error('加载用户选项失败') }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.userName = ''
  searchForm.updateUserId = ''
  searchForm.orgId = ''
  searchForm.branchId = ''
  searchForm.teamId = ''
  searchForm.flowRoleId = ''
  pagination.page = 1
  loadData()
}

const handleAdd = () => {
  dialogMode.value = 'add'
  Object.assign(form, {
    relId: '',
    orgId: '',
    orgName: '',
    branchId: '',
    branchName: '',
    teamId: '',
    teamName: '',
    flowRoleId: '',
    flowRoleName: '',
    userId: '',
    userName: '',
    flowRoleType: '0'
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  dialogMode.value = 'view'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    const selectedRole = flowRoleOptions.value.find(r => r.flowRoleId === form.flowRoleId)
    const selectedDept = deptOptions.value.find(d => d.id === form.branchId)
    const selectedTeam = teamOptions.value.find(t => t.id === form.teamId)
    const selectedUser = userOptions.value.find(u => u.id === form.userId)
    form.flowRoleName = selectedRole ? selectedRole.flowRoleName : ''
    form.branchName = selectedDept ? selectedDept.departmentName : ''
    form.teamName = selectedTeam ? selectedTeam.teamName : ''
    form.userName = selectedUser ? selectedUser.username : ''

    if (dialogMode.value === 'add') {
      // 新增前先校验用户-角色组合是否已存在
      if (form.userId && form.flowRoleId) {
        const checkRes = await flowRoleUserRelApi.check(form.userId, form.flowRoleId)
        if (checkRes.data === true) {
          ElMessage.warning('该用户已分配此角色，请勿重复添加')
          return
        }
      }
      await flowRoleUserRelApi.create(form)
    } else {
      await flowRoleUserRelApi.update(form.relId, form)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch { ElMessage.error('操作失败') }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除该用户流程角色关系？`, '确认', { type: 'warning' })
    await flowRoleUserRelApi.delete(row.relId)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

onMounted(() => {
  loadData()
  loadFlowRoleOptions()
  loadDeptOptions()
  loadTeamOptions()
  loadUserOptions()
})
</script>
