<template>
  <div class="dashboard animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">IT架构总览</h1>
      <p class="page-header__desc">全行IT资产与架构运行状态实时监控</p>
    </div>

    <!-- Statistics Cards -->
    <div class="stat-grid">
      <div class="stat-card stat-card--primary animate-slide-up">
        <div class="stat-card__icon"><el-icon><Box /></el-icon></div>
        <div class="stat-card__value font-mono">{{ stats.serverCount }}</div>
        <div class="stat-card__label">服务器总数</div>
      </div>
      <div class="stat-card stat-card--success animate-slide-up animate-delay-1">
        <div class="stat-card__icon"><el-icon><Connection /></el-icon></div>
        <div class="stat-card__value font-mono">{{ stats.networkCount }}</div>
        <div class="stat-card__label">网络设备</div>
      </div>
      <div class="stat-card stat-card--warning animate-slide-up animate-delay-2">
        <div class="stat-card__icon"><el-icon><Document /></el-icon></div>
        <div class="stat-card__value font-mono">{{ stats.storageCount }}</div>
        <div class="stat-card__label">存储设备</div>
      </div>
      <div class="stat-card stat-card--info animate-slide-up animate-delay-3">
        <div class="stat-card__icon"><el-icon><Cloudy /></el-icon></div>
        <div class="stat-card__value font-mono">{{ stats.cloudCount }}</div>
        <div class="stat-card__label">云资源</div>
      </div>
      <div class="stat-card stat-card--primary animate-slide-up animate-delay-4">
        <div class="stat-card__icon"><el-icon><Grid /></el-icon></div>
        <div class="stat-card__value font-mono">{{ stats.appCount }}</div>
        <div class="stat-card__label">应用系统</div>
      </div>
      <div class="stat-card stat-card--success animate-slide-up animate-delay-5">
        <div class="stat-card__icon"><el-icon><Tickets /></el-icon></div>
        <div class="stat-card__value font-mono">{{ stats.todoCount }}</div>
        <div class="stat-card__label">待审批任务</div>
      </div>
    </div>

    <!-- Charts Row -->
    <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 24px; margin-bottom: 24px;">
      <div class="page-card animate-slide-up animate-delay-6">
        <div class="page-card__header">
          <h3><el-icon color="var(--accent)"><PieChart /></el-icon> 应用系统状态分布</h3>
        </div>
        <div ref="statusChartRef" style="height: 300px;"></div>
      </div>
      <div class="page-card animate-slide-up animate-delay-7">
        <div class="page-card__header">
          <h3><el-icon color="var(--accent)"><DataLine /></el-icon> 技术栈分布</h3>
        </div>
        <div ref="techChartRef" style="height: 300px;"></div>
      </div>
    </div>

    <!-- Recent Activities -->
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
      <div class="page-card animate-slide-up animate-delay-8">
        <div class="page-card__header">
          <h3><el-icon color="var(--accent)"><Clock /></el-icon> 最新应用系统</h3>
          <el-button type="primary" text @click="router.push('/arch/application')">查看全部 <el-icon><ArrowRight /></el-icon></el-button>
        </div>
        <el-table :data="recentApps" style="width: 100%">
          <el-table-column prop="appName" label="应用名称" min-width="140">
            <template #default="{ row }">
              <div style="display: flex; align-items: center; gap: 8px;">
                <span class="status-dot" :class="'status-dot--' + getLifecycleDot(row.lifecycle)"></span>
                <span style="color: var(--text-primary);">{{ row.appName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="appType" label="类型" width="80"><template #default="{ row }"><el-tag size="small">{{ row.appType }}</el-tag></template></el-table-column>
          <el-table-column prop="importanceLevel" label="级别" width="70"><template #default="{ row }"><span :style="{ color: getLevelColor(row.importanceLevel) }">{{ row.importanceLevel }}</span></template></el-table-column>
          <el-table-column prop="lifecycle" label="生命周期" width="100"><template #default="{ row }"><span class="text-secondary">{{ row.lifecycle }}</span></template></el-table-column>
        </el-table>
      </div>

      <div class="page-card animate-slide-up animate-delay-9">
        <div class="page-card__header">
          <h3><el-icon color="var(--warning)"><Tickets /></el-icon> 待审批任务</h3>
          <el-button type="primary" text @click="router.push('/wf/todo')">立即处理 <el-icon><ArrowRight /></el-icon></el-button>
        </div>
        <div class="approval-list">
          <div v-for="item in pendingApprovals" :key="item.id" class="approval-item">
            <div class="approval-item__info">
              <div class="approval-item__title">{{ item.taskName }}</div>
              <div class="approval-item__meta">
                <span>{{ item.applicantName }}</span>
                <span class="text-muted">|</span>
                <span>{{ item.departmentName }}</span>
                <span class="text-muted">|</span>
                <span>{{ item.createTime }}</span>
              </div>
            </div>
            <div class="approval-item__action">
              <el-button type="primary" size="small" @click="router.push('/wf/todo')">审批</el-button>
            </div>
          </div>
          <el-empty v-if="pendingApprovals.length === 0" description="暂无待审批任务" :image-size="60" />
        </div>
      </div>
    </div>

    <div class="page-card mt-lg animate-slide-up animate-delay-10">
      <div class="page-card__header">
        <h3><el-icon color="var(--accent)"><DataAnalysis /></el-icon> 部门资源分布</h3>
      </div>
      <div ref="deptChartRef" style="height: 280px;"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { reportApi, archAppApi, wfApi } from '@/api'

const router = useRouter()
const route = useRoute()
const statusChartRef = ref(null)
const techChartRef = ref(null)
const deptChartRef = ref(null)
let statusChart = null, techChart = null, deptChart = null

const stats = reactive({ serverCount: 0, networkCount: 0, storageCount: 0, cloudCount: 0, appCount: 0, todoCount: 0 })
const recentApps = ref([])
const pendingApprovals = ref([])

const loadData = async () => {
  try {
    const [statsRes, appsRes, todoRes] = await Promise.allSettled([
      reportApi.resourceStats(),
      archAppApi.list({ page: 1, pageSize: 5 }),
      wfApi.todoList({ page: 1, pageSize: 4 })
    ])
    if (statsRes.status === 'fulfilled') {
      const d = statsRes.value.data || {}
      Object.assign(stats, { serverCount: d.serverCount || 0, networkCount: d.networkCount || 0, storageCount: d.storageCount || 0, cloudCount: d.cloudCount || 0, appCount: d.appCount || 0, todoCount: d.todoCount || 0 })
    }
    if (appsRes.status === 'fulfilled') recentApps.value = appsRes.value.data.records || []
    if (todoRes.status === 'fulfilled') pendingApprovals.value = todoRes.value.data.records || []
  } catch {}
}

// 监听路由变化，强制重新加载数据
watch(() => route.path, () => { loadData(); initCharts() })

const getLevelColor = (level) => level === '核心' ? 'var(--danger)' : level === '重要' ? 'var(--warning)' : 'var(--text-secondary)'
const getLifecycleDot = (lifecycle) => lifecycle === 'PRODUCTION' ? 'online' : lifecycle === 'DEVELOPMENT' ? 'warning' : 'offline'

const initCharts = () => {
  statusChart = echarts.init(statusChartRef.value)
  statusChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', backgroundColor: 'var(--bg-card)', borderColor: 'var(--border)', textStyle: { color: 'var(--text-primary)' } },
    legend: { orient: 'vertical', right: '5%', top: 'center', textStyle: { color: 'var(--text-secondary)' } },
    series: [{ type: 'pie', radius: ['45%', '70%'], center: ['35%', '50%'], avoidLabelOverlap: false, itemStyle: { borderRadius: 6, borderColor: 'var(--bg-card)', borderWidth: 2 }, label: { show: false }, emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold', color: 'var(--text-primary)' } }, data: [{ value: 120, name: '运行中', itemStyle: { color: '#2ecc71' } }, { value: 20, name: '开发中', itemStyle: { color: '#3498db' } }, { value: 10, name: '测试中', itemStyle: { color: '#f39c12' } }, { value: 6, name: '已下线', itemStyle: { color: '#95a5a6' } }] }]
  })

  techChart = echarts.init(techChartRef.value)
  techChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: 'var(--bg-card)', borderColor: 'var(--border)', textStyle: { color: 'var(--text-primary)' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: { type: 'value', axisLine: { lineStyle: { color: 'var(--border)' } }, axisLabel: { color: 'var(--text-muted)' }, splitLine: { lineStyle: { color: 'var(--border)', type: 'dashed' } } },
    yAxis: { type: 'category', data: ['Java', 'Spring Boot', 'MySQL', 'Redis', 'Vue.js', 'k8s'], axisLine: { lineStyle: { color: 'var(--border)' } }, axisLabel: { color: 'var(--text-secondary)' } },
    series: [{ type: 'bar', data: [89, 76, 65, 54, 43, 32], itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#c9a962' }, { offset: 1, color: '#2d5a8a' }]), borderRadius: [0, 4, 4, 0] }, barWidth: 16 }]
  })

  deptChart = echarts.init(deptChartRef.value)
  deptChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: 'var(--bg-card)', borderColor: 'var(--border)', textStyle: { color: 'var(--text-primary)' } },
    legend: { data: ['服务器', '网络设备', '存储设备'], bottom: 0, textStyle: { color: 'var(--text-secondary)' } },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '8%', containLabel: true },
    xAxis: { type: 'category', data: ['科技部', '渠道部', '信贷部', '运维部', '数据中心', '安全部'], axisLine: { lineStyle: { color: 'var(--border)' } }, axisLabel: { color: 'var(--text-secondary)' } },
    yAxis: { type: 'value', axisLine: { lineStyle: { color: 'var(--border)' } }, axisLabel: { color: 'var(--text-muted)' }, splitLine: { lineStyle: { color: 'var(--border)', type: 'dashed' } } },
    series: [
      { name: '服务器', type: 'bar', stack: 'total', data: [320, 232, 180, 290, 210, 150], itemStyle: { color: 'var(--primary-light)' }, barWidth: 24 },
      { name: '网络设备', type: 'bar', stack: 'total', data: [120, 82, 90, 130, 80, 60], itemStyle: { color: 'var(--accent)' }, barWidth: 24 },
      { name: '存储设备', type: 'bar', stack: 'total', data: [30, 20, 25, 40, 30, 20], itemStyle: { color: 'var(--info)' }, barWidth: 24 }
    ]
  })
}

const handleResize = () => { statusChart?.resize(); techChart?.resize(); deptChart?.resize() }

onMounted(() => { loadData(); initCharts(); window.addEventListener('resize', handleResize) })
onUnmounted(() => { window.removeEventListener('resize', handleResize); statusChart?.dispose(); techChart?.dispose(); deptChart?.dispose() })
</script>

<style scoped lang="scss">
.approval-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.approval-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--bg-dark-3);
  border-radius: 8px;
  border: 1px solid var(--border);
  transition: all 0.2s ease;
  &:hover { border-color: var(--border-light); background: var(--bg-hover); }
  &__title { font-weight: 500; color: var(--text-primary); margin-bottom: 4px; }
  &__meta { font-size: 0.8125rem; color: var(--text-secondary); display: flex; gap: 8px; align-items: center; }
}
</style>
