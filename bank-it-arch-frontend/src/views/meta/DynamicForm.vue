<template>
  <div class="dynamic-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-width="labelWidth"
      :disabled="disabled"
    >
      <el-tabs v-if="showTabs" v-model="activeGroup">
        <el-tab-pane
          v-for="group in formConfig.groups"
          :key="group.groupCode"
          :label="group.groupName"
          :name="group.groupCode"
        >
          <div class="form-group">
            <el-row :gutter="gutter">
              <el-col
                v-for="field in group.fields"
                :key="field.fieldCode"
                :span="getFieldSpan(field)"
              >
                <el-form-item
                  :label="field.fieldName"
                  :prop="field.fieldCode"
                  :required="field.isRequired"
                >
                  <!-- 输入框 -->
                  <el-input
                    v-if="field.displayType === 'INPUT' || !field.displayType"
                    v-model="formData[field.fieldCode]"
                    :placeholder="field.placeholder || `请输入${field.fieldName}`"
                    :disabled="field.readonly"
                  />

                  <!-- 文本域 -->
                  <el-input
                    v-else-if="field.displayType === 'TEXTAREA'"
                    v-model="formData[field.fieldCode]"
                    type="textarea"
                    :rows="3"
                    :placeholder="field.placeholder"
                    :disabled="field.readonly"
                  />

                  <!-- 下拉框 -->
                  <el-select
                    v-else-if="field.displayType === 'SELECT'"
                    v-model="formData[field.fieldCode]"
                    :placeholder="field.placeholder || `请选择${field.fieldName}`"
                    :disabled="field.readonly"
                    clearable
                  >
                    <el-option
                      v-for="opt in field.options"
                      :key="opt.value"
                      :label="opt.label"
                      :value="opt.value"
                      :disabled="opt.disabled"
                    />
                  </el-select>

                  <!-- 日期 -->
                  <el-date-picker
                    v-else-if="field.displayType === 'DATE'"
                    v-model="formData[field.fieldCode]"
                    type="date"
                    :placeholder="field.placeholder"
                    :disabled="field.readonly"
                    value-format="YYYY-MM-DD"
                  />

                  <!-- 日期时间 -->
                  <el-date-picker
                    v-else-if="field.displayType === 'DATETIME'"
                    v-model="formData[field.fieldCode]"
                    type="datetime"
                    :placeholder="field.placeholder"
                    :disabled="field.readonly"
                    value-format="YYYY-MM-DD HH:mm:ss"
                  />

                  <!-- 日期范围 -->
                  <el-date-picker
                    v-else-if="field.displayType === 'DATERANGE'"
                    v-model="formData[field.fieldCode]"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    :disabled="field.readonly"
                    value-format="YYYY-MM-DD"
                  />

                  <!-- 开关 -->
                  <el-switch
                    v-else-if="field.displayType === 'SWITCH'"
                    v-model="formData[field.fieldCode]"
                    :disabled="field.readonly"
                  />

                  <!-- 单选框 -->
                  <el-radio-group
                    v-else-if="field.displayType === 'RADIO'"
                    v-model="formData[field.fieldCode]"
                    :disabled="field.readonly"
                  >
                    <el-radio
                      v-for="opt in field.options"
                      :key="opt.value"
                      :label="opt.value"
                    >
                      {{ opt.label }}
                    </el-radio>
                  </el-radio-group>

                  <!-- 多选框 -->
                  <el-checkbox-group
                    v-else-if="field.displayType === 'CHECKBOX'"
                    v-model="formData[field.fieldCode]"
                    :disabled="field.readonly"
                  >
                    <el-checkbox
                      v-for="opt in field.options"
                      :key="opt.value"
                      :label="opt.value"
                    >
                      {{ opt.label }}
                    </el-checkbox>
                  </el-checkbox-group>

                  <!-- 数字输入 -->
                  <el-input-number
                    v-else-if="field.displayType === 'NUMBER'"
                    v-model="formData[field.fieldCode]"
                    :min="0"
                    :max="999999999"
                    :disabled="field.readonly"
                  />

                  <!-- 默认输入框 -->
                  <el-input
                    v-else
                    v-model="formData[field.fieldCode]"
                    :placeholder="field.placeholder"
                    :disabled="field.readonly"
                  />

                  <!-- 帮助文本 -->
                  <div v-if="field.helpText" class="help-text">
                    {{ field.helpText }}
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 不使用Tabs的表单 -->
      <el-row v-else :gutter="gutter">
        <el-col
          v-for="field in allFields"
          :key="field.fieldCode"
          :span="getFieldSpan(field)"
        >
          <el-form-item
            :label="field.fieldName"
            :prop="field.fieldCode"
            :required="field.isRequired"
          >
            <component
              :is="getComponent(field)"
              v-model="formData[field.fieldCode]"
              :field="field"
              :disabled="field.readonly"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 插槽：自定义操作按钮 -->
      <slot name="footer">
        <el-form-item v-if="showButtons">
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            {{ submitText }}
          </el-button>
          <el-button @click="handleReset">{{ resetText }}</el-button>
        </el-form-item>
      </slot>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const props = defineProps({
  modelCode: { type: String, required: true },
  formData: { type: Object, default: () => ({}) },
  labelWidth: { type: String, default: '120px' },
  gutter: { type: Number, default: 20 },
  showTabs: { type: Boolean, default: true },
  showButtons: { type: Boolean, default: true },
  submitText: { type: String, default: '提交' },
  resetText: { type: String, default: '重置' },
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['submit', 'reset', 'update:formData'])

const formRef = ref(null)
const loading = ref(false)
const formConfig = ref({ groups: [] })
const activeGroup = ref('')
const formRules = ref({})

onMounted(() => {
  loadFormConfig()
})

const loadFormConfig = async () => {
  try {
    const res = await fetch(`/api/v1/meta/dynamic/form-config/${props.modelCode}`)
    formConfig.value = await res.json()
    if (formConfig.value.groups?.length > 0) {
      activeGroup.value = formConfig.value.groups[0].groupCode
    }
    initFormRules()
  } catch (error) {
    console.error('加载表单配置失败:', error)
  }
}

const allFields = computed(() => {
  if (!formConfig.value.groups) return []
  return formConfig.value.groups.flatMap(g => g.fields || [])
})

const getFieldSpan = (field) => {
  // 根据字段类型决定跨列数
  if (field.displayType === 'TEXTAREA' || field.fieldType === 'TEXT') {
    return 24
  }
  return 12
}

const getComponent = (field) => {
  // 根据displayType返回对应组件
  const componentMap = {
    INPUT: 'el-input',
    TEXTAREA: 'el-input',
    SELECT: 'el-select',
    DATE: 'el-date-picker',
    DATETIME: 'el-date-picker',
    DATERANGE: 'el-date-picker',
    SWITCH: 'el-switch',
    RADIO: 'el-radio-group',
    CHECKBOX: 'el-checkbox-group',
    NUMBER: 'el-input-number'
  }
  return componentMap[field.displayType] || 'el-input'
}

const initFormRules = () => {
  const rules = {}
  allFields.value.forEach(field => {
    const fieldRules = []
    if (field.isRequired) {
      fieldRules.push({
        required: true,
        message: `${field.fieldName}不能为空`,
        trigger: ['blur', 'change']
      })
    }
    if (field.validationRule) {
      fieldRules.push({
        pattern: new RegExp(field.validationRule),
        message: `${field.fieldName}格式不正确`,
        trigger: ['blur', 'change']
      })
    }
    if (fieldRules.length > 0) {
      rules[field.fieldCode] = fieldRules
    }
  })
  formRules.value = rules
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    emit('submit', props.formData)
  } catch (error) {
    console.error('表单校验失败:', error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formRef.value?.resetFields()
  emit('reset')
}

const validate = () => formRef.value?.validate()
const scrollToField = (prop) => formRef.value?.scrollToField(prop)

defineExpose({ validate, scrollToField, formRef })
</script>

<style scoped>
.dynamic-form {
  padding: 16px;
}

.form-group {
  padding: 8px 0;
}

.help-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
