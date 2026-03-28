<template>
  <div class="system-menu animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">菜单管理</h1>
      <p class="page-header__desc">系统菜单与权限配置</p>
    </div>
    <div class="page-card">
      <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
        <div style="display: flex; gap: 12px;">
          <el-input v-model="searchKeyword" placeholder="搜索菜单名称" style="width: 240px;" clearable />
        </div>
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增菜单</el-button>
      </div>
      <el-table
        :data="filteredMenuList"
        v-loading="loading"
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :row-class-name="tableRowClassName"
        class="menu-tree-table"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="200">
          <template #default="{ row }">
            <span :class="['menu-name-cell', row.menuType === 'CATALOG' ? 'menu-name-cell--parent' : 'menu-name-cell--child']">
              <el-icon class="menu-name-cell__icon">
                <component :is="row.icon || (row.menuType === 'CATALOG' ? 'Folder' : 'Document')" />
              </el-icon>
              <span class="menu-name-cell__text">{{ row.menuName }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="menuCode" label="菜单编码" width="140">
          <template #default="{ row }">
            <code class="menu-code">{{ row.menuCode }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="menuType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.menuType === 'CATALOG' ? 'warning' : row.menuType === 'MENU' ? 'primary' : 'info'">
              {{ row.menuType === 'CATALOG' ? '目录' : row.menuType === 'MENU' ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="160" show-overflow-tooltip />
        <el-table-column prop="component" label="组件" min-width="160" show-overflow-tooltip />
        <el-table-column prop="isVisible" label="显示" width="80" align="center">
          <template #default="{ row }">
            <span :class="row.isVisible === 1 ? 'status-show' : 'status-hide'">
              {{ row.isVisible === 1 ? '是' : '否' }}
            </span>
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

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增菜单' : '编辑菜单'" width="550px" destroy-on-close>
      <el-form :model="menuForm" label-width="100px">
        <el-form-item label="菜单类型">
          <el-radio-group v-model="menuForm.menuType">
            <el-radio label="CATALOG">目录</el-radio>
            <el-radio label="MENU">菜单</el-radio>
            <el-radio label="BUTTON">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称"><el-input v-model="menuForm.menuName" /></el-form-item>
        <el-form-item label="菜单编码"><el-input v-model="menuForm.menuCode" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="menuForm.icon" placeholder="如: Folder, Setting" /></el-form-item>
        <el-form-item label="路由路径"><el-input v-model="menuForm.path" /></el-form-item>
        <el-form-item label="组件路径"><el-input v-model="menuForm.component" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="menuForm.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="权限标识"><el-input v-model="menuForm.permission" /></el-form-item>
        <el-form-item label="显示">
          <el-radio-group v-model="menuForm.isVisible">
            <el-radio :label="1">显示</el-radio>
            <el-radio :label="0">隐藏</el-radio>
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
import { sysMenuApi } from '@/api'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('add')
const searchKeyword = ref('')
const menuList = ref([])

const filteredMenuList = computed(() => {
  if (!searchKeyword.value) return menuList.value
  const keyword = searchKeyword.value.toLowerCase()
  return menuList.value.filter(m => m.menuName.toLowerCase().includes(keyword) || (m.menuCode && m.menuCode.toLowerCase().includes(keyword)))
})

const tableRowClassName = ({ row }) => {
  if (row.menuType === 'CATALOG') {
    return 'menu-row--parent'
  }
  return 'menu-row--child'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await sysMenuApi.tree()
    menuList.value = res.data || []
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const menuForm = reactive({ id: null, parentId: null, menuName: '', menuCode: '', menuType: 'MENU', icon: '', path: '', component: '', sortOrder: 0, isVisible: 1, permission: '' })
const handleAdd = () => { dialogMode.value = 'add'; Object.assign(menuForm, { id: null, parentId: null, menuName: '', menuCode: '', menuType: 'MENU', icon: '', path: '', component: '', sortOrder: 0, isVisible: 1, permission: '' }); dialogVisible.value = true }
const handleEdit = (row) => { dialogMode.value = 'edit'; Object.assign(menuForm, row); dialogVisible.value = true }
const handleSubmit = async () => {
  try {
    if (dialogMode.value === 'add') await sysMenuApi.create(menuForm)
    else await sysMenuApi.update(menuForm.id, menuForm)
    ElMessage.success('操作成功'); dialogVisible.value = false; loadData()
  } catch { ElMessage.error('操作失败') }
}
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除 ${row.menuName}？`, '确认', { type: 'warning' })
    await sysMenuApi.delete(row.id); ElMessage.success('删除成功'); loadData()
  } catch {}
}

onMounted(() => { loadData() })
</script>

<style scoped lang="scss">
.menu-tree-table {
  // Parent menu (CATALOG) row - with accent border
  :deep(.menu-row--parent) {
    background: var(--bg-dark-3) !important;

    > td:first-child {
      border-left: 3px solid var(--accent) !important;
    }
  }

  // Tree indentation for each level
  :deep(.el-table__indent) {
    display: inline-block;
    width: 24px;
    flex-shrink: 0;
  }

  // Expand icon
  :deep(.el-table__expand-icon) {
    color: var(--text-muted);
    font-size: 12px;
    vertical-align: middle;

    &:hover {
      color: var(--accent);
    }
  }

  // Tree placeholder
  :deep(.el-table__placeholder) {
    display: inline-block;
    width: 16px;
  }
}

.menu-name-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  vertical-align: middle;
  line-height: 1;

  &--parent {
    font-weight: 600;
    font-size: 15px;
    color: var(--text-primary);

    .menu-name-cell__icon {
      color: var(--accent);
    }
  }

  &--child {
    font-weight: 400;
    font-size: 14px;
    color: var(--text-secondary);

    .menu-name-cell__icon {
      color: var(--text-muted);
    }
  }

  &__icon {
    font-size: 16px;
  }

  &__text {
    white-space: nowrap;
  }
}

.menu-code {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--accent);
  background: var(--bg-dark-3);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}

.status-show {
  color: var(--success);
  font-size: 12px;
}

.status-hide {
  color: var(--text-muted);
  font-size: 12px;
}
</style>
