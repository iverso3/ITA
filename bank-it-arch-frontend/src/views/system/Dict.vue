<template>
  <div class="system-dict animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">字典管理</h1>
      <p class="page-header__desc">系统数据字典与枚举值管理</p>
    </div>
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
      <div class="page-card">
        <div class="page-card__header"><h3>字典列表</h3><el-button type="primary" size="small" @click="handleAddDict">新增字典</el-button></div>
        <el-table :data="dictList" v-loading="loading" stripe highlight-current-row @row-click="handleDictRowClick">
          <el-table-column prop="dictName" label="字典名称" min-width="140" />
          <el-table-column prop="dictCode" label="字典编码" width="140">
            <template #default="{ row }"><code style="color: var(--accent);">{{ row.dictCode }}</code></template>
          </el-table-column>
          <el-table-column prop="dictType" label="类型" width="80" align="center">
            <template #default="{ row }"><el-tag size="small" type="info">{{ row.dictType === 'SYSTEM' ? '系统' : '业务' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" text size="small" @click.stop="handleEditDict(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="page-card">
        <div class="page-card__header"><h3>字典项列表 {{ selectedDict ? `(${selectedDict.dictName})` : '' }}</h3><el-button type="primary" size="small" @click="handleAddItem" :disabled="!selectedDict">新增字典项</el-button></div>
        <el-table :data="dictItemList" v-loading="itemLoading" stripe>
          <el-table-column prop="itemName" label="名称" min-width="120" />
          <el-table-column prop="itemCode" label="编码" width="120">
            <template #default="{ row }"><code style="color: var(--accent);">{{ row.itemCode }}</code></template>
          </el-table-column>
          <el-table-column prop="itemValue" label="值" min-width="120" />
          <el-table-column prop="isDefault" label="默认" width="60" align="center">
            <template #default="{ row }">{{ row.isDefault === 1 ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="60" align="center" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="primary" text size="small" @click="handleEditItem(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 字典对话框 -->
    <el-dialog v-model="dictDialogVisible" :title="dictDialogMode === 'add' ? '新增字典' : '编辑字典'" width="450px" destroy-on-close>
      <el-form :model="dictForm" label-width="90px">
        <el-form-item label="字典名称"><el-input v-model="dictForm.dictName" /></el-form-item>
        <el-form-item label="字典编码"><el-input v-model="dictForm.dictCode" /></el-form-item>
        <el-form-item label="字典类型">
          <el-radio-group v-model="dictForm.dictType">
            <el-radio label="SYSTEM">系统</el-radio>
            <el-radio label="BUSINESS">业务</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="dictForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dictDialogVisible = false">取消</el-button><el-button type="primary" @click="handleDictSubmit">确定</el-button></template>
    </el-dialog>

    <!-- 字典项对话框 -->
    <el-dialog v-model="itemDialogVisible" :title="itemDialogMode === 'add' ? '新增字典项' : '编辑字典项'" width="450px" destroy-on-close>
      <el-form :model="itemForm" label-width="90px">
        <el-form-item label="名称"><el-input v-model="itemForm.itemName" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="itemForm.itemCode" /></el-form-item>
        <el-form-item label="值"><el-input v-model="itemForm.itemValue" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="itemForm.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="默认">
          <el-radio-group v-model="itemForm.isDefault">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="itemDialogVisible = false">取消</el-button><el-button type="primary" @click="handleItemSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { sysDictApi } from '@/api'

const loading = ref(false)
const itemLoading = ref(false)
const dictList = ref([])
const dictItemList = ref([])
const selectedDict = ref(null)
const dictDialogVisible = ref(false)
const dictDialogMode = ref('add')
const itemDialogVisible = ref(false)
const itemDialogMode = ref('add')
const dictForm = reactive({ id: null, dictName: '', dictCode: '', dictType: 'SYSTEM', description: '' })
const itemForm = reactive({ id: null, dictId: null, itemName: '', itemCode: '', itemValue: '', sortOrder: 0, isDefault: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await sysDictApi.list()
    dictList.value = res.data || []
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const loadDictItems = async (dictId) => {
  itemLoading.value = true
  try {
    const res = await sysDictItemApi.listByDict(dictId)
    dictItemList.value = res.data || []
  } catch { ElMessage.error('加载失败') } finally { itemLoading.value = false }
}

const handleDictRowClick = (row) => {
  selectedDict.value = row
  loadDictItems(row.id)
}

const handleAddDict = () => { dictDialogMode.value = 'add'; Object.assign(dictForm, { id: null, dictName: '', dictCode: '', dictType: 'SYSTEM', description: '' }); dictDialogVisible.value = true }
const handleEditDict = (row) => { dictDialogMode.value = 'edit'; Object.assign(dictForm, row); dictDialogVisible.value = true }
const handleDictSubmit = async () => {
  try {
    if (dictDialogMode.value === 'add') await sysDictApi.create(dictForm)
    else await sysDictApi.update(dictForm.id, dictForm)
    ElMessage.success('操作成功'); dictDialogVisible.value = false; loadData()
  } catch { ElMessage.error('操作失败') }
}

const handleAddItem = () => { itemDialogMode.value = 'add'; Object.assign(itemForm, { id: null, dictId: selectedDict.value.id, itemName: '', itemCode: '', itemValue: '', sortOrder: 0, isDefault: 0 }); itemDialogVisible.value = true }
const handleEditItem = (row) => { itemDialogMode.value = 'edit'; Object.assign(itemForm, row); itemDialogVisible.value = true }
const handleItemSubmit = async () => {
  try {
    if (itemDialogMode.value === 'add') await sysDictItemApi.create(itemForm)
    else await sysDictItemApi.update(itemForm.id, itemForm)
    ElMessage.success('操作成功'); itemDialogVisible.value = false; loadDictItems(selectedDict.value.id)
  } catch { ElMessage.error('操作失败') }
}

onMounted(() => { loadData() })
</script>
