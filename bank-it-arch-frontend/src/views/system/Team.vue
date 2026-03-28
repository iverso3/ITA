<template>
  <div class="system-team animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">团队管理</h1>
      <p class="page-header__desc">团队管理与归属</p>
    </div>
    <div class="page-card">
      <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
        <div style="display: flex; gap: 12px;">
          <el-input v-model="searchKeyword" placeholder="搜索团队名称" style="width: 240px;" clearable />
        </div>
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增团队</el-button>
      </div>
      <el-table :data="teamList" v-loading="loading" stripe>
        <el-table-column prop="teamName" label="团队名称" min-width="160" />
        <el-table-column prop="teamCode" label="团队编码" width="140">
          <template #default="{ row }"><code style="color: var(--accent);">{{ row.teamCode }}</code></template>
        </el-table-column>
        <el-table-column prop="departmentName" label="所属部门" width="140" />
        <el-table-column prop="leaderName" label="团队负责人" width="120" />
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

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增团队' : '编辑团队'" width="500px" destroy-on-close>
      <el-form :model="teamForm" label-width="100px">
        <el-form-item label="团队名称"><el-input v-model="teamForm.teamName" /></el-form-item>
        <el-form-item label="团队编码"><el-input v-model="teamForm.teamCode" /></el-form-item>
        <el-form-item label="所属部门">
          <el-select v-model="teamForm.departmentId" style="width: 100%;">
            <el-option label="信息科技部" :value="1" />
            <el-option label="运维部" :value="2" />
            <el-option label="数据中心" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="团队负责人"><el-input v-model="teamForm.leaderName" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="teamForm.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="teamForm.isActive">
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
import { sysTeamApi } from '@/api'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('add')
const searchKeyword = ref('')
const teamList = ref([])
const pagination = reactive({ page: 1, pageSize: 100, total: 0 })
const teamForm = reactive({ id: null, teamName: '', teamCode: '', departmentId: null, leaderName: '', sortOrder: 0, isActive: 1 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await sysTeamApi.list({ page: 1, pageSize: 100 })
    teamList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const handleAdd = () => { dialogMode.value = 'add'; Object.assign(teamForm, { id: null, teamName: '', teamCode: '', departmentId: null, leaderName: '', sortOrder: 0, isActive: 1 }); dialogVisible.value = true }
const handleEdit = (row) => { dialogMode.value = 'edit'; Object.assign(teamForm, row); dialogVisible.value = true }
const handleSubmit = async () => {
  try {
    if (dialogMode.value === 'add') await sysTeamApi.create(teamForm)
    else await sysTeamApi.update(teamForm.id, teamForm)
    ElMessage.success('操作成功'); dialogVisible.value = false; loadData()
  } catch { ElMessage.error('操作失败') }
}
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除 ${row.teamName}？`, '确认', { type: 'warning' })
    await sysTeamApi.delete(row.id); ElMessage.success('删除成功'); loadData()
  } catch {}
}

onMounted(() => { loadData() })
</script>
