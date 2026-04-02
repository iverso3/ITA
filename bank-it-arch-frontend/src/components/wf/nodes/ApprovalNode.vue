<template>
  <div class="approval-node">
    <!-- 左侧入口 -->
    <Handle id="left" type="target" :position="Position.Left" />
    <div class="node-header">
      <el-icon><User /></el-icon>
      <span>{{ data.nodeName || '审批节点' }}</span>
    </div>
    <div class="node-body">
      <div class="approval-type">
        {{ approvalTypeLabel }}
      </div>
      <div class="node-info" v-if="data.assignedRoleName">
        {{ data.assignedRoleName }}
      </div>
    </div>
    <!-- 右侧出口 -->
    <Handle id="right" type="source" :position="Position.Right" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { User } from '@element-plus/icons-vue'
import { Handle, Position } from '@vue-flow/core'

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

const approvalTypeLabel = computed(() => {
  const typeMap = {
    'SINGLE': '单人审批',
    'GRAB': '抢任务',
    'MULTI_COUNTER': '会签',
    'MULTI_OR': '或签'
  }
  return typeMap[props.data.approvalType] || '单人审批'
})
</script>

<style scoped>
.approval-node {
  width: 160px;
  background: white;
  border: 2px solid #409EFF;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.node-header {
  background: #409EFF;
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

.approval-type {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.node-info {
  font-size: 11px;
  color: #909399;
}
</style>
