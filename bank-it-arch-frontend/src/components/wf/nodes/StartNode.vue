<template>
  <div class="start-node">
    <Handle id="right" type="source" :position="Position.Right" />
    <div class="node-icon">
      <el-icon><VideoPlay /></el-icon>
    </div>
    <div class="node-label">{{ data.nodeName || '开始' }}</div>
  </div>
</template>

<script setup>
import { VideoPlay } from '@element-plus/icons-vue'
import { Handle, Position } from '@vue-flow/core'

defineProps({
  data: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.start-node {
  width: 120px;
  height: 48px;
  background: linear-gradient(135deg, #67C23A 0%, #95d475 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: white;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.4);
}

.node-icon {
  display: flex;
  align-items: center;
}
</style>
