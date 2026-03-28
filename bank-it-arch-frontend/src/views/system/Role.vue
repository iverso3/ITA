<template>
  <div class="system-role animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">角色管理</h1>
      <p class="page-header__desc">系统角色与权限配置</p>
    </div>
    <div class="page-card">
      <div style="margin-bottom: 20px;">
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增角色</el-button>
      </div>
      <el-table :data="roleList" v-loading="loading" stripe>
        <el-table-column prop="roleName" label="角色名称" width="160">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-icon :color="row.roleType === 'SYSTEM' ? 'var(--warning)' : 'var(--info)'"><Postcard /></el-icon>
              <span style="color: var(--text-primary); font-weight: 500;">{{ row.roleName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="roleCode" label="角色编码" width="200"><template #default="{ row }"><code style="color: var(--accent);">{{ row.roleCode }}</code></template></el-table-column>
        <el-table-column prop="roleType" label="类型" width="100" align="center"><template #default="{ row }"><el-tag size="small" :type="row.roleType === 'SYSTEM' ? 'warning' : 'info'">{{ row.roleType === 'SYSTEM' ? '系统' : '业务' }}</el-tag></template></el-table-column>
        <el-table-column prop="dataScope" label="数据范围" width="140"><template #default="{ row }">{{ getDataScopeText(row.dataScope) }}</template></el-table-column>
        <el-table-column prop="userCount" label="用户数" width="80" align="center" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="isActive" label="状态" width="80" align="center">
          <template #default="{ row }"><span class="status-badge" :class="'status-badge--' + (row.isActive ? 'online' : 'offline')">{{ row.isActive ? '启用' : '停用' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" text size="small" @click="handlePermission(row)">菜单权限</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增角色' : '编辑角色'" width="500px" destroy-on-close>
      <el-form :model="roleForm" label-width="100px">
        <el-form-item label="角色名称"><el-input v-model="roleForm.roleName" /></el-form-item>
        <el-form-item label="角色编码"><el-input v-model="roleForm.roleCode" /></el-form-item>
        <el-form-item label="角色类型"><el-select v-model="roleForm.roleType" style="width:100%;"><el-option label="系统角色" value="SYSTEM" /><el-option label="业务角色" value="BUSINESS" /></el-select></el-form-item>
        <el-form-item label="数据范围"><el-select v-model="roleForm.dataScope" style="width:100%;"><el-option label="全部数据" value="ALL" /><el-option label="本部门及子部门" value="DEPT_CHILDREN" /><el-option label="本部门" value="DEPT" /><el-option label="仅本人" value="SELF" /></el-select></el-form-item>
        <el-form-item label="描述"><el-input v-model="roleForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="permissionDialogVisible" title="菜单权限配置" width="600px" destroy-on-close>
      <div style="margin-bottom: 16px; color: var(--text-secondary); font-size: 13px;">
        勾选菜单或按钮权限。注意：授权按钮权限前必须先拥有其父菜单权限。
      </div>
      <el-tree
        ref="menuTreeRef"
        :data="menuTreeData"
        :props="{ label: 'menuName', children: 'children', disabled: () => false }"
        node-key="id"
        :default-expand-all="true"
        show-checkbox
        check-strictly
        :expand-on-click-node="false"
      >
        <template #default="{ node, data }">
          <span style="display: flex; align-items: center; gap: 8px;">
            <el-icon v-if="data.menuType === 'CATALOG'"><FolderOpened /></el-icon>
            <el-icon v-else-if="data.menuType === 'MENU'"><Document /></el-icon>
            <el-icon v-else><Grid /></el-icon>
            <span>{{ data.menuName }}</span>
            <el-tag v-if="data.menuType === 'BUTTON'" size="small" type="info">按钮</el-tag>
            <el-tag v-if="data.menuType === 'CATALOG'" size="small" type="warning">目录</el-tag>
          </span>
        </template>
      </el-tree>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permissionLoading" @click="handlePermissionSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sysRoleApi } from '@/api'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('add')
const roleList = ref([])
const roleForm = reactive({ id: null, roleName: '', roleCode: '', roleType: 'BUSINESS', dataScope: 'DEPT', description: '' })

const permissionDialogVisible = ref(false)
const permissionLoading = ref(false)
const menuTreeRef = ref(null)
const currentRoleId = ref(null)
const menuTreeData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await sysRoleApi.list()
    roleList.value = res.data || []
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const handleAdd = () => {
  dialogMode.value = 'add'
  Object.assign(roleForm, { id: null, roleName: '', roleCode: '', roleType: 'BUSINESS', dataScope: 'DEPT', description: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  Object.assign(roleForm, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (dialogMode.value === 'add') await sysRoleApi.create(roleForm)
    else await sysRoleApi.update(roleForm.id, roleForm)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch { ElMessage.error('操作失败') }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除角色 ${row.roleName}？`, '确认', { type: 'warning' })
    await sysRoleApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

const handlePermission = async (row) => {
  currentRoleId.value = row.id
  permissionDialogVisible.value = true
  permissionLoading.value = true
  try {
    const res = await sysRoleApi.getRoleMenuTree(row.id)
    menuTreeData.value = res.data.menus || []
    await nextTick()
    if (menuTreeRef.value && res.data.assignedMenuIds) {
      menuTreeRef.value.setCheckedKeys(res.data.assignedMenuIds || [])
    }
  } catch { ElMessage.error('加载菜单权限失败') } finally {
    permissionLoading.value = false
  }
}

const handlePermissionSubmit = async () => {
  if (!menuTreeRef.value) return
  const checkedKeys = menuTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
  const allCheckedKeys = [...checkedKeys, ...halfCheckedKeys]

  permissionLoading.value = true
  try {
    await sysRoleApi.assignRoleMenus(currentRoleId.value, { menuIds: allCheckedKeys })
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch { ElMessage.error('权限分配失败') } finally {
    permissionLoading.value = false
  }
}

const getDataScopeText = (s) => ({ ALL: '全部数据', DEPT: '本部门', DEPT_CHILDREN: '本部门及子部门', SELF: '仅本人' }[s] || s)

import { nextTick } from 'vue'

onMounted(() => { loadData() })
</script>
