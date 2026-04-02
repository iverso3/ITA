<template>
  <div class="dashboard animate-fade-in">
    <!-- Page Header -->
    <div class="page-header">
      <h1 class="page-header__title">IT架构总览</h1>
      <p class="page-header__desc">全行IT资产与架构运行状态实时监控</p>
    </div>

    <!-- Statistics Strip - Full Width -->
    <div class="stats-strip">
      <div class="stat-card animate-slide-up">
        <div class="stat-card__icon stat-card__icon--primary"><el-icon><Box /></el-icon></div>
        <div class="stat-card__body">
          <div class="stat-card__value font-mono">{{ stats.serverCount }}</div>
          <div class="stat-card__label">服务器总数</div>
        </div>
      </div>
      <div class="stat-card animate-slide-up animate-delay-1">
        <div class="stat-card__icon stat-card__icon--success"><el-icon><Connection /></el-icon></div>
        <div class="stat-card__body">
          <div class="stat-card__value font-mono">{{ stats.networkCount }}</div>
          <div class="stat-card__label">网络设备</div>
        </div>
      </div>
      <div class="stat-card animate-slide-up animate-delay-2">
        <div class="stat-card__icon stat-card__icon--warning"><el-icon><Document /></el-icon></div>
        <div class="stat-card__body">
          <div class="stat-card__value font-mono">{{ stats.storageCount }}</div>
          <div class="stat-card__label">存储设备</div>
        </div>
      </div>
      <div class="stat-card animate-slide-up animate-delay-3">
        <div class="stat-card__icon stat-card__icon--info"><el-icon><Cloudy /></el-icon></div>
        <div class="stat-card__body">
          <div class="stat-card__value font-mono">{{ stats.cloudCount }}</div>
          <div class="stat-card__label">云资源</div>
        </div>
      </div>
      <div class="stat-card animate-slide-up animate-delay-4">
        <div class="stat-card__icon stat-card__icon--primary"><el-icon><Grid /></el-icon></div>
        <div class="stat-card__body">
          <div class="stat-card__value font-mono">{{ stats.appCount }}</div>
          <div class="stat-card__label">应用系统</div>
        </div>
      </div>
      <div class="stat-card animate-slide-up animate-delay-5">
        <div class="stat-card__icon stat-card__icon--danger"><el-icon><Tickets /></el-icon></div>
        <div class="stat-card__body">
          <div class="stat-card__value font-mono">{{ todoCount }}</div>
          <div class="stat-card__label">待审批任务</div>
        </div>
      </div>
    </div>

    <!-- Main Content Grid -->
    <div class="main-grid">
      <!-- Left: Task Panel -->
      <section class="task-section">
        <!-- Tabs -->
        <div class="task-tabs">
          <button
            class="task-tab"
            :class="{ 'task-tab--active': activeTab === 'todo' }"
            @click="activeTab = 'todo'"
          >
            <el-icon><Tickets /></el-icon>
            我的待办
            <span class="task-tab__badge" v-if="todoCount > 0">{{ todoCount }}</span>
          </button>
          <button
            class="task-tab"
            :class="{ 'task-tab--active': activeTab === 'focus' }"
            @click="activeTab = 'focus'"
          >
            <el-icon><Star /></el-icon>
            我的关注
            <span class="task-tab__badge" v-if="focusCount > 0">{{ focusCount }}</span>
          </button>
        </div>

        <!-- Task Content -->
        <div class="task-content" v-loading="taskLoading">
          <!-- Todo Tab - All Tasks List -->
          <div v-if="activeTab === 'todo'" class="task-body">
            <!-- All Tasks List (Flat) -->
            <div class="all-tasks-list" v-if="allTasks.length > 0">
              <div
                v-for="(task, taskIndex) in allTasks"
                :key="task.id"
                class="task-item"
                :style="{ animationDelay: taskIndex * 20 + 'ms' }"
                @click="handleTaskClick(task)"
              >
                <div class="task-item__priority" :class="'task-item__priority--' + getPriorityClass(task.priority)">
                  <el-icon v-if="task.priority === 'URGENT'"><WarningFilled /></el-icon>
                  <el-icon v-else><Clock /></el-icon>
                </div>
                <div class="task-item__info">
                  <div class="task-item__name">{{ task.taskName }}</div>
                  <div class="task-item__meta">
                    <el-tag size="small" type="info">{{ task.processType }}</el-tag>
                    <span class="text-muted">|</span>
                    <span>{{ task.applicantName }}</span>
                    <span class="text-muted">|</span>
                    <span>{{ task.departmentName }}</span>
                  </div>
                </div>
                <div class="task-item__time">{{ formatTime(task.createTime) }}</div>
              </div>
            </div>

            <!-- Empty State -->
            <el-empty v-if="allTasks.length === 0 && !taskLoading" description="暂无待办任务" :image-size="80" />
          </div>

          <!-- Focus Tab -->
          <div v-else class="focus-body">
            <el-empty description="暂无关注内容" :image-size="60" />
          </div>
        </div>
      </section>

      <!-- Right: Quick Links & Announcements -->
      <aside class="side-panel">
        <!-- Quick Links -->
        <div class="quick-links page-card">
          <div class="page-card__header">
            <h3><el-icon color="var(--accent)"><Link /></el-icon> 快捷入口</h3>
          </div>
          <div class="quick-links__grid">
            <div class="quick-link" @click="router.push('/arch/application')">
              <el-icon class="quick-link__icon"><Grid /></el-icon>
              <span>总行应用列表</span>
            </div>
            <div class="quick-link" @click="router.push('/arch/application')">
              <el-icon class="quick-link__icon"><Connection /></el-icon>
              <span>分行应用列表</span>
            </div>
            <div class="quick-link" @click="router.push('/oss/software')">
              <el-icon class="quick-link__icon"><Document /></el-icon>
              <span>内部标准</span>
            </div>
            <div class="quick-link" @click="router.push('/wf/todo')">
              <el-icon class="quick-link__icon"><Key /></el-icon>
              <span>权限申请</span>
            </div>
          </div>
        </div>

        <!-- Announcements -->
        <div class="announcements page-card">
          <div class="page-card__header">
            <h3><el-icon color="var(--warning)"><Bell /></el-icon> 消息公告</h3>
          </div>
          <div class="announcement-list">
            <div class="announcement-item">
              <el-icon color="var(--text-muted)"><Document /></el-icon>
              <span>操作手册指引 v2.1</span>
            </div>
            <div class="announcement-item">
              <el-icon color="var(--text-muted)"><Document /></el-icon>
              <span>维护指引手册</span>
            </div>
            <div class="announcement-item">
              <el-icon color="var(--text-muted)"><Document /></el-icon>
              <span>常见问题FAQ</span>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- Charts Section -->
    <div class="charts-grid">
      <div class="page-card animate-slide-up animate-delay-6">
        <div class="page-card__header">
          <h3><el-icon color="var(--accent)"><PieChart /></el-icon> 应用系统状态分布</h3>
        </div>
        <div ref="statusChartRef" style="height: 280px;"></div>
      </div>
      <div class="page-card animate-slide-up animate-delay-7">
        <div class="page-card__header">
          <h3><el-icon color="var(--accent)"><DataLine /></el-icon> 技术栈分布</h3>
        </div>
        <div ref="techChartRef" style="height: 280px;"></div>
      </div>
    </div>

    <!-- Recent Apps -->
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

    <!-- Department Chart -->
    <div class="page-card mt-lg animate-slide-up animate-delay-9">
      <div class="page-card__header">
        <h3><el-icon color="var(--accent)"><DataAnalysis /></el-icon> 部门资源分布</h3>
      </div>
      <div ref="deptChartRef" style="height: 260px;"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { reportApi, archAppApi, wfApi } from '@/api'
import {
  Box,
  Connection,
  Document,
  Cloudy,
  Grid,
  Tickets,
  PieChart,
  DataLine,
  Clock,
  ArrowRight,
  DataAnalysis,
  WarningFilled,
  Key,
  Star,
  Link,
  Bell
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const statusChartRef = ref(null)
const techChartRef = ref(null)
const deptChartRef = ref(null)
let statusChart = null, techChart = null, deptChart = null

const stats = reactive({ serverCount: 0, networkCount: 0, storageCount: 0, cloudCount: 0, appCount: 0, todoCount: 0 })
const recentApps = ref([])

// Task panel state
const activeTab = ref('todo')
const taskLoading = ref(false)
const allTasks = ref([])
const taskCategories = ref([])
const focusCount = ref(0)

const todoCount = computed(() => {
  return taskCategories.value.reduce((sum, cat) => sum + cat.count, 0)
})

const getPriorityClass = (priority) => {
  if (priority === 'URGENT') return 'danger'
  if (priority === 'HIGH') return 'warning'
  return 'info'
}

const formatTime = (time) => {
  if (!time) return ''
  return time.split(' ')[1] || time
}

const handleTaskClick = (task) => {
  router.push(`/wf/task/${task.id}`)
}

const loadTasks = async () => {
  taskLoading.value = true
  try {
    const res = await wfApi.todoList({ page: 1, pageSize: 999 })
    allTasks.value = res.data.records || []

    // Group by processType for counting
    const categoryMap = {}
    allTasks.value.forEach(task => {
      const type = task.processType || '其他'
      if (!categoryMap[type]) {
        categoryMap[type] = { processType: type, count: 0 }
      }
      categoryMap[type].count++
    })
    taskCategories.value = Object.values(categoryMap)
  } catch {
    allTasks.value = []
    taskCategories.value = []
  } finally {
    taskLoading.value = false
  }
}

const loadData = async () => {
  try {
    const [statsRes, appsRes] = await Promise.allSettled([
      reportApi.resourceStats(),
      archAppApi.list({ page: 1, pageSize: 5 })
    ])
    if (statsRes.status === 'fulfilled') {
      const d = statsRes.value.data || {}
      Object.assign(stats, {
        serverCount: d.serverCount || 0,
        networkCount: d.networkCount || 0,
        storageCount: d.storageCount || 0,
        cloudCount: d.cloudCount || 0,
        appCount: d.appCount || 0,
        todoCount: todoCount.value || d.todoCount || 0
      })
    }
    if (appsRes.status === 'fulfilled') recentApps.value = appsRes.value.data.records || []
  } catch {}
}

watch(todoCount, (newVal) => {
  stats.todoCount = newVal
})

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

onMounted(() => { loadTasks(); loadData(); initCharts(); window.addEventListener('resize', handleResize) })
onUnmounted(() => { window.removeEventListener('resize', handleResize); statusChart?.dispose(); techChart?.dispose(); deptChart?.dispose() })
</script>

<style scoped lang="scss">
// ====== Stats Strip ======
.stats-strip {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 12px;
  transition: all 0.25s ease;

  &:hover {
    border-color: var(--border-light);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  &__icon {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 12px;
    font-size: 22px;

    &--primary { background: rgba(59, 130, 246, 0.1); color: #3b82f6; }
    &--success { background: rgba(16, 185, 129, 0.1); color: #10b981; }
    &--warning { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
    &--info { background: rgba(6, 182, 212, 0.1); color: #06b6d4; }
    &--danger { background: rgba(239, 68, 68, 0.1); color: #ef4444; }
  }

  &__body {
    flex: 1;
    min-width: 0;
  }

  &__value {
    font-size: 1.5rem;
    font-weight: 700;
    color: var(--text-primary);
    line-height: 1.2;
  }

  &__label {
    font-size: 0.8125rem;
    color: var(--text-secondary);
    margin-top: 2px;
  }
}

// ====== Main Grid ======
.main-grid {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 24px;
  margin-bottom: 24px;
}

// ====== Task Section ======
.task-section {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
}

.task-tabs {
  display: flex;
  border-bottom: 1px solid var(--border);
  background: var(--bg-dark-2);
}

.task-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 24px;
  font-size: 0.9375rem;
  font-weight: 500;
  color: var(--text-secondary);
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    color: var(--text-primary);
    background: var(--bg-hover);
  }

  &--active {
    color: var(--accent);
    background: var(--bg-card);
    border-bottom-color: var(--accent);
  }

  &__badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 22px;
    height: 22px;
    padding: 0 8px;
    font-size: 0.75rem;
    font-weight: 600;
    color: #fff;
    background: var(--accent);
    border-radius: 11px;
  }
}

.task-content {
  min-height: 400px;
}

.task-body {
  padding: 16px;
}

// ====== All Tasks List (Flat) ======
.all-tasks-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 480px;
  overflow-y: auto;
  padding-right: 4px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: var(--bg-dark-2);
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--border);
    border-radius: 3px;

    &:hover {
      background: var(--text-muted);
    }
  }
}

// ====== Task Item ======
.task-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: var(--bg-dark-3);
  border: 1px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  animation: fadeInUp 0.25s ease-out backwards;

  &:hover {
    border-color: var(--accent);
    background: var(--bg-hover);
    transform: translateX(4px);
  }

  &__priority {
    width: 34px;
    height: 34px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    flex-shrink: 0;

    &--danger {
      background: rgba(239, 68, 68, 0.12);
      color: var(--danger);
    }

    &--warning {
      background: rgba(245, 158, 11, 0.12);
      color: var(--warning);
    }

    &--info {
      background: rgba(59, 130, 246, 0.12);
      color: #3b82f6;
    }
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-size: 0.875rem;
    font-weight: 500;
    color: var(--text-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    margin-bottom: 4px;
  }

  &__meta {
    font-size: 0.75rem;
    color: var(--text-secondary);
    display: flex;
    align-items: center;
    gap: 6px;
  }

  &__time {
    font-size: 0.75rem;
    font-family: 'SF Mono', 'Monaco', monospace;
    color: var(--text-muted);
    flex-shrink: 0;
  }
}

// ====== Focus Body ======
.focus-body {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

// ====== Side Panel ======
.side-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.quick-links__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 16px;
}

.quick-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  background: var(--bg-dark-3);
  border: 1px solid var(--border);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: var(--accent);
    background: var(--bg-hover);
    transform: translateY(-2px);
  }

  &__icon {
    font-size: 24px;
    color: var(--accent);
  }

  span {
    font-size: 0.8125rem;
    color: var(--text-secondary);
    text-align: center;
  }
}

.announcement-list {
  padding: 8px 16px 16px;
}

.announcement-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
  cursor: pointer;
  transition: color 0.2s ease;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    color: var(--accent);
  }

  span {
    font-size: 0.875rem;
    color: var(--text-secondary);
  }
}

// ====== Charts Grid ======
.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

// ====== Animations ======
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
