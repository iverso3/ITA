<template>
  <div class="eval-indicators-base">
    <el-table :data="indicators" border class="eval-table" :span-method="spanMethod">
      <el-table-column prop="level1" label="一级指标" width="100" />
      <el-table-column prop="level2" label="二级指标" width="150" />
      <el-table-column prop="criteria" label="指标说明" min-width="220" show-overflow-tooltip />
      <el-table-column prop="weight" label="分值权重" width="70" align="center" />
      <el-table-column label="评测结果" width="150" align="center">
        <template #default="{ row }">
          <el-select v-if="row.inputType === 'DROPDOWN'" v-model="row.result" placeholder="请选择" :disabled="readonly" class="eval-select" @change="handleChange(row)">
            <el-option v-for="opt in row.options" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-input-number v-else-if="row.inputType === 'NUMBER'" v-model="row.result" :min="0" :max="99999" :disabled="readonly" class="eval-input" @change="handleChange(row)" controls-position="right" size="small" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="评测得分" width="70" align="center">
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
  category: { type: String, default: 'BASE' },
  indicators: { type: Array, default: () => [] },
  readonly: { type: Boolean, default: false }
})

const emit = defineEmits(['update'])

const indicators = ref([
  // 行业认可度 (Industry Recognition) - 15分
  { level1: '行业认可度', level2: '同业使用的数量', criteria: '开源软件同业使用的数量', weight: 15, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '5家以下', value: 'LESS_5', score: 3 },
    { label: '5-10家', value: '5_10', score: 7 },
    { label: '10-20家', value: '10_20', score: 11 },
    { label: '20家以上', value: 'MORE_20', score: 15 }
  ]},
  // 产品活跃度 (Product Activity) - 18分
  { level1: '产品活跃度', level2: '版本发布周期间隔', criteria: '最近3个版本的发布周期间隔是否间隔3个月以内', weight: 3, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '是', value: 'YES', score: 3 },
    { label: '否', value: 'NO', score: 0 }
  ]},
  { level1: '产品活跃度', level2: '代码提交次数', criteria: '近一个月GitHub所有分支提交次数', weight: 3, result: 0, score: 0, inputType: 'NUMBER', options: [] },
  { level1: '产品活跃度', level2: 'GitHub贡献者数量', criteria: '近1年每季度贡献者数量是否呈上升趋势', weight: 3, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '是', value: 'YES', score: 3 },
    { label: '否', value: 'NO', score: 0 }
  ]},
  { level1: '产品活跃度', level2: 'GitHub Stars', criteria: 'GitHub上的星数量', weight: 3, result: 0, score: 0, inputType: 'NUMBER', options: [] },
  { level1: '产品活跃度', level2: 'GitHub Forks', criteria: 'GitHub上的拷贝数量', weight: 3, result: 0, score: 0, inputType: 'NUMBER', options: [] },
  { level1: '产品活跃度', level2: 'GitHub Watching', criteria: 'GitHub上的关注数量', weight: 3, result: 0, score: 0, inputType: 'NUMBER', options: [] },
  // 服务支持 (Service Support) - 7分
  { level1: '服务支持', level2: '商业支持情况', criteria: '是否有商业支持', weight: 3, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '有', value: 'YES', score: 3 },
    { label: '无', value: 'NO', score: 0 }
  ]},
  { level1: '服务支持', level2: '官方或社区文档情况', criteria: '官方文档或社区文档是否丰富', weight: 4, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '丰富', value: 'RICH', score: 4 },
    { label: '一般', value: 'NORMAL', score: 2 },
    { label: '匮乏', value: 'POOR', score: 0 }
  ]},
  // 功能特性 (Functional Features) - 20分
  { level1: '功能特性', level2: '核心功能满足度', criteria: '核心功能满足应用功能需求程度', weight: 15, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '完全满足', value: '100', score: 15 },
    { label: '基本满足', value: '80', score: 12 },
    { label: '部分满足', value: '60', score: 9 },
    { label: '不满足', value: '0', score: 0 }
  ]},
  { level1: '功能特性', level2: '扩展功能满足度', criteria: '扩展功能满足应用功能需求程度', weight: 5, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '完全满足', value: '100', score: 5 },
    { label: '基本满足', value: '80', score: 4 },
    { label: '部分满足', value: '60', score: 3 },
    { label: '不满足', value: '0', score: 0 }
  ]},
  // 安全性 (Security) - 14分
  { level1: '安全性', level2: '高危及以上漏洞数量', criteria: '已暴露的高危及以上安全漏洞数量', weight: 5, result: 0, score: 5, inputType: 'NUMBER', options: [] },
  { level1: '安全性', level2: '中危漏洞数量', criteria: '已暴露的中危安全漏洞数量', weight: 3, result: 0, score: 3, inputType: 'NUMBER', options: [] },
  { level1: '安全性', level2: '低危漏洞数量', criteria: '已暴露的低危安全漏洞数量', weight: 3, result: 0, score: 3, inputType: 'NUMBER', options: [] },
  { level1: '安全性', level2: '安全漏洞修复积极性', criteria: '最近发布的3个版本是否有新增漏洞及修复漏洞信息', weight: 3, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '无新增漏洞', value: 'NO_NEW', score: 3 },
    { label: '有新增但已修复', value: 'FIXED', score: 2 },
    { label: '有新增未修复', value: 'UNFIXED', score: 0 }
  ]},
  // 可靠性 (Reliability) - 4分
  { level1: '可靠性', level2: '容错能力', criteria: '在部分节点宕机后是否仍可提供服务', weight: 2, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '是', value: 'YES', score: 2 },
    { label: '否', value: 'NO', score: 0 }
  ]},
  { level1: '可靠性', level2: '数据恢复能力', criteria: '故障下是否可保证数据一致性和防止数据丢失', weight: 2, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '是', value: 'YES', score: 2 },
    { label: '否', value: 'NO', score: 0 }
  ]},
  // 兼容性 (Compatibility) - 4分
  { level1: '兼容性', level2: '国产硬件兼容性', criteria: '在国产硬件上功能是否正常、运行稳定', weight: 2, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '完全兼容', value: 'FULL', score: 2 },
    { label: '部分兼容', value: 'PARTIAL', score: 1 },
    { label: '不兼容', value: 'NO', score: 0 }
  ]},
  { level1: '兼容性', level2: '国产操作系统兼容性', criteria: '在国产操作系统上功能是否正常、运行稳定', weight: 2, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '完全兼容', value: 'FULL', score: 2 },
    { label: '部分兼容', value: 'PARTIAL', score: 1 },
    { label: '不兼容', value: 'NO', score: 0 }
  ]},
  // 性能效率 (Performance Efficiency) - 5分
  { level1: '性能效率', level2: '资源使用情况', criteria: '压力测试下TPS、QPS、平均响应时间、最大响应时间、最大并发、服务调用成功率(CPU/内存/带宽使用率及I/O满足程度)', weight: 5, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '优秀', value: 'EXCELLENT', score: 5 },
    { label: '良好', value: 'GOOD', score: 4 },
    { label: '一般', value: 'NORMAL', score: 2 },
    { label: '较差', value: 'POOR', score: 0 }
  ]},
  // 可维护性 (Maintainability) - 5分
  { level1: '可维护性', level2: '在线变更能力', criteria: '关键配置参数是否支持在线变更', weight: 2, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '支持', value: 'YES', score: 2 },
    { label: '不支持', value: 'NO', score: 0 }
  ]},
  { level1: '可维护性', level2: '运行态监控', criteria: '是否提供Service监控服务或监控接口', weight: 3, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '提供', value: 'YES', score: 3 },
    { label: '不提供', value: 'NO', score: 0 }
  ]},
  // 可扩展性 (Scalability) - 4分
  { level1: '可扩展性', level2: '水平扩展能力', criteria: '是否支持大规模集群或水平扩展', weight: 2, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '支持', value: 'YES', score: 2 },
    { label: '不支持', value: 'NO', score: 0 }
  ]},
  { level1: '可扩展性', level2: '动态扩展能力', criteria: '是否支持动态扩缩容', weight: 2, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '支持', value: 'YES', score: 2 },
    { label: '不支持', value: 'NO', score: 0 }
  ]},
  // 易用性 (Usability) - 5分
  { level1: '易用性', level2: '应用示例情况', criteria: '重要场景是否提供完整的示例代码', weight: 2, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '有完整示例', value: 'COMPLETE', score: 2 },
    { label: '有部分示例', value: 'PARTIAL', score: 1 },
    { label: '无示例', value: 'NONE', score: 0 }
  ]},
  { level1: '易用性', level2: '安装部署难易程度', criteria: '图形化/一键脚本安装 vs 纯手动部署', weight: 3, result: null, score: 0, inputType: 'DROPDOWN', options: [
    { label: '图形化/一键部署', value: 'EASY', score: 3 },
    { label: '需手动配置', value: 'NORMAL', score: 2 },
    { label: '需编译构建', value: 'HARD', score: 0 }
  ]}
])

const totalScore = computed(() => {
  return indicators.value.reduce((sum, ind) => sum + (ind.score || 0), 0)
})

const handleChange = (row) => {
  if (row.inputType === 'NUMBER') {
    row.score = (row.result === 0 || row.result === null) ? row.weight : 0
  } else if (row.options && Array.isArray(row.options)) {
    const opt = row.options.find(o => o.value === row.result)
    row.score = opt ? opt.score : 0
  }
  emit('update', buildEvalData())
}

const buildEvalData = () => {
  return {
    indicators: JSON.parse(JSON.stringify(indicators.value || [])),
    totalScore: totalScore.value,
    category: props.category
  }
}

watch(() => props.indicators, (val) => {
  if (val && val.length > 0 && Array.isArray(val)) {
    val.forEach((ext, idx) => {
      if (indicators.value[idx]) {
        indicators.value[idx].result = ext.result
        indicators.value[idx].score = ext.score
      }
    })
  }
}, { immediate: true })

watch(() => props.readonly, (val) => {
  if (val) {
    emit('update', buildEvalData())
  }
})

const spanMethod = ({ row, column, rowIndex, columnIndex }) => {
  if (columnIndex === 0) {
    const currentLevel1 = row.level1
    let rowspan = 0
    for (let i = rowIndex; i < indicators.value.length; i++) {
      if (indicators.value[i].level1 === currentLevel1) {
        rowspan++
      } else {
        break
      }
    }
    // 检查当前一级指标之前出现了多少行
    let startRospan = 0
    for (let i = 0; i < rowIndex; i++) {
      if (indicators.value[i].level1 === currentLevel1) {
        startRospan++
      }
    }
    if (startRospan > 0) {
      return { rowspan: 0, colspan: 0 }
    }
    return { rowspan, colspan: 1 }
  }
}

defineExpose({
  totalScore
})
</script>

<style scoped>
.eval-indicators-base {
  padding: 0;
  overflow-x: auto;
}

.eval-table {
  margin-bottom: 12px;
  width: 100%;
}

.eval-select {
  width: 100%;
}

.eval-input {
  width: 80px;
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
