<template>
  <div class="end-node">
    <Handle id="left" type="target" :position="Position.Left" />
    <div class="node-icon">
      <el-icon><CircleClose /></el-icon>
    </div>
    <div class="node-label">{{ data.nodeName || '结束' }}</div>
  </div>
</template>

<script setup>
import { CircleClose } from '@element-plus/icons-vue'
import { Handle, Position } from '@vue-flow/core'

defineProps({
  data: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.end-node {
  width: 120px;
  height: 48px;
  background: linear-gradient(135deg, #F56C6C 0%, #f89898 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: white;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.4);
}

.node-icon {
  display: flex;
  align-items: center;
}
</style>
