<template>
  <div class="parallel-join-node">
    <Handle id="left-top" type="target" :position="Position.Left" style="top: 25%" />
    <Handle id="left-bottom" type="target" :position="Position.Left" style="top: 75%" />
    <div class="node-icon">
      <el-icon><ArrowRight /></el-icon>
    </div>
    <div class="node-label">{{ data.nodeName || '并行汇聚' }}</div>
    <Handle id="right" type="source" :position="Position.Right" />
  </div>
</template>

<script setup>
import { ArrowRight } from '@element-plus/icons-vue'
import { Handle, Position } from '@vue-flow/core'

defineProps({
  data: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.parallel-join-node {
  width: 120px;
  height: 48px;
  background: linear-gradient(135deg, #9C27B0 0%, #b980c7 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: white;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(156, 39, 176, 0.4);
}

.node-icon {
  display: flex;
  align-items: center;
}
</style>
