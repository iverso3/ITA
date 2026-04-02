<template>
  <div class="eval-indicators-cmpnt">
    <el-table :data="indicators" border class="eval-table">
      <el-table-column prop="level1" label="一级指标" width="120" />
      <el-table-column prop="level2" label="二级指标" width="180" />
      <el-table-column prop="criteria" label="评测标准" min-width="350" show-overflow-tooltip />
      <el-table-column prop="weight" label="分值权重" width="80" align="center" />
      <el-table-column label="评测结果" width="200" align="center">
        <template #default="{ row }">
          <el-select v-model="row.result" placeholder="请选择" :disabled="readonly" class="eval-select" @change="handleChange(row)">
            <el-option v-for="opt in row.options" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
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
  category: { type: String, default: 'CMPNT' },
  indicators: { type: Array, default: () => [] },
  readonly: { type: Boolean, default: false }
})

const emit = defineEmits(['update'])

// CMPNT quality indicators (仅功能特性 - 核心功能满足度)
const indicators = ref([
  { level1: '功能特性', level2: '核心功能满足度', criteria: '功能满足应用功能需求程度', weight: 100, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '完全不满足', value: '0', score: 0 },
    { label: '20%满足', value: '20', score: 20 },
    { label: '40%满足', value: '40', score: 40 },
    { label: '60%满足', value: '60', score: 60 },
    { label: '80%满足', value: '80', score: 80 },
    { label: '100%满足', value: '100', score: 100 }
  ]}
])

const totalScore = computed(() => {
  return indicators.value.reduce((sum, ind) => sum + (ind.score || 0), 0)
})

const handleChange = (row) => {
  const opt = row.options.find(o => o.value === row.result)
  row.score = opt ? opt.score : 0
  emit('update', { indicators: indicators.value, totalScore: totalScore.value })
}

watch(() => props.indicators, (val) => {
  if (val && val.length > 0) {
    // Merge with external indicators if provided
  }
}, { immediate: true })
</script>

<style scoped>
.eval-indicators-cmpnt {
  padding: 0 16px;
}

.eval-table {
  margin-bottom: 12px;
}

.eval-select {
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
