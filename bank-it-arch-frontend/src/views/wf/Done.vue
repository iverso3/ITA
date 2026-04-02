<template>
  <div class="wf-done animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">我的已办</h1>
      <p class="page-header__desc">我提交的和已审批的流程记录</p>
    </div>

    <!-- 查询条件 -->
    <div class="page-card query-form">
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="类型">
          <el-select v-model="queryForm.businessType" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="开源软件引入" value="OSS_IMPL_APPLY" />
            <el-option label="应用架构" value="ARCH_APPLICATION" />
            <el-option label="技术栈" value="TECH_STACK" />
            <el-option label="数据架构" value="DATA_ARCH" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="queryForm.keyword" placeholder="请输入标题" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="提交人">
          <el-input v-model="queryForm.applicantName" placeholder="请输入" clearable style="width: 120px;" />
        </el-form-item>
        <el-form-item label="流程状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="处理中" value="RUNNING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已撤回" value="WITHDRAWN" />
            <el-option label="已终止" value="TERMINATED" />
          </el-select>
        </el-form-item>
        <el-form-item label="提交时间">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 列表 -->
    <div class="page-card">
      <el-table :data="instanceList" v-loading="loading" stripe>
        <el-table-column type="index" width="50" />
        <el-table-column prop="businessType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ getBusinessTypeText(row.businessType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="applicantName" label="提交人" width="100" />
        <el-table-column prop="startTime" label="提交时间" width="160" />
        <el-table-column prop="status" label="审批状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentNodeName" label="当前节点" width="140">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'RUNNING'" size="small" type="warning">{{ row.currentNodeName || '-' }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="currentApproverName" label="当前审批人" width="100">
          <template #default="{ row }">
            <span v-if="row.status === 'RUNNING'">{{ row.currentApproverName || '-' }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.myRole === 'APPLICANT' && row.status === 'RUNNING'" type="danger" text size="small" @click="handleWithdraw(row)">撤回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="pagination.page"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="loadData" />
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="详情" width="900px">
      <div v-if="viewInstance" class="instance-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题" :span="2">{{ viewInstance.title }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ getBusinessTypeText(viewInstance.businessType) }}</el-descriptions-item>
          <el-descriptions-item label="流程状态">
            <el-tag size="small" :type="getStatusType(viewInstance.status)">{{ getStatusText(viewInstance.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交人">{{ viewInstance.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ viewInstance.startTime }}</el-descriptions-item>
          <el-descriptions-item label="当前节点" v-if="viewInstance.status === 'RUNNING'">{{ viewInstance.currentNodeName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="当前审批人" v-if="viewInstance.status === 'RUNNING'">{{ viewInstance.currentApproverName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批结果" v-if="viewInstance.result">
            <el-tag size="small" :type="viewInstance.result === 'APPROVED' ? 'success' : 'danger'">{{ getResultText(viewInstance.result) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 引入申请详情 -->
        <div v-if="viewInstance.businessType === 'OSS_IMPL_APPLY' && applyDetail" class="apply-section">
          <h4 style="margin: 16px 0 8px;">申请信息</h4>
          <el-tabs>
            <el-tab-pane label="基本信息" name="basic">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="申请单号">{{ applyDetail.implApplyNo }}</el-descriptions-item>
                <el-descriptions-item label="申请类型">{{ applyDetail.implApplyType === '0' ? '首次引入' : '新版本引入' }}</el-descriptions-item>
                <el-descriptions-item label="软件名称">{{ applyDetail.swName }}</el-descriptions-item>
                <el-descriptions-item label="软件版本">{{ applyDetail.swVersion }}</el-descriptions-item>
                <el-descriptions-item label="软件分类">{{ getCategoryText(applyDetail.swCategory) }}</el-descriptions-item>
                <el-descriptions-item label="许可协议">{{ applyDetail.licAbbr }}</el-descriptions-item>
                <el-descriptions-item label="是否安全工具">{{ applyDetail.secInstrt === '1' ? '是' : '否' }}</el-descriptions-item>
                <el-descriptions-item label="操作系统">{{ getOsTypeText(applyDetail.osType) }}</el-descriptions-item>
                <el-descriptions-item label="应用编号">{{ applyDetail.useAppNo || '-' }}</el-descriptions-item>
                <el-descriptions-item label="投产版本">{{ applyDetail.launchVersion || '-' }}</el-descriptions-item>
                <el-descriptions-item label="任务编号及名称" :span="2">{{ applyDetail.launchTaskInfo || '-' }}</el-descriptions-item>
                <el-descriptions-item label="申请说明" :span="2">{{ applyDetail.implCmnt || '-' }}</el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>
            <el-tab-pane label="评审信息" name="review">
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="评审背景">{{ applyDetail.evalBackground || '-' }}</el-descriptions-item>
                <el-descriptions-item label="系统环境">{{ applyDetail.systemEnv || '-' }}</el-descriptions-item>
                <el-descriptions-item label="功能介绍">{{ applyDetail.functionIntro || '-' }}</el-descriptions-item>
                <el-descriptions-item label="评审结论">{{ applyDetail.evalConclusion || '-' }}</el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>
            <el-tab-pane label="评测指标" name="eval">
              <!-- 准入指标 -->
              <div class="eval-block">
                <div class="eval-block-title">准入指标</div>
                <el-table :data="entryIndicators" border size="small">
                  <el-table-column prop="level1" label="一级指标" width="100" />
                  <el-table-column prop="level2" label="二级指标" width="150" />
                  <el-table-column prop="criteria" label="指标说明" min-width="200" show-overflow-tooltip />
                  <el-table-column prop="weight" label="分值权重" width="70" align="center" />
                  <el-table-column label="评测结果" width="150" align="center">
                    <template #default="{ row }">
                      <span>{{ getOptionLabel(row) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="评测得分" width="70" align="center">
                    <template #default="{ row }"><span>{{ row.score || 0 }}</span></template>
                  </el-table-column>
                </el-table>
              </div>
              <!-- 质量指标 -->
              <div class="eval-block" v-if="qualityIndicators.length > 0">
                <div class="eval-block-title">质量指标</div>
                <el-table :data="qualityIndicators" border size="small">
                  <el-table-column prop="level1" label="一级指标" width="100" />
                  <el-table-column prop="level2" label="二级指标" width="150" />
                  <el-table-column prop="criteria" label="指标说明" min-width="200" show-overflow-tooltip />
                  <el-table-column prop="weight" label="分值权重" width="70" align="center" />
                  <el-table-column label="评测结果" width="150" align="center">
                    <template #default="{ row }">
                      <span v-if="row.inputType === 'NUMBER'">{{ row.result }}</span>
                      <span v-else>{{ getOptionLabel(row) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="评测得分" width="70" align="center">
                    <template #default="{ row }"><span>{{ row.score || 0 }}</span></template>
                  </el-table-column>
                </el-table>
                <div class="total-score">质量指标总分：{{ qualityTotalScore }} / 100</div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="审批路径" name="trace">
              <div v-if="traceLoading" style="text-align: center; padding: 40px;">加载中...</div>
              <div v-else-if="!traceData || traceData.nodes.length === 0" style="text-align: center; padding: 40px;">暂无审批路径</div>
              <div v-else class="trace-table-container">
                <el-table :data="traceNodes" border stripe>
                  <el-table-column prop="nodeName" label="办理环节" width="150" />
                  <el-table-column prop="operatorName" label="办理人" width="100" />
                  <el-table-column prop="actionTime" label="时间" width="160">
                    <template #default="{ row }">{{ row.actionTime ? formatTime(row.actionTime) : '-' }}</template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="100" align="center">
                    <template #default="{ row }">
                      <el-tag v-if="row.status === 'COMPLETED'" size="small" type="success">已完成</el-tag>
                      <el-tag v-else-if="row.status === 'REJECTED'" size="small" type="warning">回退</el-tag>
                      <el-tag v-else-if="row.status === 'CURRENT'" size="small" type="primary">进行中</el-tag>
                      <el-tag v-else size="small" type="info">待处理</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="opinion" label="办理意见">
                    <template #default="{ row }"><span>{{ row.opinion || '-' }}</span></template>
                  </el-table-column>
                </el-table>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { wfApi, ossImplApplyApi } from '@/api'
import dayjs from 'dayjs'

const loading = ref(false)
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const queryForm = reactive({
  businessType: '',
  keyword: '',
  applicantName: '',
  status: '',
  dateRange: []
})
const instanceList = ref([])
const viewDialogVisible = ref(false)
const viewInstance = ref(null)
const applyDetail = ref(null)
const traceData = ref(null)
const traceLoading = ref(false)

// 准入指标
const entryIndicators = ref([
  { level1: '许可证准入', level2: '开源许可证准入情况', criteria: '当前引入版本使用的开源许可证属于准入许可证', weight: '/', result: '', score: 0, inputType: 'DISPLAY_ONLY', options: [] },
  { level1: '安全风险准入', level2: '已暴露最高安全漏洞风险等级', criteria: '当前引入版本无高危及以上漏洞', weight: '/', result: '', score: 0, inputType: 'DROPDOWN', options: [{ label: '无高危及以上漏洞', value: 'NONE' }, { label: '有高危及以上漏洞', value: 'EXISTS' }] },
  { level1: '安全风险准入', level2: '软件涉恶意代码情况', criteria: '当前引入版本未发现病毒或木马', weight: '/', result: '', score: 0, inputType: 'DROPDOWN', options: [{ label: '未发现病毒或木马', value: 'NONE' }, { label: '有发现病毒或木马', value: 'EXISTS' }] }
])

// 质量指标
const qualityIndicators = ref([])

const qualityTotalScore = computed(() => {
  return qualityIndicators.value.reduce((sum, ind) => sum + (ind.score || 0), 0)
})

const getOptionLabel = (row) => {
  if (!row.options) return row.result
  const opt = row.options.find(o => o.value === row.result)
  return opt ? opt.label : row.result
}

// 转换trace数据为表格格式
const traceNodes = computed(() => {
  if (!traceData.value?.nodes) return []
  return traceData.value.nodes.map(node => {
    // 判断状态：当前节点=进行中，已执行且action=REJECT=回退，已执行=已完成，未执行=待处理
    let status = 'PENDING'
    if (node.current) {
      status = 'CURRENT'
    } else if (node.executed) {
      status = node.action === 'REJECT' ? 'REJECTED' : 'COMPLETED'
    }
    return {
      nodeName: node.nodeName,
      operatorName: node.operatorName || node.assigneeName || '-',
      actionTime: node.actionTime,
      status: status,
      opinion: node.opinion || '-'
    }
  })
})

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'
}

const getBusinessTypeText = (type) => {
  const map = { 'OSS_IMPL_APPLY': '开源软件引入', 'ARCH_APPLICATION': '应用架构', 'TECH_STACK': '技术栈', 'DATA_ARCH': '数据架构' }
  return map[type] || type
}

const getStatusType = (status) => {
  const map = { 'RUNNING': 'primary', 'COMPLETED': 'success', 'REJECTED': 'danger', 'WITHDRAWN': 'warning', 'TERMINATED': 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 'RUNNING': '处理中', 'COMPLETED': '已完成', 'REJECTED': '已拒绝', 'WITHDRAWN': '已撤回', 'TERMINATED': '已终止' }
  return map[status] || status
}

const getResultText = (result) => {
  const map = { 'APPROVED': '通过', 'REJECTED': '拒绝', 'WITHDRAWN': '撤回', 'TERMINATED': '终止' }
  return map[result] || result
}

const getCategoryText = (category) => {
  const map = { 'BASE': '开源基础软件', 'TOOL': '开源工具软件', 'CMPNT': '开源组件' }
  return map[category] || category
}

const getOsTypeText = (osType) => {
  const map = { '0': 'Linux', '1': 'Windows', '2': 'AIX', '3': '其他' }
  return map[osType] || osType
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      businessType: queryForm.businessType || undefined,
      keyword: queryForm.keyword || undefined,
      applicantName: queryForm.applicantName || undefined,
      status: queryForm.status || undefined,
      startDateFrom: queryForm.dateRange?.[0] || undefined,
      startDateTo: queryForm.dateRange?.[1] || undefined
    }
    const res = await wfApi.myDoneList(params)
    instanceList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('Failed to load:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  queryForm.businessType = ''
  queryForm.keyword = ''
  queryForm.applicantName = ''
  queryForm.status = ''
  queryForm.dateRange = []
  pagination.page = 1
  loadData()
}

const handleView = async (row) => {
  viewInstance.value = row
  viewDialogVisible.value = true
  applyDetail.value = null
  traceData.value = null

  // 如果是引入申请，获取申请详情
  if (row.businessType === 'OSS_IMPL_APPLY' && row.businessKey) {
    try {
      const res = await ossImplApplyApi.detailByUuid(row.businessKey)
      if (res.code === 200) {
        applyDetail.value = res.data
        loadEvalIndicators()
      }
    } catch (error) {
      console.error('Failed to load apply detail:', error)
    }
  }

  // 加载审批路径
  if (row.id) {
    traceLoading.value = true
    try {
      const res = await wfApi.instanceTrace(row.id)
      if (res.code === 200) {
        traceData.value = res.data
      }
    } catch (error) {
      console.error('Failed to load trace:', error)
    } finally {
      traceLoading.value = false
    }
  }
}

const loadEvalIndicators = async () => {
  if (!applyDetail.value?.implApplyNo) return
  try {
    const suplRes = await ossImplApplyApi.supplementary(applyDetail.value.implApplyNo)
    if (suplRes.code === 200 && suplRes.data) {
      const supl = suplRes.data
      if (supl.evalResultListJson && supl.evalResultListJson !== 'null') {
        try {
          const indicators = JSON.parse(supl.evalResultListJson)
          if (indicators && Array.isArray(indicators)) {
            // 更新准入指标
            entryIndicators.value.forEach(entry => {
              const found = indicators.find(i => i.level2 === entry.level2)
              if (found) {
                entry.result = found.result
                entry.score = found.score || 0
              }
            })
            // 设置质量指标
            qualityIndicators.value = indicators
          }
        } catch (e) {
          console.error('Failed to parse evalResultListJson:', e)
        }
      }
    }
  } catch (error) {
    console.error('Failed to load eval indicators:', error)
  }
}

const handleWithdraw = async (row) => {
  try {
    await ElMessageBox.confirm('确定要撤回该申请吗？', '提示', { type: 'warning' })
    await wfApi.withdrawInstance(row.id)
    ElMessage.success('撤回成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤回失败')
    }
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.query-form {
  padding: 16px 16px 0;
}

.apply-section {
  margin-top: 16px;
}

.eval-block {
  margin-bottom: 16px;
}

.eval-block-title {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}

.total-score {
  text-align: right;
  padding: 12px 0;
  font-weight: 600;
  color: #409eff;
}

.trace-table-container {
  margin-top: 12px;
}
</style>
