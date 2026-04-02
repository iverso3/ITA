<template>
  <div class="wf-task-detail animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">任务详情</h1>
      <p class="page-header__desc">查看任务详情并进行审批操作</p>
    </div>

    <div class="page-card" v-loading="loading">
      <!-- 基本信息 -->
      <el-descriptions title="任务信息" :column="2" border>
        <el-descriptions-item label="任务名称" :span="2">{{ taskData.taskName }}</el-descriptions-item>
        <el-descriptions-item label="流程名称">{{ taskData.definitionName }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag size="small" type="warning">{{ taskData.status === 'PENDING' ? '待审批' : taskData.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请人">{{ taskData.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="申请部门">{{ taskData.applicantDeptName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ taskData.createTime }}</el-descriptions-item>
      </el-descriptions>

      <!-- 引入申请详情 - 完整表单 -->
      <div v-if="taskData.businessType === 'OSS_IMPL_APPLY' && applyDetail" class="apply-section">
        <h3 class="section-title">申请信息</h3>

        <!-- 发起人编辑模式 -->
        <div v-if="isStartNode" class="edit-mode-hint">
          <el-alert type="info" :closable="false" show-icon>
            <template #title>
              <span>请修改以下信息后，点击下方「重新提交」按钮</span>
            </template>
          </el-alert>
        </div>

        <!-- 基本信息 - 查看模式 -->
        <div v-if="!isStartNode" class="form-section">
          <h4 class="sub-section-title">基本信息</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="申请单号">{{ applyDetail.implApplyNo }}</el-descriptions-item>
            <el-descriptions-item label="申请类型">{{ applyDetail.implApplyType === '0' ? '首次引入' : '新版本引入' }}</el-descriptions-item>
            <el-descriptions-item label="软件分类">{{ getCategoryText(applyDetail.swCategory) }}</el-descriptions-item>
            <el-descriptions-item label="开源软件名称">{{ applyDetail.swName }}</el-descriptions-item>
            <el-descriptions-item label="开源软件版本">{{ applyDetail.swVersion }}</el-descriptions-item>
            <el-descriptions-item label="开源许可协议">{{ applyDetail.licAbbr }}</el-descriptions-item>
            <el-descriptions-item label="是否安全工具">{{ applyDetail.secInstrt === '1' ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="操作系统">{{ getOsTypeText(applyDetail.osType) }}</el-descriptions-item>
            <el-descriptions-item label="应用编号">{{ applyDetail.useAppNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="投产版本">{{ applyDetail.launchVersion || '-' }}</el-descriptions-item>
            <el-descriptions-item label="任务编号及名称" :span="2">{{ applyDetail.launchTaskInfo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="申请说明" :span="2">{{ applyDetail.implCmnt || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 基本信息 - 编辑模式 -->
        <div v-else class="form-section">
          <h4 class="sub-section-title">基本信息</h4>
          <el-form :model="applyForm" label-width="140px" size="small">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="申请单号">
                  <el-input v-model="applyForm.implApplyNo" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="申请类型">
                  <el-select v-model="applyForm.implApplyType" placeholder="请选择" style="width: 100%">
                    <el-option label="首次引入" value="0" />
                    <el-option label="新版本引入" value="1" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="软件分类">
                  <el-input v-model="applyForm.swCategory" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="开源软件名称">
                  <el-input v-model="applyForm.swName" disabled />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="开源软件版本">
                  <el-input v-model="applyForm.swVersion" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="开源许可协议">
                  <el-input v-model="applyForm.licAbbr" disabled />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="是否安全工具">
                  <el-radio-group v-model="applyForm.secInstrt">
                    <el-radio label="1">是</el-radio>
                    <el-radio label="0">否</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="操作系统">
                  <el-select v-model="applyForm.osType" placeholder="请选择" style="width: 100%">
                    <el-option label="Windows" value="WINDOWS" />
                    <el-option label="Linux" value="LINUX" />
                    <el-option label="macOS" value="MACOS" />
                    <el-option label="跨平台" value="CROSS_PLATFORM" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="应用编号">
                  <el-input v-model="applyForm.useAppNo" placeholder="请输入" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="投产版本">
                  <el-input v-model="applyForm.launchVersion" placeholder="请输入" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="任务编号及名称">
              <el-input v-model="applyForm.launchTaskInfo" placeholder="请输入" />
            </el-form-item>
            <el-form-item label="申请说明">
              <el-input v-model="applyForm.implCmnt" type="textarea" :rows="3" placeholder="请输入" />
            </el-form-item>
          </el-form>
        </div>

        <!-- 评审背景及结论 - 查看模式 -->
        <div v-if="!isStartNode" class="form-section">
          <h4 class="sub-section-title">评审背景及结论</h4>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="评审背景">{{ applyDetail.evalBackground || '-' }}</el-descriptions-item>
            <el-descriptions-item label="系统环境">{{ applyDetail.systemEnv || '-' }}</el-descriptions-item>
            <el-descriptions-item label="功能介绍">{{ applyDetail.functionIntro || '-' }}</el-descriptions-item>
            <el-descriptions-item label="评审结论">{{ applyDetail.evalConclusion || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 评审背景及结论 - 编辑模式 -->
        <div v-else class="form-section">
          <h4 class="sub-section-title">评审背景及结论</h4>
          <el-form :model="applyForm" label-width="140px" size="small">
            <el-form-item label="评审背景">
              <el-input v-model="applyForm.evalBackground" type="textarea" :rows="3" placeholder="请输入评审背景" />
            </el-form-item>
            <el-form-item label="系统环境">
              <el-input v-model="applyForm.systemEnv" type="textarea" :rows="2" placeholder="请输入系统环境" />
            </el-form-item>
            <el-form-item label="功能介绍">
              <el-input v-model="applyForm.functionIntro" type="textarea" :rows="3" placeholder="请输入功能介绍" />
            </el-form-item>
            <el-form-item label="评审结论">
              <el-input v-model="applyForm.evalConclusion" type="textarea" :rows="2" placeholder="请输入评审结论" />
            </el-form-item>
          </el-form>
        </div>

        <!-- 评测指标 -->
        <div class="form-section">
          <h4 class="sub-section-title">评测指标</h4>

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
                  <span v-if="row.inputType === 'DISPLAY_ONLY'">-</span>
                  <span v-else>{{ getOptionLabel(row) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="评测得分" width="70" align="center">
                <template #default="{ row }">
                  <span>{{ row.score || 0 }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 质量指标 -->
          <div class="eval-block">
            <div class="eval-block-title">质量指标</div>
            <el-table :data="qualityIndicators" border size="small">
              <el-table-column prop="level1" label="一级指标" width="100" />
              <el-table-column prop="level2" label="二级指标" width="150" />
              <el-table-column prop="criteria" label="指标说明" min-width="200" show-overflow-tooltip />
              <el-table-column prop="weight" label="分值权重" width="70" align="center" />
              <el-table-column label="评测结果" width="150" align="center">
                <template #default="{ row }">
                  <span v-if="row.inputType === 'DISPLAY_ONLY'">-</span>
                  <span v-else-if="row.inputType === 'NUMBER'">{{ row.result }}</span>
                  <span v-else>{{ getOptionLabel(row) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="评测得分" width="70" align="center">
                <template #default="{ row }">
                  <span>{{ row.score || 0 }}</span>
                </template>
              </el-table-column>
            </el-table>
            <div class="total-score">质量指标总分：{{ qualityTotalScore }} / 100</div>
          </div>
        </div>

        <!-- 管理员审批补充信息 - 仅在开源软件管理员审批节点显示 -->
        <div v-if="isAdminApprovalNode" class="form-section approval-supl-section">
          <h4 class="sub-section-title">管理员审批补充信息</h4>
          <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px;">
            <template #title>
              <span>请补充以下信息后，点击「通过」完成审批</span>
            </template>
          </el-alert>
          <el-form :model="approvalSuplForm" label-width="140px" size="small">
            <!-- 新版本引入和首次引入都需要填写的字段 -->
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="引入团队" required>
                  <el-input v-model="approvalSuplForm.implTeamName" placeholder="请输入引入团队" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="适用场景" required>
                  <el-select v-model="approvalSuplForm.applicableScene" placeholder="请选择" style="width: 100%">
                    <el-option label="全场景适用" value="ALL_SCENE" />
                    <el-option label="限定内部使用" value="INNER_SCENE" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="适用职能范围" required>
                  <el-select v-model="approvalSuplForm.applicableFunctionRange" placeholder="请选择" style="width: 100%">
                    <el-option label="全行使用" value="ALL_BANK_USE" />
                    <el-option label="仅分行使用" value="ONLY_BRANCH_USE" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="是否主推荐使用" required>
                  <el-select v-model="approvalSuplForm.isMainUse" placeholder="请选择" style="width: 100%">
                    <el-option label="是" value="1" />
                    <el-option label="否" value="0" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="版本类型" required>
                  <el-select v-model="approvalSuplForm.verType" placeholder="请选择" style="width: 100%">
                    <el-option label="白名单" value="WHITELIST" />
                    <el-option label="灰名单" value="GREYLIST" />
                    <el-option label="黑名单" value="BLACKLIST" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 仅首次引入需要填写的字段 -->
            <template v-if="applyDetail?.implApplyType === '0'">
              <el-divider content-position="left">以下仅首次引入需要填写</el-divider>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="责任团队" required>
                    <el-input v-model="approvalSuplForm.rspTeamName" placeholder="请输入责任团队" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="责任人" required>
                    <el-input v-model="approvalSuplForm.rspUserName" placeholder="请输入责任人" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="软件类型" required>
                    <el-select v-model="approvalSuplForm.swType" placeholder="请选择" style="width: 100%">
                      <el-option label="主推软件" value="MAIN" />
                      <el-option label="非主推软件" value="LIMIT" />
                      <el-option label="已退出软件" value="QUIT" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="产品类型" required>
                    <el-select v-model="approvalSuplForm.productType" placeholder="请选择" style="width: 100%">
                      <el-option label="操作系统数据库" value="OS_DB" />
                      <el-option label="中间件" value="MIDDLEWARE" />
                      <el-option label="人工智能" value="AI" />
                      <el-option label="云计算" value="CLOUD" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="应用场景">
                <el-input v-model="approvalSuplForm.applicationScene" type="textarea" :rows="3" placeholder="请输入应用场景（最大1024字符）" maxlength="1024" show-word-limit />
              </el-form-item>
            </template>
          </el-form>
        </div>
      </div>

      <!-- 审批路径 - 垂直时间线表格 -->
      <div class="trace-section">
        <h3 class="section-title">审批路径</h3>
        <div v-if="traceLoading" style="text-align: center; padding: 40px;">加载中...</div>
        <div v-else-if="!traceData || traceData.nodes.length === 0" style="text-align: center; padding: 40px;">暂无审批路径</div>
        <div v-else class="trace-table-container">
          <el-table :data="traceNodes" border stripe>
            <el-table-column prop="nodeName" label="办理环节" width="150" />
            <el-table-column prop="operatorName" label="办理人" width="100" />
            <el-table-column prop="departmentName" label="办理部门" width="120" />
            <el-table-column prop="actionTime" label="时间" width="160">
              <template #default="{ row }">
                {{ row.actionTime ? formatTime(row.actionTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'COMPLETED'" size="small" type="success">已完成</el-tag>
                <el-tag v-else-if="row.status === 'REJECTED'" size="small" type="warning">回退</el-tag>
                <el-tag v-else-if="row.status === 'CURRENT'" size="small" type="primary">进行中</el-tag>
                <el-tag v-else-if="row.status === 'PENDING'" size="small" type="info">待处理</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="opinion" label="办理意见">
              <template #default="{ row }">
                <span>{{ row.opinion || '-' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 审批操作按钮 -->
      <div v-if="taskData.status === 'PENDING'" class="action-bar">
        <el-input v-model="opinion" type="textarea" :rows="2" :placeholder="isStartNode ? '请输入修改说明（可选）' : '请输入审批意见（可选）'" style="margin-bottom: 12px;" />
        <div class="action-buttons">
          <el-button @click="handleBack">返回</el-button>
          <!-- START节点显示"重新提交"，其他节点显示"通过" -->
          <el-button v-if="isStartNode" type="primary" @click="handleResubmit">重新提交</el-button>
          <el-button v-else type="primary" @click="handleApprove">通过</el-button>
          <el-button v-if="!isStartNode" type="danger" @click="handleReject">驳回</el-button>
        </div>
      </div>
      <div v-else class="action-bar">
        <el-button @click="handleBack">返回</el-button>
      </div>
    </div>

    <!-- 驳回对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回到指定节点" width="500px">
      <el-alert type="warning" :closable="false" style="margin-bottom: 16px;">
        请选择要驳回到的节点，驳回后该节点的审批人将重新收到审批任务
      </el-alert>
      <el-table :data="rejectNodes" stripe v-loading="rejectNodesLoading">
        <el-table-column width="50">
          <template #default="{ row }">
            <el-radio v-model="selectedRejectNode" :label="row.nodeId">&nbsp;</el-radio>
          </template>
        </el-table-column>
        <el-table-column prop="nodeName" label="节点名称" />
        <el-table-column prop="operatorName" label="审批人" />
        <el-table-column prop="action" label="审批结果">
          <template #default="{ row }">
            <el-tag size="small" :type="row.action === 'APPROVE' ? 'success' : 'danger'">
              {{ getActionText(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 16px;">
        <el-input v-model="rejectOpinion" type="textarea" :rows="2" placeholder="请输入驳回原因" />
      </div>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReject" :disabled="!selectedRejectNode">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { wfApi, ossImplApplyApi } from '@/api'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const taskData = ref({})
const applyDetail = ref(null)
const traceData = ref(null)
const traceLoading = ref(false)
const opinion = ref('')

// 是否是START节点（发起人的待办）
const isStartNode = computed(() => {
  return taskData.value.nodeCategory === 'START'
})

// 是否是开源软件管理员审批节点
// 通过判断：不是START节点，且业务类型是引入申请，且任务状态是待审批，且节点名称为"开源软件管理员"
const isAdminApprovalNode = computed(() => {
  if (taskData.value.nodeCategory === 'START') return false
  if (taskData.value.businessType !== 'OSS_IMPL_APPLY') return false
  if (taskData.value.status !== 'PENDING') return false
  // 节点名称等于"开源软件管理员"
  const nodeName = taskData.value.taskName || ''
  return nodeName === '开源软件管理员'
})

// 发起人编辑表单数据
const applyForm = reactive({
  id: '',
  implApplyNo: '',
  implApplyType: '',
  swCategory: '',
  swName: '',
  swVersion: '',
  licAbbr: '',
  secInstrt: '',
  osType: '',
  useAppNo: '',
  launchVersion: '',
  launchTaskInfo: '',
  implCmnt: '',
  evalBackground: '',
  systemEnv: '',
  functionIntro: '',
  evalConclusion: ''
})

// 管理员审批补充信息表单数据
const approvalSuplForm = reactive({
  applicableScene: '',      // 适用场景：ALL_SCENE / INNER_SCENE
  applicableFunctionRange: '', // 适用职能范围：ALL_BANK_USE / ONLY_BRANCH_USE
  isMainUse: '0',          // 是否主推荐使用：0 / 1
  verType: '',             // 版本类型：WHITELIST / GREYLIST / BLACKLIST
  implTeamId: '',          // 引入团队ID
  implTeamName: '',        // 引入团队名称
  rspTeamId: '',           // 责任团队ID（仅首次引入）
  rspTeamName: '',         // 责任团队名称（仅首次引入）
  rspUserId: '',           // 责任人ID（仅首次引入）
  rspUserName: '',         // 责任人姓名（仅首次引入）
  swType: '',              // 软件类型（仅首次引入）：MAIN / LIMIT / QUIT
  productType: '',         // 产品类型（仅首次引入）：OS_DB / MIDDLEWARE / AI / CLOUD
  applicationScene: ''     // 应用场景描述（仅首次引入）
})

// 驳回相关
const rejectDialogVisible = ref(false)
const rejectNodes = ref([])
const rejectNodesLoading = ref(false)
const selectedRejectNode = ref(null)
const rejectOpinion = ref('')

// 准入指标
const entryIndicators = ref([
  {
    level1: '许可证准入',
    level2: '开源许可证准入情况',
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
    criteria: '当前引入版本无高危及以上漏洞',
    weight: '/',
    result: '',
    score: 0,
    inputType: 'DROPDOWN',
    options: [
      { label: '无高危及以上漏洞', value: 'NONE' },
      { label: '有高危及以上漏洞', value: 'EXISTS' }
    ]
  },
  {
    level1: '安全风险准入',
    level2: '软件涉恶意代码情况',
    criteria: '当前引入版本未发现病毒或木马',
    weight: '/',
    result: '',
    score: 0,
    inputType: 'DROPDOWN',
    options: [
      { label: '未发现病毒或木马', value: 'NONE' },
      { label: '有发现病毒或木马', value: 'EXISTS' }
    ]
  }
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
      departmentName: '-',
      actionTime: node.actionTime,
      status: status,
      opinion: node.opinion || '-'
    }
  })
})

const getCategoryText = (category) => {
  const map = { 'BASE': '开源基础软件', 'TOOL': '开源工具软件', 'CMPNT': '开源组件' }
  return map[category] || category
}

const getOsTypeText = (osType) => {
  const map = { '0': 'Linux', '1': 'Windows', '2': 'AIX', '3': '其他' }
  return map[osType] || osType
}

const getActionText = (action) => {
  const map = {
    'START': '发起',
    'APPROVE': '通过',
    'REJECT': '驳回',
    'TRANSFER': '转办',
    'DELEGATE': '加签',
    'CLAIM': '签收'
  }
  return map[action] || action
}

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'
}

const loadData = async () => {
  const taskId = route.params.id
  if (!taskId) return

  loading.value = true
  try {
    // 获取任务详情
    const taskRes = await wfApi.taskDetail(taskId)
    if (taskRes.code === 200) {
      taskData.value = taskRes.data || {}
    }

    // 如果是引入申请，获取申请详情
    if (taskData.value.businessType === 'OSS_IMPL_APPLY' && taskData.value.businessKey) {
      try {
        const res = await ossImplApplyApi.detailByUuid(taskData.value.businessKey)
        if (res.code === 200) {
          applyDetail.value = res.data
          // 如果是START节点，填充表单数据用于编辑
          if (isStartNode.value) {
            Object.assign(applyForm, res.data)
          }
          // 如果是管理员审批节点，加载审批补充信息
          if (isAdminApprovalNode.value) {
            loadApprovalSuplData()
          }
          // 加载评测指标数据
          loadEvalIndicators()
        }
      } catch (error) {
        console.error('Failed to load apply detail:', error)
      }
    }

    // 获取审批路径
    if (taskData.value.instanceId) {
      traceLoading.value = true
      try {
        const res = await wfApi.instanceTrace(taskData.value.instanceId)
        if (res.code === 200) {
          traceData.value = res.data
        }
      } catch (error) {
        console.error('Failed to load trace:', error)
      } finally {
        traceLoading.value = false
      }
    }
  } catch (error) {
    console.error('Failed to load task detail:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadEvalIndicators = async () => {
  if (!applyDetail.value?.implApplyNo) return
  try {
    const suplRes = await ossImplApplyApi.supplementary(applyDetail.value.implApplyNo)
    if (suplRes.code === 200 && suplRes.data) {
      const supl = suplRes.data
      // 解析评测结果列表
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
      // 加载审批补充字段（从主表字段获取）
      if (supl.applicableScene) approvalSuplForm.applicableScene = supl.applicableScene
      if (supl.applicableFunctionRange) approvalSuplForm.applicableFunctionRange = supl.applicableFunctionRange
      if (supl.isMainUse) approvalSuplForm.isMainUse = supl.isMainUse
      if (supl.verType) approvalSuplForm.verType = supl.verType
      if (supl.productType) approvalSuplForm.productType = supl.productType
      if (supl.applicationScene) approvalSuplForm.applicationScene = supl.applicationScene
    }
  } catch (error) {
    console.error('Failed to load eval indicators:', error)
  }
}

// 加载审批补充信息（从申请详情中获取）
const loadApprovalSuplData = () => {
  if (!applyDetail.value) return
  // 从applyDetail中取审批补充字段（这些字段在oss_impl_apply_info表中）
  approvalSuplForm.applicableScene = applyDetail.value.applicableScene || ''
  approvalSuplForm.applicableFunctionRange = applyDetail.value.applicableFunctionRange || ''
  approvalSuplForm.isMainUse = applyDetail.value.isMainUse || '0'
  approvalSuplForm.verType = applyDetail.value.verType || ''
  approvalSuplForm.implTeamId = applyDetail.value.implTeamId || ''
  approvalSuplForm.implTeamName = applyDetail.value.implTeamName || ''
  approvalSuplForm.rspTeamId = applyDetail.value.rspTeamId || ''
  approvalSuplForm.rspTeamName = applyDetail.value.rspTeamName || ''
  approvalSuplForm.rspUserId = applyDetail.value.rspUserId || ''
  approvalSuplForm.rspUserName = applyDetail.value.rspUserName || ''
  approvalSuplForm.swType = applyDetail.value.swType || ''
  approvalSuplForm.productType = applyDetail.value.productType || ''
  approvalSuplForm.applicationScene = applyDetail.value.applicationScene || ''
}

const handleBack = () => {
  router.back()
}

const handleApprove = async () => {
  // 如果是管理员审批节点，需要先保存补充信息
  if (isAdminApprovalNode.value) {
    // 验证必填字段
    if (!approvalSuplForm.applicableScene) {
      ElMessage.warning('请选择适用场景')
      return
    }
    if (!approvalSuplForm.applicableFunctionRange) {
      ElMessage.warning('请选择适用职能范围')
      return
    }
    if (!approvalSuplForm.verType) {
      ElMessage.warning('请选择版本类型')
      return
    }
    if (applyDetail.value?.implApplyType === '0') {
      // 首次引入额外校验
      if (!approvalSuplForm.rspTeamName) {
        ElMessage.warning('请输入责任团队')
        return
      }
      if (!approvalSuplForm.rspUserName) {
        ElMessage.warning('请输入责任人')
        return
      }
      if (!approvalSuplForm.swType) {
        ElMessage.warning('请选择软件类型')
        return
      }
      if (!approvalSuplForm.productType) {
        ElMessage.warning('请选择产品类型')
        return
      }
    }
    try {
      // 保存补充信息到申请记录
      const suplData = {
        applicableScene: approvalSuplForm.applicableScene,
        applicableFunctionRange: approvalSuplForm.applicableFunctionRange,
        isMainUse: approvalSuplForm.isMainUse,
        verType: approvalSuplForm.verType,
        implTeamId: approvalSuplForm.implTeamId,
        implTeamName: approvalSuplForm.implTeamName,
        rspTeamId: approvalSuplForm.rspTeamId,
        rspTeamName: approvalSuplForm.rspTeamName,
        rspUserId: approvalSuplForm.rspUserId,
        rspUserName: approvalSuplForm.rspUserName,
        swType: approvalSuplForm.swType,
        productType: approvalSuplForm.productType,
        applicationScene: approvalSuplForm.applicationScene
      }
      await ossImplApplyApi.updateSupplementary(applyDetail.value.implApplyNo, suplData)
    } catch (error) {
      console.error('Failed to save supplementary info:', error)
      ElMessage.error('保存补充信息失败')
      return
    }
  }
  try {
    await wfApi.approve(route.params.id, { opinion: opinion.value })
    ElMessage.success('审批通过')
    router.push('/wf/todo')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 发起人重新提交：先更新表单，再提交审批
const handleResubmit = async () => {
  try {
    // 如果有表单数据，先更新
    if (applyForm.id) {
      await ossImplApplyApi.update(applyForm.id, applyForm)
    }
    // 然后提交审批（这会触发START节点的流转到下一个审批节点）
    await wfApi.approve(route.params.id, { opinion: opinion.value })
    ElMessage.success('重新提交成功')
    router.push('/wf/todo')
  } catch (error) {
    console.error('Failed to resubmit:', error)
    ElMessage.error('重新提交失败')
  }
}

const handleReject = async () => {
  // 加载可驳回的节点列表
  rejectNodesLoading.value = true
  try {
    const res = await wfApi.rejectNodes(route.params.id)
    if (res.code === 200) {
      rejectNodes.value = res.data || []
      if (rejectNodes.value.length === 0) {
        ElMessage.warning('没有可驳回的节点')
        return
      }
      selectedRejectNode.value = null
      rejectOpinion.value = ''
      rejectDialogVisible.value = true
    }
  } catch (error) {
    console.error('Failed to load reject nodes:', error)
    ElMessage.error('加载驳回节点失败')
  } finally {
    rejectNodesLoading.value = false
  }
}

const submitReject = async () => {
  if (!selectedRejectNode.value) {
    ElMessage.warning('请选择要驳回到的节点')
    return
  }
  if (!rejectOpinion.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  try {
    await wfApi.returnTask(route.params.id, {
      opinion: rejectOpinion.value,
      targetNodeId: selectedRejectNode.value
    })
    ElMessage.success('驳回成功')
    rejectDialogVisible.value = false
    router.push('/wf/todo')
  } catch (error) {
    ElMessage.error('驳回失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.wf-task-detail {
  padding: 20px;
}

.apply-section,
.trace-section {
  margin-top: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 12px;
  color: #303133;
  border-left: 3px solid #409eff;
  padding-left: 8px;
}

.form-section {
  margin-bottom: 24px;
}

.sub-section-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #606266;
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

.action-bar {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e0e0e0;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.edit-mode-hint {
  margin-bottom: 16px;
}

.approval-supl-section {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 2px dashed #409eff;
}
</style>
