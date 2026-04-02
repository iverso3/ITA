<template>
  <div class="eval-indicators-tool">
    <el-table :data="indicators" border class="eval-table" :span-method="spanMethod">
      <el-table-column prop="level1" label="一级指标" width="120" />
      <el-table-column prop="level2" label="二级指标" width="180" />
      <el-table-column prop="criteria" label="评测标准" min-width="350" show-overflow-tooltip />
      <el-table-column prop="weight" label="分值权重" width="80" align="center" />
      <el-table-column label="评测结果" width="200" align="center">
        <template #default="{ row }">
          <el-select v-if="row.inputType === 'DROPDOWN'" v-model="row.result" placeholder="请选择" :disabled="readonly" class="eval-select" @change="handleChange(row)">
            <el-option v-for="opt in row.options" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-input-number v-else-if="row.inputType === 'NUMBER'" v-model="row.result" :min="0" :disabled="readonly" class="eval-input" @change="handleChange(row)" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="评测得分" width="80" align="center">
        <template #default="{ row }">
          <span class="score">{{ row.score || 0 }}</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="total-score">
      <span class="total-label">质量指标总分：</span>
      <span class="total-value">{{ totalScore }}</span>
      <span class="total-max">/ 100</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  category: { type: String, default: 'TOOL' },
  indicators: { type: Array, default: () => [] },
  readonly: { type: Boolean, default: false }
})

const emit = defineEmits(['update'])

// TOOL quality indicators (功能特性、安全性、易用性)
const indicators = ref([
  // 功能特性
  { level1: '功能特性', level2: '核心功能满足度', criteria: '核心功能满足应用功能需求程度', weight: 40, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '完全满足', value: '100', score: 40 },
    { label: '基本满足', value: '80', score: 32 },
    { label: '部分满足', value: '60', score: 24 },
    { label: '不满足', value: '0', score: 0 }
  ]},
  { level1: '功能特性', level2: '扩展功能满足度', criteria: '扩展功能满足应用功能需求程度', weight: 10, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '完全满足', value: '100', score: 10 },
    { label: '基本满足', value: '80', score: 8 },
    { label: '部分满足', value: '60', score: 6 },
    { label: '不满足', value: '0', score: 0 }
  ]},
  // 安全性
  { level1: '安全性', level2: '高危及以上漏洞数量', criteria: '已暴露的高危及以上安全漏洞数量', weight: 15, result: 0, score: 15, inputType: 'NUMBER' },
  { level1: '安全性', level2: '中危漏洞数量', criteria: '已暴露的中危安全漏洞数量', weight: 10, result: 0, score: 10, inputType: 'NUMBER' },
  { level1: '安全性', level2: '低危漏洞数量', criteria: '已暴露的低危安全漏洞数量', weight: 5, result: 0, score: 5, inputType: 'NUMBER' },
  { level1: '安全性', level2: '安全漏洞修复积极性', criteria: '最近发布的3个版本是否有新增漏洞及修复漏洞信息', weight: 5, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '无新增漏洞', value: 'NO_NEW', score: 5 },
    { label: '有新增但已修复', value: 'FIXED', score: 3 },
    { label: '有新增未修复', value: 'UNFIXED', score: 0 }
  ]},
  // 易用性
  { level1: '易用性', level2: '安装部署难易程度', criteria: '安装部署难易程度（如支持图形化安装部署、一键脚本安装部署、纯手工部署等）', weight: 15, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '图形化/一键部署', value: 'EASY', score: 15 },
    { label: '需手动配置', value: 'NORMAL', score: 10 },
    { label: '需编译构建', value: 'HARD', score: 5 }
  ]}
])

const totalScore = computed(() => {
  return indicators.value.reduce((sum, ind) => sum + (ind.score || 0), 0)
})

const handleChange = (row) => {
  if (row.inputType === 'NUMBER') {
    row.score = row.result === 0 ? row.weight : 0
  } else if (row.options) {
    const opt = row.options.find(o => o.value === row.result)
    row.score = opt ? opt.score : 0
  }
  emit('update', { indicators: indicators.value, totalScore: totalScore.value })
}

const spanMethod = ({ row, column, rowIndex, columnIndex }) => {
  if (columnIndex === 0) {
    // 找到当前一级指标的起始行
    let startRow = rowIndex
    while (startRow > 0 && indicators.value[startRow - 1].level1 === row.level1) {
      startRow--
    }
    // 计算该一级指标跨多少行
    let rowspan = 0
    for (let i = startRow; i < indicators.value.length; i++) {
      if (indicators.value[i].level1 === row.level1) {
        rowspan++
      } else {
        break
      }
    }
    // 如果当前行不是该组的第一行，返回0使单元格消失
    if (rowIndex > startRow) {
      return { rowspan: 0, colspan: 0 }
    }
    return { rowspan, colspan: 1 }
  }
}

watch(() => props.indicators, (val) => {
  if (val && val.length > 0) {
    // Merge with external indicators if provided
  }
}, { immediate: true })
</script>

<style scoped>
.eval-indicators-tool {
  padding: 0 16px;
}

.eval-table {
  margin-bottom: 12px;
}

.eval-select {
  width: 100%;
}

.eval-input {
  width: 100%;
}

.score {
  font-weight: 600;
  color: #409eff;
}

.total-score {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 12px 0;
  gap: 8px;
}

.total-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.total-value {
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
}

.total-max {
  font-size: 14px;
  color: #909399;
}
</style>
