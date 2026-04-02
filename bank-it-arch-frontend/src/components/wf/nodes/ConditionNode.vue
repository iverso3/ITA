<template>
  <div class="condition-node">
    <Handle id="left" type="target" :position="Position.Left" />
    <div class="node-header">
      <el-icon><Switch /></el-icon>
      <span>{{ data.nodeName || '条件节点' }}</span>
    </div>
    <div class="node-body">
      <div class="condition-info">
        {{ conditionLabel }}
      </div>
    </div>
    <Handle id="right" type="source" :position="Position.Right" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Switch } from '@element-plus/icons-vue'
import { Handle, Position } from '@vue-flow/core'

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

const conditionLabel = computed(() => {
  return props.data.conditionRule ? '有条件' : '无条件'
})
</script>

<style scoped>
.condition-node {
  width: 140px;
  background: white;
  border: 2px solid #E6A23C;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.3);
}

.node-header {
  background: #E6A23C;
  color: white;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
}

.node-body {
  padding: 8px 12px;
}

.condition-info {
  font-size: 12px;
  color: #606266;
}
</style>
