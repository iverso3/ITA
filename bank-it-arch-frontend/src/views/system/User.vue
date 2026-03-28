<template>
  <div class="system-user animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">用户管理</h1>
      <p class="page-header__desc">系统用户管理与LDAP同步</p>
    </div>
    <div class="page-card">
      <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
        <div style="display: flex; gap: 12px;">
          <el-input v-model="searchKeyword" placeholder="搜索用户名或姓名" style="width: 240px;" clearable @keyup.enter="loadData" />
          <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px;" @change="loadData">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
          <el-button @click="loadData"><el-icon><Search /></el-icon></el-button>
        </div>
        <div style="display: flex; gap: 12px;">
          <el-button @click="handleSync"><el-icon><Refresh /></el-icon> LDAP同步</el-button>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增用户</el-button>
        </div>
      </div>
      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" width="140">
          <template #default="{ row }"><span style="color: var(--text-primary); font-weight: 500;">{{ row.username }}</span></template>
        </el-table-column>
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column prop="teamName" label="团队" width="120" />
        <el-table-column prop="userType" label="类型" width="80" align="center">
          <template #default="{ row }"><el-tag size="small" :type="row.userType === 'LDAP' ? 'warning' : 'info'">{{ row.userType === 'LDAP' ? 'LDAP' : '本地' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }"><span class="status-badge" :class="'status-badge--' + (row.status === 'ENABLED' ? 'online' : 'offline')">{{ row.status === 'ENABLED' ? '启用' : '禁用' }}</span></template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" text size="small" @click="handleToggleStatus(row)">{{ row.status === 'ENABLED' ? '禁用' : '启用' }}</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;"><el-pagination v-model:current-page="pagination.page" :total="pagination.total" layout="total, prev, pager, next" @current-change="loadData" /></div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增用户' : '编辑用户'" width="500px" destroy-on-close>
      <el-form :model="userForm" label-width="90px">
        <el-form-item label="用户名"><el-input v-model="userForm.username" :disabled="dialogMode === 'edit'" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="userForm.realName" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="userForm.email" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="userForm.phone" /></el-form-item>
        <el-form-item label="用户类型"><el-select v-model="userForm.userType" style="width:100%;"><el-option label="本地用户" value="LOCAL" /><el-option label="LDAP用户" value="LDAP" /></el-select></el-form-item>
        <el-form-item v-if="dialogMode === 'add'" label="密码"><el-input v-model="userForm.password" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sysUserApi } from '@/api'

const searchKeyword = ref('')
const filterStatus = ref('')
const loading = ref(false)
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const dialogVisible = ref(false)
const dialogMode = ref('add')
const userList = ref([])
const userForm = reactive({ id: null, username: '', realName: '', email: '', phone: '', userType: 'LOCAL', password: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await sysUserApi.list({ page: pagination.page, pageSize: pagination.pageSize, keyword: searchKeyword.value, status: filterStatus.value })
    userList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const handleAdd = () => { dialogMode.value = 'add'; Object.assign(userForm, { id: null, username: '', realName: '', email: '', phone: '', userType: 'LOCAL', password: '' }); dialogVisible.value = true }
const handleEdit = (row) => { dialogMode.value = 'edit'; Object.assign(userForm, row); dialogVisible.value = true }
const handleSubmit = async () => {
  try {
    if (dialogMode.value === 'add') await sysUserApi.create(userForm)
    else await sysUserApi.update(userForm.id, userForm)
    ElMessage.success('操作成功'); dialogVisible.value = false; loadData()
  } catch { ElMessage.error('操作失败') }
}
const handleToggleStatus = async (row) => {
  const newStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  try {
    await sysUserApi.toggleStatus(row.id, { status: newStatus })
    ElMessage.success('状态更新成功'); loadData()
  } catch { ElMessage.error('操作失败') }
}
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除用户 ${row.realName}？`, '确认', { type: 'warning' })
    await sysUserApi.delete(row.id); ElMessage.success('删除成功'); loadData()
  } catch {}
}
const handleSync = async () => {
  ElMessage.info('LDAP同步功能需要后端配置，请联系管理员')
}

onMounted(() => { loadData() })
</script>
