<template>
  <div class="report-panorama animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">全景图</h1>
      <p class="page-header__desc">IT架构全景可视化展示</p>
    </div>
    <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px;">
      <div class="page-card">
        <div class="page-card__header"><h3><el-icon color="var(--accent)"><FullScreen /></el-icon> 系统全景图</h3><el-button type="primary" text size="small">全屏</el-button></div>
        <div ref="systemChartRef" style="height: 350px;"></div>
      </div>
      <div class="page-card">
        <div class="page-card__header"><h3><el-icon color="var(--accent)"><Coin /></el-icon> 技术栈全景</h3><el-button type="primary" text size="small">全屏</el-button></div>
        <div ref="techChartRef" style="height: 350px;"></div>
      </div>
      <div class="page-card">
        <div class="page-card__header"><h3><el-icon color="var(--accent)"><DataAnalysis /></el-icon> 数据分布全景</h3><el-button type="primary" text size="small">全屏</el-button></div>
        <div ref="dataChartRef" style="height: 350px;"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { reportApi } from '@/api'

const systemChartRef = ref(null), techChartRef = ref(null), dataChartRef = ref(null)
let systemChart = null, techChart = null, dataChart = null

const loadData = async () => {
  try {
    // System panorama data
    const sysRes = await reportApi.systemPanorama()
    if (sysRes.data && sysRes.data.nodes && systemChart) {
      const nodes = sysRes.data.nodes || []
      const typeGroups = {}
      nodes.forEach(n => {
        const group = n.group || '其他'
        if (!typeGroups[group]) typeGroups[group] = 0
        typeGroups[group]++
      })
      const sunburstData = {
        name: '全行系统',
        children: Object.entries(typeGroups).map(([name, value]) => ({
          name: `${name}(${value})`,
          value,
          itemStyle: { color: ['#e74c3c', '#3498db', '#2ecc71', '#f39c12', '#9b59b6', '#1abc9c'][Math.abs(name.charCodeAt(0)) % 6] }
        }))
      }
      systemChart.setOption({
        series: [{ data: [sunburstData] }]
      })
    }

    // Tech stack distribution
    const techRes = await reportApi.techDistribution()
    if (techRes.data && techRes.data.distribution && techChart) {
      const dist = techRes.data.distribution.map((item, idx) => ({
        name: item.name + '\n' + item.value,
        value: item.value,
        itemStyle: { color: ['#e74c3c', '#3498db', '#2ecc71', '#f39c12', '#9b59b6', '#1abc9c'][idx % 6] }
      }))
      techChart.setOption({
        series: [{ data: dist }]
      })
    }

    // Data distribution
    const dataRes = await reportApi.dataDistribution()
    if (dataRes.data && dataRes.data.distribution && dataChart) {
      const dist = dataRes.data.distribution
      dataChart.setOption({
        xAxis: { data: dist.map(d => d.name) },
        series: [{ data: dist.map((d, idx) => ({
          value: d.value,
          itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#c9a962' }, { offset: 1, color: '#1a3a5c' }]) }
        })) }]
      })
    }
  } catch (e) {
    console.error('Failed to load panorama data:', e)
  }
}

const initCharts = () => {
  systemChart = echarts.init(systemChartRef.value)
  systemChart.setOption({
    backgroundColor: 'transparent', tooltip: { trigger: 'item', backgroundColor: 'var(--bg-card)', borderColor: 'var(--border)', textStyle: { color: 'var(--text-primary)' } },
    series: [{ type: 'sunburst', data: [], radius: ['20%', '90%'], label: { rotate: 'radial', color: 'var(--text-primary)' }, itemStyle: { borderRadius: 4, borderColor: 'var(--bg-card)', borderWidth: 2 } }]
  })

  techChart = echarts.init(techChartRef.value)
  techChart.setOption({
    backgroundColor: 'transparent', tooltip: { trigger: 'item', backgroundColor: 'var(--bg-card)', borderColor: 'var(--border)', textStyle: { color: 'var(--text-primary)' } },
    series: [{ type: 'treemap', data: [], label: { show: true, formatter: '{b}', color: '#fff', fontSize: 11 }, itemStyle: { borderRadius: 4, borderColor: 'var(--bg-card)', borderWidth: 2 } }]
  })

  dataChart = echarts.init(dataChartRef.value)
  dataChart.setOption({
    backgroundColor: 'transparent', tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: 'var(--bg-card)', borderColor: 'var(--border)', textStyle: { color: 'var(--text-primary)' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: { type: 'category', data: [], axisLine: { lineStyle: { color: 'var(--border)' } }, axisLabel: { color: 'var(--text-secondary)' } },
    yAxis: { type: 'value', name: '数量', axisLine: { lineStyle: { color: 'var(--border)' } }, axisLabel: { color: 'var(--text-muted)' }, splitLine: { lineStyle: { color: 'var(--border)', type: 'dashed' } } },
    series: [{ type: 'bar', data: [], barWidth: 28 }]
  })
}

onMounted(() => { initCharts(); loadData() })
onUnmounted(() => { systemChart?.dispose(); techChart?.dispose(); dataChart?.dispose() })
</script>
