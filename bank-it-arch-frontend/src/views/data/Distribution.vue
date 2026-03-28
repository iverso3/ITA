<template>
  <div class="data-distribution animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">数据分布</h1>
      <p class="page-header__desc">数据在各系统间的分布情况</p>
    </div>
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
      <div class="page-card">
        <div class="page-card__header">
          <h3><el-icon color="var(--accent)"><PieChart /></el-icon> 数据类型分布</h3>
        </div>
        <div ref="typeChartRef" style="height: 350px;"></div>
      </div>
      <div class="page-card">
        <div class="page-card__header">
          <h3><el-icon color="var(--accent)"><Histogram /></el-icon> 数据量分布</h3>
        </div>
        <div ref="volumeChartRef" style="height: 350px;"></div>
      </div>
    </div>
    <div class="page-card" style="margin-top: 24px;">
      <div class="page-card__header">
        <h3><el-icon color="var(--accent)"><List /></el-icon> 分布详情</h3>
      </div>
      <el-table :data="distributionList" v-loading="loading" stripe>
        <el-table-column prop="appName" label="应用名称" min-width="160" />
        <el-table-column prop="entityCount" label="实体数量" width="120" align="center" />
        <el-table-column prop="dataVolume" label="数据量(GB)" width="140" align="right">
          <template #default="{ row }">
            <span class="font-mono">{{ formatVolume(row.dataVolume) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="storageType" label="存储类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.storageType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sensitivity" label="敏感等级分布" min-width="200">
          <template #default="{ row }">
            <div v-if="row.sensitivityDistribution" style="display: flex; gap: 8px; flex-wrap: wrap;">
              <el-tag v-if="row.sensitivityDistribution.HIGHLY_CONFIDENTIAL" size="small" type="danger">
                高度机密: {{ row.sensitivityDistribution.HIGHLY_CONFIDENTIAL }}
              </el-tag>
              <el-tag v-if="row.sensitivityDistribution.CONFIDENTIAL" size="small" type="warning">
                机密: {{ row.sensitivityDistribution.CONFIDENTIAL }}
              </el-tag>
              <el-tag v-if="row.sensitivityDistribution.INTERNAL" size="small" type="info">
                内部: {{ row.sensitivityDistribution.INTERNAL }}
              </el-tag>
              <el-tag v-if="row.sensitivityDistribution.PUBLIC" size="small">
                公开: {{ row.sensitivityDistribution.PUBLIC }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { reportApi } from '@/api'

const typeChartRef = ref(null)
const volumeChartRef = ref(null)
let typeChart = null
let volumeChart = null
const loading = ref(false)
const distributionList = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await reportApi.dataDistribution()
    if (res.data) {
      distributionList.value = res.data.details || []

      // 更新图表
      if (res.data.typeDistribution && typeChart) {
        const typeData = res.data.typeDistribution.map((item, idx) => ({
          name: item.name,
          value: item.value,
          itemStyle: { color: ['#3498db', '#e74c3c', '#2ecc71', '#f39c12', '#9b59b6', '#1abc9c'][idx % 6] }
        }))
        typeChart.setOption({
          series: [{ data: typeData }]
        })
      }

      if (res.data.volumeDistribution && volumeChart) {
        const volumeData = res.data.volumeDistribution
        volumeChart.setOption({
          xAxis: { data: volumeData.map(d => d.name) },
          series: [{
            data: volumeData.map((d, idx) => ({
              value: d.value,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#c9a962' },
                  { offset: 1, color: '#1a3a5c' }
                ])
              }
            }))
          }]
        })
      }
    }
  } catch (e) {
    console.error('Failed to load distribution data:', e)
  } finally {
    loading.value = false
  }
}

const initCharts = () => {
  typeChart = echarts.init(typeChartRef.value)
  typeChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', backgroundColor: 'var(--bg-card)', borderColor: 'var(--border)', textStyle: { color: 'var(--text-primary)' } },
    legend: { bottom: 0, textStyle: { color: 'var(--text-secondary)' } },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      itemStyle: { borderRadius: 6, borderColor: 'var(--bg-card)', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 12, fontWeight: 'bold', color: 'var(--text-primary)' } },
      data: []
    }]
  })

  volumeChart = echarts.init(volumeChartRef.value)
  volumeChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: 'var(--bg-card)', borderColor: 'var(--border)', textStyle: { color: 'var(--text-primary)' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: { type: 'category', axisLine: { lineStyle: { color: 'var(--border)' } }, axisLabel: { color: 'var(--text-muted)' }, splitLine: { show: false } },
    yAxis: { type: 'value', axisLine: { lineStyle: { color: 'var(--border)' } }, axisLabel: { color: 'var(--text-muted)' }, splitLine: { lineStyle: { color: 'var(--border)', type: 'dashed' } } },
    series: [{
      type: 'bar',
      barWidth: 40,
      data: [],
      itemStyle: { borderRadius: [4, 4, 0, 0] }
    }]
  })
}

const formatVolume = (volume) => {
  if (!volume) return '0'
  if (volume >= 1024) return (volume / 1024).toFixed(2) + ' TB'
  return volume + ' GB'
}

const handleResize = () => {
  typeChart?.resize()
  volumeChart?.resize()
}

onMounted(() => {
  initCharts()
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  typeChart?.dispose()
  volumeChart?.dispose()
})
</script>
