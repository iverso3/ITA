<template>
  <div class="model-designer">
    <!-- 左侧：模型列表 -->
    <div class="designer-left">
      <div class="panel-header">
        <span class="panel-title">模型列表</span>
        <el-button type="primary" size="small" @click="handleOpenCreateModel">
          <el-icon><Plus /></el-icon>
          新建
        </el-button>
      </div>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索模型..."
        prefix-icon="Search"
        clearable
        class="search-input"
      />
      <el-tree
        :data="modelTree"
        :props="{ label: 'modelName', children: 'children' }"
        node-key="id"
        default-expand-all
        @node-click="handleSelectModel"
        class="model-tree"
      >
        <template #default="{ node, data }">
          <span class="tree-node">
            <el-icon v-if="data.modelType === 'ENTITY'"><Document /></el-icon>
            <el-icon v-else-if="data.modelType === 'RELATION'"><Connection /></el-icon>
            <span>{{ data.modelName }}</span>
            <el-tag size="small" :type="getStatusType(data.status)">
              {{ data.status }}
            </el-tag>
          </span>
        </template>
      </el-tree>
    </div>

    <!-- 中间：模型配置 -->
    <div class="designer-center">
      <div v-if="!currentModel && !isEdit" class="empty-state">
        <el-empty description="请选择或创建一个模型" />
      </div>
      <div v-else class="model-config">
        <div class="config-header">
          <h3>{{ isEdit ? '编辑模型' : '模型详情' }}</h3>
          <el-button-group>
            <el-button v-if="!isEdit" @click="isEdit = true">编辑</el-button>
            <el-button v-else type="primary" @click="handleSave">保存</el-button>
            <el-button v-if="isEdit" @click="isEdit = false">取消</el-button>
            <el-button type="success" @click="handlePublish">发布</el-button>
          </el-button-group>
        </div>

        <el-tabs v-model="activeTab" class="config-tabs">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-form :model="modelForm" label-width="120px" :disabled="!isEdit">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="模型编码">
                    <el-input v-model="modelForm.modelCode" placeholder="如: app, server" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="模型名称">
                    <el-input v-model="modelForm.modelName" placeholder="如: 应用系统" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="模型类型">
                    <el-select v-model="modelForm.modelType" placeholder="选择类型">
                      <el-option label="实体模型" value="ENTITY" />
                      <el-option label="关系模型" value="RELATION" />
                      <el-option label="抽象模型" value="ABSTRACT" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="关联表名">
                    <el-input v-model="modelForm.tableName" placeholder="物理表名" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="描述">
                <el-input v-model="modelForm.description" type="textarea" :rows="3" />
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 字段配置 -->
          <el-tab-pane label="字段配置" name="fields">
            <div class="fields-toolbar">
              <el-button type="primary" size="small" :disabled="!isEdit" @click="handleAddField">
                <el-icon><Plus /></el-icon> 添加字段
              </el-button>
              <el-button size="small" :disabled="!isEdit" @click="handleBatchAdd">
                <el-icon><DocumentCopy /></el-icon> 批量添加
              </el-button>
            </div>
            <el-table :data="fieldsList" border stripe size="small">
              <el-table-column prop="fieldCode" label="字段编码" width="150" />
              <el-table-column prop="fieldName" label="字段名称" width="150" />
              <el-table-column prop="fieldType" label="类型" width="100">
                <template #default="{ row }">
                  <el-tag size="small">{{ row.fieldType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="storageMode" label="存储模式" width="100">
                <template #default="{ row }">
                  <el-tag v-if="row.storageMode === 'COLUMN'" type="success" size="small">COLUMN</el-tag>
                  <el-tag v-else-if="row.storageMode === 'JSON'" type="warning" size="small">JSON</el-tag>
                  <el-tag v-else size="small">{{ row.storageMode }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="isRequired" label="必填" width="60" align="center">
                <template #default="{ row }">
                  <el-checkbox v-model="row.isRequired" :disabled="!isEdit" />
                </template>
              </el-table-column>
              <el-table-column prop="isSensitive" label="敏感" width="60" align="center">
                <template #default="{ row }">
                  <el-checkbox v-model="row.isSensitive" :disabled="!isEdit" />
                </template>
              </el-table-column>
              <el-table-column prop="displayType" label="显示类型" width="120" />
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" :disabled="!isEdit" @click="handleEditField(row)">编辑</el-button>
                  <el-button link type="danger" size="small" :disabled="!isEdit" @click="handleDeleteField(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 分组配置 -->
          <el-tab-pane label="分组配置" name="groups">
            <div class="groups-toolbar">
              <el-button type="primary" size="small" :disabled="!isEdit" @click="handleAddGroup">
                <el-icon><Plus /></el-icon> 添加分组
              </el-button>
            </div>
            <el-table :data="groupsList" border stripe size="small">
              <el-table-column prop="groupCode" label="分组编码" width="150" />
              <el-table-column prop="groupName" label="分组名称" width="150" />
              <el-table-column prop="groupType" label="分组类型" width="120" />
              <el-table-column prop="sortOrder" label="排序" width="80" />
              <el-table-column prop="description" label="描述" />
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" :disabled="!isEdit" @click="handleEditGroup(row)">编辑</el-button>
                  <el-button link type="danger" size="small" :disabled="!isEdit" @click="handleDeleteGroup(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 关系配置 -->
          <el-tab-pane label="关系配置" name="relations">
            <div class="relations-toolbar">
              <el-button type="primary" size="small" :disabled="!isEdit" @click="handleAddRelation">
                <el-icon><Plus /></el-icon> 添加关系
              </el-button>
            </div>
            <el-table :data="relationsList" border stripe size="small">
              <el-table-column prop="relCode" label="关系编码" width="150" />
              <el-table-column prop="relName" label="关系名称" width="150" />
              <el-table-column prop="relType" label="关系类型" width="120">
                <template #default="{ row }">
                  <el-tag size="small">{{ row.relType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="sourceModelName" label="源模型" width="150" />
              <el-table-column prop="targetModelName" label="目标模型" width="150" />
              <el-table-column prop="isCascadeDelete" label="级联删除" width="100" align="center">
                <template #default="{ row }">
                  <el-checkbox v-model="row.isCascadeDelete" :disabled="!isEdit" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" :disabled="!isEdit" @click="handleEditRelation(row)">编辑</el-button>
                  <el-button link type="danger" size="small" :disabled="!isEdit" @click="handleDeleteRelation(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 右侧：属性面板 -->
    <div class="designer-right">
      <div class="panel-header">
        <span class="panel-title">属性面板</span>
      </div>
      <div v-if="!selectedField" class="empty-state">
        <el-empty description="选择字段查看属性" />
      </div>
      <div v-else class="field-props">
        <el-form label-width="100px" size="small">
          <el-form-item label="字段编码">
            <el-input v-model="selectedField.fieldCode" :disabled="!isEdit" />
          </el-form-item>
          <el-form-item label="字段名称">
            <el-input v-model="selectedField.fieldName" :disabled="!isEdit" />
          </el-form-item>
          <el-form-item label="字段类型">
            <el-select v-model="selectedField.fieldType" :disabled="!isEdit">
              <el-option label="字符串" value="STRING" />
              <el-option label="整数" value="INT" />
              <el-option label="长整数" value="BIGINT" />
              <el-option label="小数" value="DECIMAL" />
              <el-option label="布尔" value="BOOLEAN" />
              <el-option label="日期" value="DATE" />
              <el-option label="日期时间" value="DATETIME" />
              <el-option label="文本" value="TEXT" />
              <el-option label="JSON" value="JSON" />
              <el-option label="文件" value="FILE" />
              <el-option label="枚举" value="ENUM" />
            </el-select>
          </el-form-item>
          <el-form-item label="存储模式">
            <el-select v-model="selectedField.storageMode" :disabled="!isEdit">
              <el-option label="物理列" value="COLUMN" />
              <el-option label="JSON" value="JSON" />
              <el-option label="标签" value="TAG" />
            </el-select>
          </el-form-item>
          <el-form-item label="显示类型">
            <el-select v-model="selectedField.displayType" :disabled="!isEdit">
              <el-option label="输入框" value="INPUT" />
              <el-option label="下拉框" value="SELECT" />
              <el-option label="日期" value="DATE" />
              <el-option label="日期范围" value="DATERANGE" />
              <el-option label="开关" value="SWITCH" />
              <el-option label="图片" value="IMAGE" />
              <el-option label="文件" value="FILE" />
              <el-option label="富文本" value="EDITOR" />
            </el-select>
          </el-form-item>
          <el-form-item label="必填">
            <el-switch v-model="selectedField.isRequired" :disabled="!isEdit" />
          </el-form-item>
          <el-form-item label="唯一">
            <el-switch v-model="selectedField.isUnique" :disabled="!isEdit" />
          </el-form-item>
          <el-form-item label="可查询">
            <el-switch v-model="selectedField.isQueryable" :disabled="!isEdit" />
          </el-form-item>
          <el-form-item label="敏感数据">
            <el-switch v-model="selectedField.isSensitive" :disabled="!isEdit" />
          </el-form-item>
          <el-form-item v-if="selectedField.isSensitive" label="敏感类型">
            <el-select v-model="selectedField.sensitiveType" :disabled="!isEdit">
              <el-option label="手机号" value="PHONE" />
              <el-option label="身份证" value="ID_CARD" />
              <el-option label="邮箱" value="EMAIL" />
              <el-option label="银行卡" value="BANK_CARD" />
            </el-select>
          </el-form-item>
          <el-form-item label="默认值">
            <el-input v-model="selectedField.defaultValue" :disabled="!isEdit" />
          </el-form-item>
          <el-form-item label="校验规则">
            <el-input v-model="selectedField.validationRule" :disabled="!isEdit" placeholder="正则表达式" />
          </el-form-item>
          <el-form-item label="帮助文本">
            <el-input v-model="selectedField.helpText" :disabled="!isEdit" />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 字段编辑对话框 -->
    <el-dialog v-model="fieldDialogVisible" :title="fieldDialogTitle" width="600px">
      <el-form :model="fieldForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="字段编码" required>
              <el-input v-model="fieldForm.fieldCode" :disabled="isFieldEditing" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="字段名称" required>
              <el-input v-model="fieldForm.fieldName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="字段类型" required>
              <el-select v-model="fieldForm.fieldType">
                <el-option label="字符串" value="STRING" />
                <el-option label="整数" value="INT" />
                <el-option label="长整数" value="BIGINT" />
                <el-option label="小数" value="DECIMAL" />
                <el-option label="布尔" value="BOOLEAN" />
                <el-option label="日期" value="DATE" />
                <el-option label="日期时间" value="DATETIME" />
                <el-option label="文本" value="TEXT" />
                <el-option label="JSON" value="JSON" />
                <el-option label="文件" value="FILE" />
                <el-option label="枚举" value="ENUM" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示类型">
              <el-select v-model="fieldForm.displayType">
                <el-option label="输入框" value="INPUT" />
                <el-option label="下拉框" value="SELECT" />
                <el-option label="日期" value="DATE" />
                <el-option label="开关" value="SWITCH" />
                <el-option label="文本域" value="TEXTAREA" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="存储模式">
              <el-select v-model="fieldForm.storageMode">
                <el-option label="物理列" value="COLUMN" />
                <el-option label="JSON" value="JSON" />
                <el-option label="标签" value="TAG" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属分组">
              <el-select v-model="fieldForm.groupId" clearable>
                <el-option v-for="g in groupsList" :key="g.id" :label="g.groupName" :value="g.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="必填">
              <el-switch v-model="fieldForm.isRequired" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="唯一">
              <el-switch v-model="fieldForm.isUnique" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="可查询">
              <el-switch v-model="fieldForm.isQueryable" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="敏感数据">
              <el-switch v-model="fieldForm.isSensitive" />
            </el-form-item>
          </el-col>
          <el-col :span="16" v-if="fieldForm.isSensitive">
            <el-form-item label="敏感类型">
              <el-select v-model="fieldForm.sensitiveType">
                <el-option label="手机号" value="PHONE" />
                <el-option label="身份证" value="ID_CARD" />
                <el-option label="邮箱" value="EMAIL" />
                <el-option label="银行卡" value="BANK_CARD" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="默认值">
          <el-input v-model="fieldForm.defaultValue" />
        </el-form-item>
        <el-form-item label="校验规则">
          <el-input v-model="fieldForm.validationRule" placeholder="正则表达式" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="fieldForm.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fieldDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveField">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分组编辑对话框 -->
    <el-dialog v-model="groupDialogVisible" :title="groupDialogTitle" width="500px">
      <el-form :model="groupForm" label-width="100px">
        <el-form-item label="分组编码" required>
          <el-input v-model="groupForm.groupCode" :disabled="isGroupEditing" />
        </el-form-item>
        <el-form-item label="分组名称" required>
          <el-input v-model="groupForm.groupName" />
        </el-form-item>
        <el-form-item label="分组类型">
          <el-select v-model="groupForm.groupType">
            <el-option label="基本信息" value="BASIC" />
            <el-option label="安全信息" value="SECURITY" />
            <el-option label="运维信息" value="OPS" />
            <el-option label="描述" value="DESCRIPTION" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="groupForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="groupForm.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="groupDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveGroup">确定</el-button>
      </template>
    </el-dialog>

    <!-- 模型新建/编辑对话框 -->
    <el-dialog v-model="modelDialogVisible" :title="modelDialogTitle" width="600px" destroy-on-close>
      <el-form :model="modelForm" label-width="120px">
        <el-form-item label="模型编码" required>
          <el-input v-model="modelForm.modelCode" placeholder="如: app, server" />
        </el-form-item>
        <el-form-item label="模型名称" required>
          <el-input v-model="modelForm.modelName" placeholder="如: 应用系统" />
        </el-form-item>
        <el-form-item label="模型类型" required>
          <el-select v-model="modelForm.modelType" placeholder="选择类型">
            <el-option label="实体模型" value="ENTITY" />
            <el-option label="关系模型" value="RELATION" />
            <el-option label="抽象模型" value="ABSTRACT" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联表名">
          <el-input v-model="modelForm.tableName" placeholder="物理表名（可选）" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="modelForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="modelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveModel">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Document, Connection, DocumentCopy } from '@element-plus/icons-vue'

// API
import {
  metaModelApi,
  metaFieldApi,
  metaGroupApi,
  metaRelationshipApi,
  metaEntityApi,
  metaPublishApi
} from '@/api'

// 状态
const searchKeyword = ref('')
const modelTree = ref([])
const currentModel = ref(null)
const isEdit = ref(false)
const activeTab = ref('basic')

// 模型对话框
const modelDialogVisible = ref(false)
const modelDialogTitle = ref('新建模型')

// 模型表单
const modelForm = reactive({
  modelCode: '',
  modelName: '',
  modelType: 'ENTITY',
  tableName: '',
  description: ''
})

// 字段列表
const fieldsList = ref([])
const groupsList = ref([])
const relationsList = ref([])
const selectedField = ref(null)

// 字段对话框
const fieldDialogVisible = ref(false)
const fieldDialogTitle = ref('添加字段')
const isFieldEditing = ref(false)
const fieldForm = reactive({
  fieldCode: '',
  fieldName: '',
  fieldType: 'STRING',
  storageMode: 'COLUMN',
  displayType: 'INPUT',
  isRequired: false,
  isUnique: false,
  isQueryable: true,
  isSensitive: false,
  sensitiveType: '',
  defaultValue: '',
  validationRule: '',
  groupId: null,
  description: ''
})

// 分组对话框
const groupDialogVisible = ref(false)
const groupDialogTitle = ref('添加分组')
const isGroupEditing = ref(false)
const groupForm = reactive({
  groupCode: '',
  groupName: '',
  groupType: 'CUSTOM',
  sortOrder: 0,
  description: ''
})

// 生命周期
onMounted(() => {
  loadModelTree()
})

// 方法
const loadModelTree = async () => {
  try {
    const res = await metaModelApi.list()
    modelTree.value = res.data || []
  } catch (error) {
    console.error('加载模型列表失败:', error)
  }
}

const handleSelectModel = (data) => {
  currentModel.value = data
  isEdit.value = false
  loadModelDetail(data.id)
}

const loadModelDetail = async (id) => {
  try {
    const res = await metaModelApi.detail(id)
    const detail = res.data
    // 填充表单
    Object.assign(modelForm, {
      modelCode: detail.modelCode,
      modelName: detail.modelName,
      modelType: detail.modelType,
      tableName: detail.tableName,
      description: detail.description
    })
    fieldsList.value = detail.fields || []
    groupsList.value = detail.groups || []
    relationsList.value = detail.relationships || []
  } catch (error) {
    console.error('加载模型详情失败:', error)
  }
}

const handleOpenCreateModel = () => {
  currentModel.value = null
  modelDialogTitle.value = '新建模型'
  Object.assign(modelForm, {
    modelCode: '',
    modelName: '',
    modelType: 'ENTITY',
    tableName: '',
    description: ''
  })
  fieldsList.value = []
  groupsList.value = []
  relationsList.value = []
  modelDialogVisible.value = true
}

const handleSaveModel = async () => {
  if (!modelForm.modelCode || !modelForm.modelName) {
    ElMessage.warning('请填写模型编码和名称')
    return
  }
  try {
    const res = await metaModelApi.create(modelForm)
    currentModel.value = res.data
    isEdit.value = false
    modelDialogVisible.value = false
    ElMessage.success('创建成功')
    loadModelTree()
  } catch (error) {
    ElMessage.error('创建失败: ' + (error.message || ''))
  }
}

const handleSave = async () => {
  try {
    if (currentModel.value) {
      await metaModelApi.update(currentModel.value.id, modelForm)
      ElMessage.success('保存成功')
    }
    isEdit.value = false
    loadModelTree()
  } catch (error) {
    ElMessage.error('保存失败: ' + (error.message || ''))
  }
}

const handlePublish = async () => {
  if (!currentModel.value) {
    ElMessage.warning('请先保存模型')
    return
  }
  try {
    await metaPublishApi.submit(currentModel.value.id)
    ElMessage.success('发布成功')
    loadModelTree()
  } catch (error) {
    ElMessage.error('发布失败: ' + (error.message || ''))
  }
}

// 字段操作
const handleAddField = () => {
  isFieldEditing.value = false
  Object.assign(fieldForm, {
    fieldCode: '',
    fieldName: '',
    fieldType: 'STRING',
    storageMode: 'COLUMN',
    displayType: 'INPUT',
    isRequired: false,
    isUnique: false,
    isQueryable: true,
    isSensitive: false,
    sensitiveType: '',
    defaultValue: '',
    validationRule: '',
    groupId: null,
    description: ''
  })
  fieldDialogTitle.value = '添加字段'
  fieldDialogVisible.value = true
}

const handleEditField = (row) => {
  isFieldEditing.value = true
  Object.assign(fieldForm, row)
  fieldDialogTitle.value = '编辑字段'
  fieldDialogVisible.value = true
}

const handleSaveField = async () => {
  if (!fieldForm.fieldCode || !fieldForm.fieldName) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    const modelId = currentModel.value?.id
    if (isFieldEditing.value) {
      await metaFieldApi.update(modelId, fieldForm.id, fieldForm)
    } else {
      await metaFieldApi.create(modelId, fieldForm)
    }
    ElMessage.success('保存成功')
    fieldDialogVisible.value = false
    loadModelDetail(modelId)
  } catch (error) {
    ElMessage.error('保存失败: ' + (error.message || ''))
  }
}

const handleDeleteField = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该字段吗？', '提示', { type: 'warning' })
    const modelId = currentModel.value?.id
    await metaFieldApi.delete(modelId, row.id)
    ElMessage.success('删除成功')
    loadModelDetail(modelId)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || ''))
    }
  }
}

// 分组操作
const handleAddGroup = () => {
  isGroupEditing.value = false
  Object.assign(groupForm, {
    groupCode: '',
    groupName: '',
    groupType: 'CUSTOM',
    sortOrder: 0,
    description: ''
  })
  groupDialogTitle.value = '添加分组'
  groupDialogVisible.value = true
}

const handleEditGroup = (row) => {
  isGroupEditing.value = true
  Object.assign(groupForm, row)
  groupDialogTitle.value = '编辑分组'
  groupDialogVisible.value = true
}

const handleSaveGroup = async () => {
  if (!groupForm.groupCode || !groupForm.groupName) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    const modelId = currentModel.value?.id
    if (isGroupEditing.value) {
      await metaGroupApi.update(modelId, groupForm.id, groupForm)
    } else {
      await metaGroupApi.create(modelId, groupForm)
    }
    ElMessage.success('保存成功')
    groupDialogVisible.value = false
    loadModelDetail(modelId)
  } catch (error) {
    ElMessage.error('保存失败: ' + (error.message || ''))
  }
}

const handleDeleteGroup = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该分组吗？字段将移至默认分组。', '提示', { type: 'warning' })
    const modelId = currentModel.value?.id
    await metaGroupApi.delete(modelId, row.id)
    ElMessage.success('删除成功')
    loadModelDetail(modelId)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || ''))
    }
  }
}

// 辅助方法
const getStatusType = (status) => {
  switch (status) {
    case 'PUBLISHED': return 'success'
    case 'DRAFT': return 'info'
    case 'ARCHIVED': return 'warning'
    default: return ''
  }
}
</script>

<style scoped>
.model-designer {
  display: flex;
  height: 100%;
  gap: 8px;
}

.designer-left {
  width: 280px;
  background: #fff;
  border-radius: 4px;
  padding: 12px;
  display: flex;
  flex-direction: column;
}

.designer-center {
  flex: 1;
  background: #fff;
  border-radius: 4px;
  padding: 16px;
  overflow: auto;
}

.designer-right {
  width: 300px;
  background: #fff;
  border-radius: 4px;
  padding: 12px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.panel-title {
  font-weight: 600;
  font-size: 14px;
}

.search-input {
  margin-bottom: 12px;
}

.model-tree {
  flex: 1;
  overflow: auto;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
}

.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.config-header h3 {
  margin: 0;
}

.fields-toolbar,
.groups-toolbar,
.relations-toolbar {
  margin-bottom: 12px;
}

.field-props {
  padding: 8px;
}
</style>
