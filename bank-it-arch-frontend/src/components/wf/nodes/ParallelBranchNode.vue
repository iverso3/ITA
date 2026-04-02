<template>
  <div class="parallel-branch-node">
    <Handle id="left" type="target" :position="Position.Left" />
    <div class="node-icon">
      <el-icon><Connection /></el-icon>
    </div>
    <div class="node-label">{{ data.nodeName || '并行分支' }}</div>
    <Handle id="right-top" type="source" :position="Position.Right" style="top: 25%" />
    <Handle id="right-bottom" type="source" :position="Position.Right" style="top: 75%" />
  </div>
</template>

<script setup>
import { Connection } from '@element-plus/icons-vue'
import { Handle, Position } from '@vue-flow/core'

defineProps({
  data: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.parallel-branch-node {
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
