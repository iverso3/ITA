<template>
  <div class="system-department animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">部门管理</h1>
      <p class="page-header__desc">组织架构与部门管理</p>
    </div>
    <div class="page-card">
      <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
        <div style="display: flex; gap: 12px;">
          <el-input v-model="searchKeyword" placeholder="搜索部门名称" style="width: 240px;" clearable />
        </div>
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增部门</el-button>
      </div>
      <el-table :data="filteredDeptList" v-loading="loading" stripe row-key="id" default-expand-all>
        <el-table-column prop="departmentName" label="部门名称" min-width="160" />
        <el-table-column prop="departmentCode" label="部门编码" width="140">
          <template #default="{ row }"><code style="color: var(--accent);">{{ row.departmentCode }}</code></template>
        </el-table-column>
        <el-table-column prop="leaderName" label="负责人" width="120" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="isActive" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isActive === 1 ? 'success' : 'info'">{{ row.isActive === 1 ? '启用' : '停用' }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增部门' : '编辑部门'" width="500px" destroy-on-close>
      <el-form :model="deptForm" label-width="100px">
        <el-form-item label="部门名称"><el-input v-model="deptForm.departmentName" /></el-form-item>
        <el-form-item label="部门编码"><el-input v-model="deptForm.departmentCode" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="deptForm.leaderName" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="deptForm.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="deptForm.isActive">
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sysDeptApi } from '@/api'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('add')
const searchKeyword = ref('')
const deptList = ref([])
const deptForm = reactive({ id: null, departmentName: '', departmentCode: '', leaderName: '', sortOrder: 0, isActive: 1, parentId: null })

const filteredDeptList = computed(() => {
  if (!searchKeyword.value) return deptList.value
  return deptList.value.filter(d => d.departmentName.includes(searchKeyword.value))
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await sysDeptApi.tree()
    deptList.value = buildTree(res.data || [])
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const buildTree = (list) => {
  const map = {}
  const roots = []
  list.forEach(item => { map[item.id] = { ...item, children: [] } })
  list.forEach(item => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else {
      roots.push(map[item.id])
    }
  })
  return roots
}

const handleAdd = () => { dialogMode.value = 'add'; Object.assign(deptForm, { id: null, departmentName: '', departmentCode: '', leaderName: '', sortOrder: 0, isActive: 1, parentId: null }); dialogVisible.value = true }
const handleEdit = (row) => { dialogMode.value = 'edit'; Object.assign(deptForm, row); dialogVisible.value = true }
const handleSubmit = async () => {
  try {
    if (dialogMode.value === 'add') await sysDeptApi.create(deptForm)
    else await sysDeptApi.update(deptForm.id, deptForm)
    ElMessage.success('操作成功'); dialogVisible.value = false; loadData()
  } catch { ElMessage.error('操作失败') }
}
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除 ${row.departmentName}？`, '确认', { type: 'warning' })
    await sysDeptApi.delete(row.id); ElMessage.success('删除成功'); loadData()
  } catch {}
}

onMounted(() => { loadData() })
</script>
