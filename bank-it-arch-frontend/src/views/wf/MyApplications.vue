<template>
  <div class="wf-my-applications animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">我的申请</h1>
      <p class="page-header__desc">我发起的审批流程</p>
    </div>
    <div class="page-card">
      <el-table :data="instanceList" v-loading="loading" stripe>
        <el-table-column type="index" width="50" />
        <el-table-column prop="title" label="申请标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="businessType" label="业务类型" width="140">
          <template #default="{ row }">
            <el-tag size="small">{{ getBusinessTypeText(row.businessType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentNodeName" label="当前节点" width="140">
          <template #default="{ row }">
            <el-tag size="small" type="warning">{{ row.currentNodeName || '已结束' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="申请时间" width="160" />
        <el-table-column prop="result" label="审批结果" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.result">{{ getResultText(row.result) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleView(row)">查看</el-button>
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
    <el-dialog v-model="viewDialogVisible" title="申请详情" width="900px">
      <div v-if="viewInstance" class="instance-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请标题" :span="2">{{ viewInstance.title }}</el-descriptions-item>
          <el-descriptions-item label="业务类型">{{ getBusinessTypeText(viewInstance.businessType) }}</el-descriptions-item>
          <el-descriptions-item label="当前节点">{{ viewInstance.currentNodeName || '已结束' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="small" :type="getStatusType(viewInstance.status)">{{ getStatusText(viewInstance.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ viewInstance.startTime }}</el-descriptions-item>
          <el-descriptions-item label="审批结果">
            <span v-if="viewInstance.result">{{ getResultText(viewInstance.result) }}</span>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 审批进度 -->
        <div v-if="historyList.length > 0" class="history-section">
          <h4 style="margin: 16px 0 8px;">审批历史</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in historyList"
              :key="index"
              :timestamp="item.createTime"
              :type="getHistoryType(item.action)">
              <p><strong>{{ item.nodeName }}</strong> - {{ item.operatorName }}</p>
              <p>{{ getActionText(item.action) }}：{{ item.opinion || '无' }}</p>
            </el-timeline-item>
          </el-timeline>
        </div>

        <!-- 如果是引入申请，显示申请详情 -->
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
              </el-descriptions>
            </el-tab-pane>
            <el-tab-pane label="评审信息" name="review">
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="评审背景">{{ applyDetail.evalBackground || '无' }}</el-descriptions-item>
                <el-descriptions-item label="评审结论">{{ applyDetail.evalConclusion || '无' }}</el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>
            <el-tab-pane label="评测指标" name="eval">
              <el-table v-if="evalIndicators.length > 0" :data="evalIndicators" border size="small">
                <el-table-column prop="level1" label="一级指标" width="100" />
                <el-table-column prop="level2" label="二级指标" width="160" />
                <el-table-column prop="criteria" label="指标说明" min-width="200" show-overflow-tooltip />
                <el-table-column prop="weight" label="分值权重" width="80" align="center" />
                <el-table-column prop="result" label="评测结果" width="120" align="center">
                  <template #default="{ row }">
                    <span v-if="row.inputType === 'NUMBER'">{{ row.result }}</span>
                    <span v-else>{{ getOptionLabel(row) }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="score" label="评测得分" width="80" align="center" />
              </el-table>
              <div v-else>暂无评测数据</div>
              <div v-if="evalIndicators.length > 0" style="text-align: right; margin-top: 12px; font-weight: 600;">
                质量指标总分：{{ evalTotalScore }} / 100
              </div>
            </el-tab-pane>
            <el-tab-pane label="审批路径" name="trace">
              <div v-if="traceLoading" style="text-align: center; padding: 40px;">加载中...</div>
              <div v-else-if="!traceData" style="text-align: center; padding: 40px;">暂无审批路径</div>
              <div v-else class="trace-container">
                <div class="trace-nodes">
                  <div
                    v-for="node in traceData.nodes"
                    :key="node.nodeId"
                    class="trace-node"
                    :class="{
                      'trace-node--start': node.nodeCategory === 'START',
                      'trace-node--end': node.nodeCategory === 'END',
                      'trace-node--approval': node.nodeCategory === 'APPROVAL',
                      'trace-node--current': node.current,
                      'trace-node--executed': node.executed
                    }">
                    <div class="trace-node__icon">
                      <span v-if="node.nodeCategory === 'START'">▶</span>
                      <span v-else-if="node.nodeCategory === 'END'">■</span>
                      <span v-else>◆</span>
                    </div>
                    <div class="trace-node__name">{{ node.nodeName }}</div>
                    <div v-if="node.assigneeName" class="trace-node__assignee">{{ node.assigneeName }}</div>
                    <div v-if="node.action" class="trace-node__action">
                      <el-tag size="small" :type="node.action === 'APPROVE' ? 'success' : 'danger'">
                        {{ getActionText(node.action) }}
                      </el-tag>
                    </div>
                    <div v-if="node.operatorName" class="trace-node__operator">{{ node.operatorName }}</div>
                  </div>
                </div>
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
import { ElMessage } from 'element-plus'
import { wfApi, ossImplApplyApi } from '@/api'

const loading = ref(false)
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const instanceList = ref([])
const viewDialogVisible = ref(false)
const viewInstance = ref(null)
const historyList = ref([])
const applyDetail = ref(null)
const traceData = ref(null)
const traceLoading = ref(false)

// 评测指标数据
const evalIndicators = computed(() => {
  if (!applyDetail.value?.evalResultListJson) return []
  try {
    return JSON.parse(applyDetail.value.evalResultListJson)
  } catch {
    return []
  }
})

const evalTotalScore = computed(() => {
  return evalIndicators.value.reduce((sum, ind) => sum + (ind.score || 0), 0)
})

const getOptionLabel = (row) => {
  if (!row.options) return row.result
  const opt = row.options.find(o => o.value === row.result)
  return opt ? opt.label : row.result
}

const getBusinessTypeText = (type) => {
  const map = {
    'OSS_IMPL_APPLY': '开源软件引入',
    'ARCH_APPLICATION': '应用架构',
    'TECH_STACK': '技术栈',
    'DATA_ARCH': '数据架构'
  }
  return map[type] || type
}

const getStatusType = (status) => {
  const map = {
    'RUNNING': 'primary',
    'COMPLETED': 'success',
    'REJECTED': 'danger',
    'WITHDRAWN': 'warning',
    'TERMINATED': 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'RUNNING': '审批中',
    'COMPLETED': '已完成',
    'REJECTED': '已拒绝',
    'WITHDRAWN': '已撤回',
    'TERMINATED': '已终止'
  }
  return map[status] || status
}

const getResultText = (result) => {
  const map = {
    'APPROVED': '通过',
    'REJECTED': '拒绝',
    'WITHDRAWN': '撤回',
    'TERMINATED': '终止'
  }
  return map[result] || result
}

const getActionText = (action) => {
  const map = {
    'START': '发起',
    'APPROVE': '通过',
    'REJECT': '拒绝',
    'TRANSFER': '转办',
    'DELEGATE': '加签',
    'CLAIM': '签收'
  }
  return map[action] || action
}

const getHistoryType = (action) => {
  const map = {
    'START': 'primary',
    'APPROVE': 'success',
    'REJECT': 'danger',
    'TRANSFER': 'warning',
    'DELEGATE': 'info',
    'CLAIM': 'info'
  }
  return map[action] || 'info'
}

const getCategoryText = (category) => {
  const map = { 'BASE': '开源基础软件', 'TOOL': '开源工具软件', 'CMPNT': '开源组件' }
  return map[category] || category
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await wfApi.myApplications({ page: pagination.page, pageSize: pagination.pageSize })
    if (res.code === 200) {
      instanceList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('Failed to load my applications:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleView = async (row) => {
  viewInstance.value = row
  viewDialogVisible.value = true
  historyList.value = []
  applyDetail.value = null
  traceData.value = null

  // 获取审批历史
  try {
    const historyRes = await wfApi.instanceHistory(row.id)
    if (historyRes.code === 200) {
      historyList.value = historyRes.data || []
    }
  } catch (error) {
    console.error('Failed to load history:', error)
  }

  // 如果是引入申请，获取申请详情
  if (row.businessType === 'OSS_IMPL_APPLY' && row.businessKey) {
    try {
      const res = await ossImplApplyApi.detailByUuid(row.businessKey)
      if (res.code === 200) {
        applyDetail.value = res.data
      }
    } catch (error) {
      console.error('Failed to load apply detail:', error)
    }
  }

  // 加载审批路径图
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

onMounted(() => { loadData() })
</script>

<style scoped>
.history-section {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.apply-section {
  margin-top: 16px;
}

.trace-container {
  padding: 20px 0;
}

.trace-nodes {
  display: flex;
  align-items: center;
  gap: 0;
  overflow-x: auto;
  padding: 20px 0;
}

.trace-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 120px;
  padding: 12px 16px;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
  position: relative;
}

.trace-node + .trace-node::before {
  content: '→';
  position: absolute;
  left: -20px;
  color: #909399;
  font-size: 16px;
}

.trace-node--start {
  border-color: #409eff;
  background: #ecf5ff;
}

.trace-node--end {
  border-color: #67c23a;
  background: #f0f9eb;
}

.trace-node--approval {
  border-color: #e6a23c;
  background: #fef0f0;
}

.trace-node--current {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.3);
}

.trace-node--executed {
  border-color: #67c23a;
  background: #f0f9eb;
}

.trace-node__icon {
  font-size: 20px;
  margin-bottom: 4px;
  color: #409eff;
}

.trace-node--end .trace-node__icon {
  color: #67c23a;
}

.trace-node--approval .trace-node__icon {
  color: #e6a23c;
}

.trace-node__name {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  text-align: center;
}

.trace-node__assignee {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.trace-node__action {
  margin-top: 4px;
}

.trace-node__operator {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 2px;
}
</style>
