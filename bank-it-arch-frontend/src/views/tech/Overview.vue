<template>
  <div class="tech-overview animate-fade-in">
    <div class="page-header">
      <h1 class="page-header__title">技术架构管理</h1>
      <p class="page-header__desc">全行技术栈分布与技术选型规范</p>
    </div>
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 24px;">
      <div class="page-card">
        <div class="page-card__header"><h3><el-icon color="var(--accent)"><Coin /></el-icon> 技术栈分布</h3></div>
        <div ref="stackChartRef" style="height: 280px;"></div>
      </div>
      <div class="page-card">
        <div class="page-card__header"><h3><el-icon color="var(--accent)"><PieChart /></el-icon> 数据库类型分布</h3></div>
        <div ref="dbChartRef" style="height: 280px;"></div>
      </div>
    </div>
    <div class="page-card">
      <div class="page-card__header"><h3><el-icon color="var(--accent)"><List /></el-icon> 技术栈清单</h3><el-button type="primary" size="small" @click="handleAdd">新增技术栈</el-button></div>
      <el-table :data="techStackList" v-loading="loading" stripe>
        <el-table-column prop="stackName" label="技术名称" min-width="160" />
        <el-table-column prop="stackType" label="类型" width="120"><template #default="{ row }"><el-tag size="small">{{ row.stackType }}</el-tag></template></el-table-column>
        <el-table-column prop="version" label="版本" width="100" />
        <el-table-column prop="vendor" label="供应商" width="140" />
        <el-table-column prop="appCount" label="应用数量" width="100" align="center" />
        <el-table-column prop="isStandard" label="标准技术" width="100" align="center">
          <template #default="{ row }"><el-tag size="small" :type="row.isStandard ? 'success' : 'info'">{{ row.isStandard ? '是' : '否' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="80" align="center">
          <template #default="{ row }"><span class="status-badge" :class="'status-badge--' + (row.isActive ? 'online' : 'offline')">{{ row.isActive ? '启用' : '停用' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增技术栈' : '编辑技术栈'" width="500px" destroy-on-close>
      <el-form :model="techForm" label-width="100px">
        <el-form-item label="编码"><el-input v-model="techForm.stackCode" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="techForm.stackName" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="techForm.stackType" style="width:100%;"><el-option label="语言" value="语言" /><el-option label="框架" value="框架" /><el-option label="中间件" value="中间件" /><el-option label="数据库" value="数据库" /><el-option label="操作系统" value="操作系统" /><el-option label="其他" value="其他" /></el-select></el-form-item>
        <el-form-item label="版本"><el-input v-model="techForm.version" /></el-form-item>
        <el-form-item label="供应商"><el-input v-model="techForm.vendor" /></el-form-item>
        <el-form-item label="标准技术"><el-switch v-model="techForm.isStandard" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="techForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { techStackApi } from '@/api'

const stackChartRef = ref(null)
const dbChartRef = ref(null)
let stackChart = null, dbChart = null
const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('add')
const techForm = ref({ id: null, stackCode: '', stackName: '', stackType: '语言', version: '', vendor: '', isStandard: 0, isActive: 1, description: '' })

const techStackList = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await techStackApi.list({ page: 1, pageSize: 100 })
    techStackList.value = res.data.records || []
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const handleAdd = () => { dialogMode.value = 'add'; techForm.value = { id: null, stackCode: '', stackName: '', stackType: '语言', version: '', vendor: '', isStandard: 0, isActive: 1, description: '' }; dialogVisible.value = true }
const handleEdit = (row) => { dialogMode.value = 'edit'; techForm.value = { ...row }; dialogVisible.value = true }
const handleSubmit = async () => { try { if (dialogMode.value === 'add') await techStackApi.create(techForm.value); else await techStackApi.update(techForm.value.id, techForm.value); ElMessage.success('操作成功'); dialogVisible.value = false; loadData() } catch { ElMessage.error('操作失败') } }
const handleDelete = async (row) => { try { await ElMessageBox.confirm(`确定删除 ${row.stackName}？`, '确认', { type: 'warning' }); await techStackApi.delete(row.id); ElMessage.success('删除成功'); loadData() } catch {} }

const initCharts = () => {
  stackChart = echarts.init(stackChartRef.value)
  stackChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: 'var(--bg-card)', borderColor: 'var(--border)', textStyle: { color: 'var(--text-primary)' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: { type: 'value', axisLine: { lineStyle: { color: 'var(--border)' } }, axisLabel: { color: 'var(--text-muted)' }, splitLine: { lineStyle: { color: 'var(--border)', type: 'dashed' } } },
    yAxis: { type: 'category', data: ['Java', 'Spring Boot', 'MySQL', 'Redis', 'Vue.js', 'k8s'], axisLine: { lineStyle: { color: 'var(--border)' } }, axisLabel: { color: 'var(--text-secondary)' } },
    series: [{ type: 'bar', data: [89, 76, 65, 54, 43, 32], itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#c9a962' }, { offset: 1, color: '#2d5a8a' }]), borderRadius: [0, 4, 4, 0] }, barWidth: 20 }]
  })

  dbChart = echarts.init(dbChartRef.value)
  dbChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', backgroundColor: 'var(--bg-card)', borderColor: 'var(--border)', textStyle: { color: 'var(--text-primary)' } },
    legend: { bottom: 0, textStyle: { color: 'var(--text-secondary)' } },
    series: [{ type: 'pie', radius: ['45%', '70%'], center: ['50%', '45%'], itemStyle: { borderRadius: 6, borderColor: 'var(--bg-card)', borderWidth: 2 }, label: { show: false }, emphasis: { label: { show: true, fontSize: 12, fontWeight: 'bold', color: 'var(--text-primary)' } }, data: [{ name: 'MySQL', value: 65, itemStyle: { color: '#3498db' } }, { name: 'Oracle', value: 25, itemStyle: { color: '#e74c3c' } }, { name: 'PostgreSQL', value: 20, itemStyle: { color: '#2ecc71' } }, { name: 'MongoDB', value: 15, itemStyle: { color: '#9b59b6' } }, { name: '其他', value: 10, itemStyle: { color: '#95a5a6' } }] }]
  })
}

onMounted(() => { loadData(); initCharts() })
onUnmounted(() => { stackChart?.dispose(); dbChart?.dispose() })
</script>
