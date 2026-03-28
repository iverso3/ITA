<template>
  <div class="system-permission animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">权限管理</h1>
      <p class="page-header__desc">细粒度权限配置与管理</p>
    </div>
    <div class="page-card">
      <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
        <div style="display: flex; gap: 12px;">
          <el-input v-model="searchKeyword" placeholder="搜索权限名称" style="width: 240px;" clearable />
        </div>
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增权限</el-button>
      </div>
      <el-table :data="permissionList" v-loading="loading" stripe>
        <el-table-column prop="permissionName" label="权限名称" min-width="160" />
        <el-table-column prop="permissionCode" label="权限编码" width="160">
          <template #default="{ row }"><code style="color: var(--accent);">{{ row.permissionCode }}</code></template>
        </el-table-column>
        <el-table-column prop="permissionType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.permissionType === 'BUTTON' ? 'primary' : 'info'">{{ row.permissionType === 'BUTTON' ? '按钮' : '数据' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="permission" label="权限标识" width="140" />
        <el-table-column prop="method" label="请求方法" width="100" align="center">
          <template #default="{ row }"><el-tag size="small">{{ row.method || '-' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="isEnabled" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isEnabled === 1 ? 'success' : 'info'">{{ row.isEnabled === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增权限' : '编辑权限'" width="500px" destroy-on-close>
      <el-form :model="permForm" label-width="100px">
        <el-form-item label="权限名称"><el-input v-model="permForm.permissionName" /></el-form-item>
        <el-form-item label="权限编码"><el-input v-model="permForm.permissionCode" /></el-form-item>
        <el-form-item label="权限类型">
          <el-radio-group v-model="permForm.permissionType">
            <el-radio label="BUTTON">按钮</el-radio>
            <el-radio label="DATA">数据</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="权限标识"><el-input v-model="permForm.permission" /></el-form-item>
        <el-form-item label="请求方法">
          <el-select v-model="permForm.method" style="width: 100%;">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
            <el-option label="PATCH" value="PATCH" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="permForm.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="permForm.isEnabled">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sysPermissionApi } from '@/api'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('add')
const searchKeyword = ref('')
const permissionList = ref([])
const permForm = reactive({ id: null, permissionName: '', permissionCode: '', permissionType: 'BUTTON', permission: '', method: '', isEnabled: 1, sortOrder: 0, menuId: null })

const loadData = async () => {
  loading.value = true
  try {
    const res = await sysPermissionApi.list()
    permissionList.value = res.data || []
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const handleAdd = () => { dialogMode.value = 'add'; Object.assign(permForm, { id: null, permissionName: '', permissionCode: '', permissionType: 'BUTTON', permission: '', method: '', isEnabled: 1, sortOrder: 0, menuId: null }); dialogVisible.value = true }
const handleEdit = (row) => { dialogMode.value = 'edit'; Object.assign(permForm, row); dialogVisible.value = true }
const handleSubmit = async () => {
  try {
    if (dialogMode.value === 'add') await sysPermissionApi.create(permForm)
    else await sysPermissionApi.update(permForm.id, permForm)
    ElMessage.success('操作成功'); dialogVisible.value = false; loadData()
  } catch { ElMessage.error('操作失败') }
}
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除 ${row.permissionName}？`, '确认', { type: 'warning' })
    await sysPermissionApi.delete(row.id); ElMessage.success('删除成功'); loadData()
  } catch {}
}

onMounted(() => { loadData() })
</script>
