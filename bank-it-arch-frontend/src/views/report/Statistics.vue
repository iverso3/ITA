<template>
  <div class="report-statistics animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">统计报表</h1>
      <p class="page-header__desc">IT架构多维度统计分析</p>
    </div>
    <div class="stat-grid" style="grid-template-columns: repeat(4, 1fr);">
      <div class="stat-card stat-card--primary"><div class="stat-card__icon"><el-icon><Grid /></el-icon></div><div class="stat-card__value font-mono">{{ stats.total || 0 }}</div><div class="stat-card__label">应用系统总数</div></div>
      <div class="stat-card stat-card--success"><div class="stat-card__icon"><el-icon><Box /></el-icon></div><div class="stat-card__value font-mono">{{ stats.serverCount || 0 }}</div><div class="stat-card__label">服务器数量</div></div>
      <div class="stat-card stat-card--warning"><div class="stat-card__icon"><el-icon><Coin /></el-icon></div><div class="stat-card__value font-mono">{{ stats.techStackCount || 0 }}</div><div class="stat-card__label">技术栈种类</div></div>
      <div class="stat-card stat-card--info"><div class="stat-card__icon"><el-icon><DataLine /></el-icon></div><div class="stat-card__value font-mono">{{ stats.entityCount || 0 }}</div><div class="stat-card__label">数据实体</div></div>
    </div>
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-top: 24px;">
      <div class="page-card">
        <div class="page-card__header"><h3><el-icon color="var(--accent)"><PieChart /></el-icon> 应用类型分布</h3></div>
        <div ref="typeChartRef" style="height: 280px;"></div>
      </div>
      <div class="page-card">
        <div class="page-card__header"><h3><el-icon color="var(--accent)"><TrendCharts /></el-icon> 生命周期分布</h3></div>
        <div ref="lifecycleChartRef" style="height: 280px;"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { reportApi } from '@/api'

const typeChartRef = ref(null), lifecycleChartRef = ref(null)
let typeChart = null, lifecycleChart = null

const stats = reactive({ total: 0, serverCount: 0, techStackCount: 0, entityCount: 0 })

const loadData = async () => {
  try {
    const res = await reportApi.systemStats()
    const d = res.data || {}
    Object.assign(stats, {
      total: d.total || d.appCount || 0,
      serverCount: d.serverCount || 0,
      techStackCount: d.techStackCount || 0,
      entityCount: d.entityCount || 0
    })
    if (d.appTypeDistribution && typeChart) {
      const chartData = d.appTypeDistribution.map((item, idx) => ({
        name: item.name,
        value: item.value,
        itemStyle: { color: ['#e74c3c', '#3498db', '#2ecc71', '#f39c12', '#9b59b6', '#1abc9c'][idx % 6] }
      }))
      typeChart.setOption({ series: [{ data: chartData }] })
    }
    if (d.lifecycleDistribution && lifecycleChart) {
      const lcMap = { PLANNING: '规划中', DEVELOPMENT: '开发中', TESTING: '测试中', PRODUCTION: '生产运行', DEPRECATED: '已废弃' }
      const chartData = d.lifecycleDistribution.map((item, idx) => ({
        name: lcMap[item.name] || item.name,
        value: item.value,
        itemStyle: { color: ['#3498db', '#f39c12', '#9b59b6', '#2ecc71', '#95a5a6'][idx % 5] }
      }))
      lifecycleChart.setOption({
        xAxis: { data: chartData.map(d => d.name) },
        series: [{ data: chartData }]
      })
    }
  } catch (e) {
    console.error('Failed to load stats:', e)
  }
}

const initCharts = () => {
  typeChart = echarts.init(typeChartRef.value)
  typeChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', backgroundColor: 'var(--bg-card)', borderColor: 'var(--border)', textStyle: { color: 'var(--text-primary)' } },
    legend: { bottom: 0, textStyle: { color: 'var(--text-secondary)' } },
    series: [{ type: 'pie', radius: ['45%', '65%'], center: ['50%', '45%'], itemStyle: { borderRadius: 6, borderColor: 'var(--bg-card)', borderWidth: 2 }, label: { show: false }, emphasis: { label: { show: true, fontSize: 12, fontWeight: 'bold', color: 'var(--text-primary)' } }, data: [] }]
  })

  lifecycleChart = echarts.init(lifecycleChartRef.value)
  lifecycleChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: 'var(--bg-card)', borderColor: 'var(--border)', textStyle: { color: 'var(--text-primary)' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: { type: 'category', data: [], axisLine: { lineStyle: { color: 'var(--border)' } }, axisLabel: { color: 'var(--text-secondary)' } },
    yAxis: { type: 'value', axisLine: { lineStyle: { color: 'var(--border)' } }, axisLabel: { color: 'var(--text-muted)' }, splitLine: { lineStyle: { color: 'var(--border)', type: 'dashed' } } },
    series: [{ type: 'bar', data: [], barWidth: 32 }]
  })
}

onMounted(() => { initCharts(); loadData() })
onUnmounted(() => { typeChart?.dispose(); lifecycleChart?.dispose() })
</script>
