<template>
  <div class="oss-impl-apply-form-page">
    <div class="page-card">
      <!-- Page Header - Only show for view/edit mode -->
      <div class="page-header" v-if="isView || isEdit">
        <h2 class="page-title">{{ isView ? '查看引入申请' : '编辑引入申请' }}</h2>
      </div>

      <!-- Form -->
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px" class="apply-form">
        <!-- Section 1: 基本信息 -->
        <div class="form-section">
          <h3 class="section-title">基本信息</h3>
          <div class="form-grid">
            <el-form-item label="申请类型" prop="implApplyType" required>
              <el-select v-model="formData.implApplyType" placeholder="请选择" :disabled="isView || isEdit" class="form-input" @change="handleApplyTypeChange">
                <el-option label="首次引入" value="0" />
                <el-option label="新版本引入" value="1" />
              </el-select>
            </el-form-item>
            <el-form-item label="软件分类" prop="swCategory" required>
              <el-select v-model="formData.swCategory" placeholder="请选择" :disabled="isView || formData.implApplyType === '1'" class="form-input" @change="handleCategoryChange">
                <el-option label="开源基础软件" value="BASE" />
                <el-option label="开源工具软件" value="TOOL" />
                <el-option label="开源组件" value="CMPNT" />
              </el-select>
            </el-form-item>
            <el-form-item label="开源许可协议" prop="licAbbr" required>
              <el-select v-model="formData.licAbbr" placeholder="请选择" :disabled="isView" class="form-input">
                <el-option label="Apache-2.0" value="Apache-2.0" />
                <el-option label="MIT" value="MIT" />
                <el-option label="GPL-3.0" value="GPL-3.0" />
                <el-option label="BSD-2-Clause" value="BSD-2-Clause" />
                <el-option label="MPL-2.0" value="MPL-2.0" />
              </el-select>
            </el-form-item>
            <el-form-item label="开源软件名称" prop="swName" required>
              <el-select
                v-if="formData.implApplyType === '1'"
                v-model="formData.swId"
                placeholder="请选择"
                :disabled="isView"
                class="form-input"
                filterable
                remote
                :remote-method="searchSoftware"
                clearable
                @focus="loadSoftwareListAll"
                @change="handleSoftwareChange"
              >
                <el-option v-for="sw in softwareList" :key="sw.id" :label="sw.swName" :value="sw.id" />
              </el-select>
              <el-input v-else v-model="formData.swName" placeholder="请输入" :disabled="isView" class="form-input white-input" />
            </el-form-item>
            <el-form-item label="开源软件版本" prop="swVersion" required>
              <el-input v-model="formData.swVersion" placeholder="请输入" :disabled="isView" class="form-input white-input" />
            </el-form-item>
          </div>
        </div>

        <!-- Section 2: 评审背景及结论 -->
        <div class="form-section">
          <h3 class="section-title">评审背景及结论</h3>
          <div class="form-grid">
            <el-form-item label="评审背景" prop="evalBackground" class="full-width">
              <el-input v-model="formData.evalBackground" type="textarea" :rows="4" placeholder="请输入项目背景、业务/技术现状、待解决问题，以及同类功能软件或同款开源软件其他版本无法满足需求的原因" :disabled="isView" class="form-input" />
              <span class="char-count">{{ (formData.evalBackground || '').length }}/2000</span>
            </el-form-item>
            <el-form-item label="系统环境" prop="systemEnv" class="full-width">
              <el-input v-model="formData.systemEnv" type="textarea" :rows="3" placeholder="请输入系统环境信息" :disabled="isView" class="form-input" />
            </el-form-item>
            <el-form-item label="功能介绍" prop="functionIntro" class="full-width">
              <el-input v-model="formData.functionIntro" type="textarea" :rows="3" placeholder="请输入功能介绍" :disabled="isView" class="form-input" />
            </el-form-item>
            <el-form-item label="评审结论" prop="evalConclusion" class="full-width">
              <el-input v-model="formData.evalConclusion" type="textarea" :rows="4" placeholder="请输入评审结论" :disabled="isView" class="form-input" />
              <span class="char-count">{{ (formData.evalConclusion || '').length }}/2000</span>
            </el-form-item>
          </div>
        </div>

        <!-- Section 3: 介质信息 -->
        <div class="form-section">
          <h3 class="section-title">介质信息</h3>
          <div class="media-section">
            <el-button type="primary" @click="handleSelectMedia" class="btn-select-media" v-if="!isView">
              <el-icon><Plus /></el-icon> 选择介质信息
            </el-button>
            <el-table :data="mediaList" stripe class="media-table">
              <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
              <el-table-column prop="fileSize" label="文件大小(kb)" width="120" align="center" />
              <el-table-column prop="mediaType" label="介质类型" width="100" align="center" />
              <el-table-column label="操作" width="100" align="center" v-if="!isView">
                <template #default="{ row }">
                  <el-button type="danger" link @click="handleRemoveMedia(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- Section 4: 评测指标及结果 -->
        <div class="form-section">
          <h3 class="section-title">评测指标及结果</h3>

          <!-- Entry Indicators (准入指标) -->
          <div class="eval-block">
            <h4 class="eval-block-title">准入指标</h4>
            <el-table :data="entryIndicators" border class="eval-table">
              <el-table-column prop="level1" label="一级指标" width="100" />
              <el-table-column prop="level2" label="二级指标" width="150" />
              <el-table-column prop="criteria" label="指标说明" min-width="220" show-overflow-tooltip />
              <el-table-column prop="weight" label="分值权重" width="70" align="center" />
              <el-table-column label="评测结果" width="150" align="center">
                <template #default="{ row }">
                  <span v-if="row.inputType === 'DISPLAY_ONLY'" class="display-only">-</span>
                  <el-select v-else v-model="row.result" placeholder="请选择" :disabled="isView" class="eval-select">
                    <el-option v-for="opt in row.options" :key="opt.value" :label="opt.label" :value="opt.value" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="评测得分" width="70" align="center">
                <template #default="{ row }">
                  <span>{{ row.score || 0 }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- Quality Indicators (质量指标) -->
          <div class="eval-block">
            <h4 class="eval-block-title">质量指标</h4>
            <component
              :is="evalComponent"
              ref="evalRef"
              :category="formData.swCategory"
              :indicators="qualityIndicators"
              :readonly="isView"
              @update="handleEvalUpdate"
            />
          </div>

          <!-- Total Score -->
          <div class="total-score">
            <span class="total-label">总分：</span>
            <span class="total-value">{{ totalScore }}</span>
            <span class="total-max">/ 100</span>
          </div>
        </div>

        <!-- Attachment Section -->
        <div class="form-section">
          <h3 class="section-title">附件</h3>
          <div class="attachment-section">
            <el-button type="primary" @click="handleAddAttachment" class="btn-add-attachment" v-if="!isView">
              <el-icon><Plus /></el-icon> 添加附件
            </el-button>
            <div class="attachment-tip" v-if="!isView">
              <span class="red">注：只支持上传评测证明材料（内容包含：开源许可证、漏洞扫描结果、CVSS评分分析、病毒扫描结果），文件格式：doc、docx，单个文件不超过50M。</span>
            </div>
            <el-table :data="attachmentList" stripe class="attachment-table">
              <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
              <el-table-column prop="fileSize" label="文件大小(kb)" width="120" align="center" />
              <el-table-column label="操作" width="100" align="center" v-if="!isView">
                <template #default="{ row }">
                  <el-button type="danger" link @click="handleRemoveAttachment(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- Form Actions - Bottom Buttons -->
        <div class="form-actions" v-if="!isView || !isEdit">
          <el-button @click="handleBack" class="btn-back">返回</el-button>
          <el-button @click="handleSave" class="btn-save" v-if="!isView">保存</el-button>
          <el-button type="primary" @click="handleSubmit" class="btn-submit" v-if="!isView && !formData.procInstId">提交</el-button>
        </div>
      </el-form>
    </div>

    <!-- Media Selection Dialog -->
    <el-dialog v-model="mediaDialogVisible" title="选择介质信息" width="600px">
      <div class="media-dialog-content">
        <p>介质选择功能开发中...</p>
      </div>
      <template #footer>
        <el-button @click="mediaDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ossImplApplyApi, wfApi } from '@/api'
import { useAuthStore } from '@/store/auth'
import EvalIndicatorsBase from '@/components/oss/EvalIndicatorsBase.vue'
import EvalIndicatorsTool from '@/components/oss/EvalIndicatorsTool.vue'
import EvalIndicatorsCmpnt from '@/components/oss/EvalIndicatorsCmpnt.vue'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const evalRef = ref(null)

const isView = computed(() => route.query.mode === 'view')
const isEdit = computed(() => route.query.mode === 'edit')
const recordId = computed(() => route.query.id)

// Entry indicators (shared by all categories)
const entryIndicators = ref([
  {
    level1: '许可证准入',
    level2: '开源许可证准入情况',
    level3: '',
    criteria: '当前引入版本使用的开源许可证属于准入许可证',
    weight: '/',
    result: '',
    score: 0,
    inputType: 'DISPLAY_ONLY',
    options: []
  },
  {
    level1: '安全风险准入',
    level2: '已暴露最高安全漏洞风险等级',
    level3: '',
    criteria: '当前引入版本无高危及以上漏洞',
    weight: '/',
    result: '',
    score: 0,
    inputType: 'DROPDOWN',
    options: [
      { label: '无高危及以上漏洞', value: 'NONE', score: 0 },
      { label: '有高危及以上漏洞', value: 'EXISTS', score: 0 }
    ]
  },
  {
    level1: '安全风险准入',
    level2: '软件涉恶意代码情况',
    level3: '',
    criteria: '当前引入版本未发现病毒或木马',
    weight: '/',
    result: '',
    score: 0,
    inputType: 'DROPDOWN',
    options: [
      { label: '未发现病毒或木马', value: 'NONE', score: 0 },
      { label: '有发现病毒或木马', value: 'EXISTS', score: 0 }
    ]
  }
])

// Quality indicators based on category
const qualityIndicators = ref([])

const evalComponent = computed(() => {
  switch (formData.swCategory) {
    case 'BASE': return EvalIndicatorsBase
    case 'TOOL': return EvalIndicatorsTool
    case 'CMPNT': return EvalIndicatorsCmpnt
    default: return null
  }
})

const totalScore = computed(() => {
  let score = 0
  entryIndicators.value.forEach(ind => {
    if (ind.score !== '/' && ind.score !== undefined) {
      score += ind.score
    }
  })
  if (evalRef.value) {
    score += evalRef.value.totalScore || 0
  }
  return score
})

const formData = reactive({
  id: '',
  implApplyNo: '',
  flowTitle: '',
  implApplyType: '1', // Default to new version
  swId: '',
  swName: '',
  swVersion: '',
  swType: '',
  swCategory: '',
  licAbbr: '',
  secInstrt: '0',
  osType: '',
  useAppNo: '',
  launchVersion: '',
  launchTaskInfo: '',
  implCmnt: '',
  evalBackground: '',
  systemEnv: '',
  functionIntro: '',
  evalConclusion: '',
  procInstId: null,
  implUserId: '',
  implUserName: ''
})

const mediaList = ref([])
const attachmentList = ref([])
const softwareList = ref([])
const mediaDialogVisible = ref(false)
const submitLoading = ref(false)
const evalData = ref(null) // 评测指标数据

const formRules = {
  implApplyType: [{ required: true, message: '请选择申请类型', trigger: 'change' }],
  swCategory: [{ required: true, message: '请选择软件分类', trigger: 'change' }],
  licAbbr: [{ required: true, message: '请选择开源许可协议', trigger: 'change' }],
  swName: [{ required: true, message: '请输入软件名称', trigger: 'blur' }],
  swVersion: [{ required: true, message: '请输入版本号', trigger: 'blur' }]
}

const loadSoftwareList = async () => {
  try {
    const res = await ossImplApplyApi.softwareList({ swCategory: formData.swCategory })
    if (res.code === 200) {
      softwareList.value = res.data || []
    }
  } catch (error) {
    console.error('Failed to load software list:', error)
  }
}

const searchSoftware = async (query) => {
  if (!query) {
    // 搜索词为空时，加载所有软件列表
    loadSoftwareListAll()
    return
  }
  try {
    const res = await ossImplApplyApi.softwareList({ keyword: query })
    if (res.code === 200) {
      softwareList.value = res.data || []
    }
  } catch (error) {
    console.error('Failed to search software:', error)
  }
}

const loadSoftwareListAll = async () => {
  try {
    const res = await ossImplApplyApi.softwareList({})
    if (res.code === 200) {
      softwareList.value = res.data || []
    }
  } catch (error) {
    console.error('Failed to load software list:', error)
  }
}

const handleSwNameSelect = (swName) => {
  const sw = softwareList.value.find(s => s.swName === swName)
  if (sw) {
    formData.swCategory = sw.swCategory
  }
}

const handleApplyTypeChange = () => {
  // 重置软件名称、软件分类、软件版本
  formData.swId = ''
  formData.swName = ''
  formData.swCategory = ''
  formData.swVersion = ''
}

const handleSoftwareChange = (swId) => {
  if (!swId) {
    // 清空选择时，重置相关字段
    formData.swId = ''
    formData.swName = ''
    formData.swCategory = ''
    return
  }
  formData.swId = swId
  const sw = softwareList.value.find(s => s.id === swId)
  if (sw) {
    formData.swName = sw.swName
    formData.swCategory = sw.swCategory
  }
}

const handleCategoryChange = () => {
  if (formData.implApplyType === '1') {
    loadSoftwareList()
  }
}

const handleSelectMedia = () => {
  mediaDialogVisible.value = true
}

const handleRemoveMedia = (row) => {
  const idx = mediaList.value.indexOf(row)
  if (idx > -1) {
    mediaList.value.splice(idx, 1)
  }
}

const handleAddAttachment = () => {
  ElMessage.info('附件上传功能开发中...')
}

const handleRemoveAttachment = (row) => {
  const idx = attachmentList.value.indexOf(row)
  if (idx > -1) {
    attachmentList.value.splice(idx, 1)
  }
}

const handleEvalUpdate = (data) => {
  evalData.value = data
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    // Set current user info
    const authStore = useAuthStore()
    formData.implUserId = authStore.userInfo?.userId || ''
    formData.implUserName = authStore.userInfo?.realName || authStore.username || ''
    const res = formData.id
      ? await ossImplApplyApi.update(formData.id, formData)
      : await ossImplApplyApi.create(formData)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      if (!formData.id) {
        formData.id = res.data.id
        formData.implApplyNo = res.data.implApplyNo
      }
      // 保存评测数据到拓展表
      await saveEvalData()
    }
  } catch (error) {
    console.error('Failed to save:', error)
  }
}

const saveEvalData = async () => {
  if (!formData.implApplyNo) {
    console.warn('saveEvalData: implApplyNo is empty')
    return false
  }
  if (!evalData.value || !evalData.value.indicators) {
    console.warn('saveEvalData: evalData is empty, indicators:', evalData.value?.indicators)
    return false
  }
  try {
    console.log('saveEvalData: starting with implApplyNo:', formData.implApplyNo)
    // 先确保supl记录存在（getOrCreate）
    const suplCheck = await ossImplApplyApi.supplementary(formData.implApplyNo)
    console.log('saveEvalData: suplCheck:', suplCheck)
    // 然后更新评测数据
    const suplData = {
      implApplyNo: formData.implApplyNo,
      evalResultListJson: JSON.stringify(evalData.value.indicators || []),
      evalScoreListJson: JSON.stringify({ totalScore: evalData.value.totalScore, category: evalData.value.category })
    }
    console.log('saveEvalData: suplData:', suplData)
    const res = await ossImplApplyApi.updateSupplementary(formData.implApplyNo, suplData)
    console.log('saveEvalData: res:', res)
    return res.code === 200
  } catch (error) {
    console.error('Failed to save eval data:', error)
    return false
  }
}

const handleSubmit = async () => {
  // TODO: 检查总分是否达到80分（暂时关闭）
  // if (totalScore.value < 80) {
  //   ElMessage.warning('评测总分低于80分，不满足引入条件，请重新填写评测指标')
  //   return
  // }

  // 首次引入时，检查软件名称和版本是否已存在
  if (formData.implApplyType === '0') {
    if (!formData.swName || !formData.swVersion) {
      ElMessage.warning('请输入软件名称和版本')
      return
    }
    try {
      console.log('checkDuplicate params:', formData.swName, formData.swVersion)
      const checkRes = await ossImplApplyApi.checkDuplicate(formData.swName, formData.swVersion)
      console.log('checkDuplicate response:', checkRes)
      if (checkRes && checkRes.code === 200 && checkRes.data && checkRes.data.exists === true) {
        ElMessage.warning('该软件已存在，请检查是否应为新版本引入')
        return
      }
    } catch (error) {
      console.error('Failed to check duplicate:', error)
      ElMessage.error('重复检查失败，请稍后重试')
      return
    }
  }

  // 新版本引入时，检查软件特定版本是否已存在于版本清单表
  if (formData.implApplyType === '1') {
    if (!formData.swId || !formData.swVersion) {
      ElMessage.warning('请选择软件并输入版本号')
      return
    }
    try {
      console.log('checkVersionExists params:', formData.swId, formData.swVersion)
      const checkRes = await ossImplApplyApi.checkVersionExists(formData.swId, formData.swVersion)
      console.log('checkVersionExists response:', checkRes)
      if (checkRes && checkRes.code === 200 && checkRes.data && checkRes.data.exists === true) {
        ElMessage.warning('该软件版本已存在，请检查申请信息')
        return
      }
    } catch (error) {
      console.error('Failed to check version exists:', error)
      ElMessage.error('版本重复检查失败，请稍后重试')
      return
    }
  }

  submitLoading.value = true
  try {
    await formRef.value.validate()
    // Set current user info
    const authStore = useAuthStore()
    formData.implUserId = authStore.userInfo?.userId || ''
    formData.implUserName = authStore.userInfo?.realName || authStore.username || ''
    // First save the apply if not saved
    if (!formData.id || !formData.implApplyNo) {
      console.log('Creating apply with formData:', formData)
      const res = formData.id
        ? await ossImplApplyApi.update(formData.id, formData)
        : await ossImplApplyApi.create(formData)
      console.log('Create response:', res)
      if (res.code !== 200) {
        submitLoading.value = false
        return
      }
      console.log('Setting id:', res.data.id, 'implApplyNo:', res.data.implApplyNo)
      formData.id = res.data.id
      formData.implApplyNo = res.data.implApplyNo
    }
    // 保存评测数据
    const saved = await saveEvalData()
    if (!saved) {
      ElMessage.error('评测数据保存失败')
      submitLoading.value = false
      return
    }
    // Query workflow definition for 引入申请
    const defRes = await wfApi.definitions()
    if (defRes.code !== 200) {
      ElMessage.error('获取流程定义失败')
      submitLoading.value = false
      return
    }
    const allDefs = defRes.data.records || defRes.data || []
    // Find 引入申请 workflow (definitionCode = imply)
    const implApplyDef = allDefs.find(d => d.definitionCode === 'imply' && d.isActive === 1)
    if (!implApplyDef) {
      ElMessage.error('未找到引入申请流程定义，请先在流程管理中配置')
      submitLoading.value = false
      return
    }
    if (!implApplyDef.id) {
      ElMessage.error('流程定义ID无效')
      submitLoading.value = false
      return
    }
    // Start the process
    console.log('Start process params:', { implApplyNo: formData.implApplyNo, definitionId: implApplyDef.id })
    if (!formData.implApplyNo) {
      ElMessage.error('申请单号为空，请先保存申请')
      submitLoading.value = false
      return
    }
    const res = await ossImplApplyApi.startProcess({
      implApplyNo: formData.implApplyNo,
      definitionId: implApplyDef.id
    })
    console.log('Start process response:', res)
    if (res.code === 200) {
      ElMessage.success('提交成功，审批流程已启动')
      formData.procInstId = res.data.id
      router.push('/oss/impl-apply')
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('Failed to submit:', error)
    ElMessage.error('提交失败')
  } finally {
    submitLoading.value = false
  }
}

const handleBack = () => {
  router.push('/oss/impl-apply')
}

const loadData = async () => {
  if (!recordId.value) return

  try {
    const res = await ossImplApplyApi.detail(recordId.value)
    if (res.code === 200) {
      Object.assign(formData, res.data)
      // Load supplementary info (评测指标数据)
      if (formData.implApplyNo) {
        const suplRes = await ossImplApplyApi.supplementary(formData.implApplyNo)
        if (suplRes.code === 200 && suplRes.data) {
          const supl = suplRes.data
          // 解析评测结果列表
          if (supl.evalResultListJson && supl.evalResultListJson !== 'null') {
            try {
              const parsedIndicators = JSON.parse(supl.evalResultListJson)
              if (parsedIndicators && Array.isArray(parsedIndicators)) {
                evalData.value = {
                  indicators: parsedIndicators,
                  category: formData.swCategory
                }
                // 同时更新qualityIndicators，这样eval组件的watch能感知到数据变化
                qualityIndicators.value = parsedIndicators
              }
            } catch (e) {
              console.error('Failed to parse evalResultListJson:', e)
            }
          }
        }
      }
    }
  } catch (error) {
    console.error('Failed to load data:', error)
  }
}

onMounted(() => {
  if (recordId.value) {
    loadData()
  }
  loadSoftwareList()
})
</script>

<style scoped>
.oss-impl-apply-form-page {
  padding: 16px;
}

.page-card {
  background: #fff;
  border-radius: 4px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.apply-form {
  padding: 24px 20px;
}

.form-section {
  margin-bottom: 32px;
}

.section-title {
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  max-width: 800px;
  margin: 0 auto;
}

.form-grid .full-width {
  grid-column: 1 / -1;
}

.form-input {
  width: 100%;
}

.white-input {
  width: 100%;
  background: #fff !important;
}

.white-input :deep(.el-input__wrapper) {
  background-color: #fff !important;
}

.char-count {
  position: absolute;
  right: 8px;
  bottom: 4px;
  font-size: 12px;
  color: #909399;
}

.media-section,
.attachment-section {
  padding: 0 16px;
}

.btn-select-media,
.btn-add-attachment {
  margin-bottom: 12px;
}

.media-table,
.attachment-table {
  margin-top: 8px;
}

.attachment-tip {
  margin: 8px 0;
  font-size: 12px;
}

.attachment-tip .red {
  color: #f56c6c;
}

.eval-block {
  margin-bottom: 16px;
}

.eval-block-title {
  margin: 0 0 8px 0;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}

.eval-table {
  margin-bottom: 8px;
}

.eval-select {
  width: 100%;
}

.display-only {
  color: #909399;
}

.total-score {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 12px 0;
  gap: 8px;
}

.total-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.total-value {
  font-size: 24px;
  font-weight: 700;
  color: #409eff;
}

.total-max {
  font-size: 14px;
  color: #909399;
}

/* Bottom Buttons */
.form-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 20px 0 8px;
  border-top: 1px solid #ebeef5;
  margin-top: 24px;
}

.btn-back {
  min-width: 80px;
}

.btn-save {
  min-width: 80px;
}

.btn-submit {
  min-width: 80px;
}
</style>
