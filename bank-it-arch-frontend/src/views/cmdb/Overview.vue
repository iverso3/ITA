<template>
  <div class="cmdb-overview animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">CMDB概览</h1>
      <p class="page-header__desc">配置管理数据库 - 全行IT资产统一视图</p>
    </div>

    <!-- Summary Stats -->
    <div class="stat-grid">
      <div class="stat-card stat-card--primary">
        <div class="stat-card__icon"><el-icon><Box /></el-icon></div>
        <div class="stat-card__value font-mono">{{ stats.servers }}</div>
        <div class="stat-card__label">服务器</div>
      </div>
      <div class="stat-card stat-card--success">
        <div class="stat-card__icon"><el-icon><Connection /></el-icon></div>
        <div class="stat-card__value font-mono">{{ stats.networks }}</div>
        <div class="stat-card__label">网络设备</div>
      </div>
      <div class="stat-card stat-card--warning">
        <div class="stat-card__icon"><el-icon><Document /></el-icon></div>
        <div class="stat-card__value font-mono">{{ stats.storages }}</div>
        <div class="stat-card__label">存储设备</div>
      </div>
      <div class="stat-card stat-card--info">
        <div class="stat-card__icon"><el-icon><Cloudy /></el-icon></div>
        <div class="stat-card__value font-mono">{{ stats.clouds }}</div>
        <div class="stat-card__label">云资源</div>
      </div>
    </div>

    <!-- Distribution Charts -->
    <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; margin-bottom: 24px;">
      <div class="page-card" v-for="(item, index) in resourceTypes" :key="index">
        <div style="text-align: center;">
          <div :ref="el => chartRefs[index] = el" style="height: 180px;"></div>
          <h4 style="margin-top: 12px; color: var(--text-primary);">{{ item.title }}</h4>
          <p class="text-secondary" style="font-size: 0.875rem; margin-top: 4px;">{{ item.subtitle }}</p>
        </div>
      </div>
    </div>

    <!-- Resource Status -->
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
      <div class="page-card">
        <div class="page-card__header">
          <h3><el-icon color="var(--accent)"><Clock /></el-icon> 资产变更记录</h3>
          <el-button type="primary" text>查看全部</el-button>
        </div>
        <el-table :data="changeLogs">
          <el-table-column prop="assetCode" label="资产编码" width="160">
            <template #default="{ row }">
              <code style="color: var(--accent); font-size: 0.8125rem;">{{ row.assetCode }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="operateType" label="操作" width="80">
            <template #default="{ row }">
              <el-tag :type="row.operateType === 'CREATE' ? 'success' : row.operateType === 'DELETE' ? 'danger' : 'warning'" size="small">
                {{ row.operateType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="100" />
          <el-table-column prop="operateTime" label="时间" />
        </el-table>
      </div>

      <div class="page-card">
        <div class="page-card__header">
          <h3><el-icon color="var(--accent)"><Warning /></el-icon> 告警与提醒</h3>
        </div>
        <div class="alert-list">
          <div v-for="alert in alerts" :key="alert.id" class="alert-item" :class="'alert-item--' + alert.level">
            <div class="alert-item__icon">
              <el-icon><WarningFilled v-if="alert.level === 'warning'" /><CircleCloseFilled v-else /></el-icon>
            </div>
            <div class="alert-item__content">
              <div class="alert-item__title">{{ alert.title }}</div>
              <div class="alert-item__desc">{{ alert.description }}</div>
            </div>
            <div class="alert-item__time">{{ alert.time }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { reportApi } from '@/api'

const route = useRoute()
const chartRefs = ref([])
const stats = reactive({ servers: 0, networks: 0, storages: 0, clouds: 0 })

const loadData = async () => {
  try {
    const res = await reportApi.resourceStats()
    const d = res.data || {}
    stats.servers = d.serverCount || 0
    stats.networks = d.networkCount || 0
    stats.storages = d.storageCount || 0
    stats.clouds = d.cloudResourceCount || 0
  } catch {}
}

const resourceTypes = [
  { title: '服务器状态', subtitle: '按运行状态分布', data: [
    { name: '运行中', value: 1150, color: '#2ecc71' },
    { name: '维护中', value: 80, color: '#f39c12' },
    { name: '离线', value: 54, color: '#95a5a6' }
  ]},
  { title: '网络设备类型', subtitle: '按设备类型分布', data: [
    { name: '交换机', value: 280, color: '#3498db' },
    { name: '路由器', value: 96, color: '#9b59b6' },
    { name: '防火墙', value: 50, color: '#e74c3c' },
    { name: '负载均衡', value: 30, color: '#1abc9c' }
  ]},
  { title: '存储容量使用', subtitle: '总体使用率 68%', data: [
    { name: '已使用', value: 68, color: '#2ecc71' },
    { name: '可用', value: 32, color: '#34495e' }
  ]},
  { title: '云资源类型', subtitle: '按资源类型分布', data: [
    { name: '云主机', value: 1500, color: '#3498db' },
    { name: '云数据库', value: 380, color: '#9b59b6' },
    { name: '云存储', value: 200, color: '#e67e22' },
    { name: '其他', value: 76, color: '#95a5a6' }
  ]}
]

const changeLogs = ref([
  { assetCode: 'ASSET-SERVER-01284', operateType: 'CREATE', operator: '张三', operateTime: '2026-03-26 10:30' },
  { assetCode: 'ASSET-NETWORK-00456', operateType: 'UPDATE', operator: '李四', operateTime: '2026-03-26 09:15' },
  { assetCode: 'ASSET-CLOUD-02156', operateType: 'UPDATE', operator: '王五', operateTime: '2026-03-25 16:40' },
  { assetCode: 'ASSET-SERVER-01283', operateType: 'DELETE', operator: '赵六', operateTime: '2026-03-25 14:20' }
])

const alerts = ref([
  { id: 1, level: 'warning', title: '存储容量预警', description: '主数据中心存储使用率超过80%', time: '1小时前' },
  { id: 2, level: 'error', title: '服务器离线告警', description: '10.10.10.25 服务器连接中断', time: '2小时前' },
  { id: 3, level: 'warning', title: '证书即将过期', description: '3台服务器的SSL证书将在30天内过期', time: '5小时前' },
  { id: 4, level: 'info', title: '资源变更提醒', description: '信贷部门申请新增3台云主机', time: '1天前' }
])

let charts = []

const initCharts = () => {
  chartRefs.value.forEach((el, index) => {
    if (!el) return
    const chart = echarts.init(el)
    const item = resourceTypes[index]

    if (index === 2) {
      // Storage - gauge style
      chart.setOption({
        backgroundColor: 'transparent',
        series: [{
          type: 'gauge',
          startAngle: 200,
          endAngle: -20,
          radius: '90%',
          center: ['50%', '60%'],
          progress: { show: true, width: 12, itemStyle: { color: item.data[0].color } },
          pointer: { show: false },
          axisLine: { lineStyle: { width: 12, color: [[1, 'var(--border)']] } },
          axisTick: { show: false },
          splitLine: { show: false },
          axisLabel: { show: false },
          data: [{ value: item.data[0].value, name: '' }],
          title: { show: false },
          detail: {
            valueAnimation: true,
            fontSize: 28,
            fontWeight: 'bold',
            fontFamily: 'JetBrains Mono',
            color: item.data[0].color,
            offsetCenter: [0, '10%'],
            formatter: '{value}%'
          }
        }]
      })
    } else {
      chart.setOption({
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'item',
          backgroundColor: 'var(--bg-card)',
          borderColor: 'var(--border)',
          textStyle: { color: 'var(--text-primary)' }
        },
        series: [{
          type: 'pie',
          radius: ['50%', '75%'],
          center: ['50%', '45%'],
          itemStyle: { borderRadius: 6, borderColor: 'var(--bg-card)', borderWidth: 2 },
          label: { show: false },
          data: item.data
        }]
      })
    }
    charts.push(chart)
  })
}

const handleResize = () => {
  charts.forEach(chart => chart?.resize())
}

// 监听路由变化，强制重新加载数据
watch(() => route.path, () => { loadData(); initCharts() })

onMounted(() => {
  loadData()
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach(chart => chart?.dispose())
})
</script>

<style scoped lang="scss">
.alert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: var(--bg-dark-3);
  border-radius: 8px;
  border-left: 3px solid;

  &--warning {
    border-left-color: var(--warning);
    .alert-item__icon { color: var(--warning); }
  }

  &--error {
    border-left-color: var(--danger);
    .alert-item__icon { color: var(--danger); }
  }

  &--info {
    border-left-color: var(--info);
    .alert-item__icon { color: var(--info); }
  }

  &__icon {
    font-size: 1.25rem;
    margin-top: 2px;
  }

  &__title {
    font-weight: 500;
    color: var(--text-primary);
    margin-bottom: 2px;
  }

  &__desc {
    font-size: 0.8125rem;
    color: var(--text-secondary);
  }

  &__time {
    margin-left: auto;
    font-size: 0.75rem;
    color: var(--text-muted);
    white-space: nowrap;
  }
}
</style>
