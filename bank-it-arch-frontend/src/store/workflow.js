import { defineStore } from 'pinia'
import { ref } from 'vue'
import { wfApi } from '@/api'

export const useWorkflowStore = defineStore('workflow', () => {
  // 流程模型
  const processModel = ref(null)
  const selectedNode = ref(null)
  const selectedLine = ref(null)
  const isDirty = ref(false)

  // 节点类型定义
  const nodeTypes = [
    {
      type: 'START',
      label: '开始',
      category: 'START',
      icon: 'VideoPlay',
      color: '#67C23A',
      description: '流程开始节点'
    },
    {
      type: 'END',
      label: '结束',
      category: 'END',
      icon: 'CircleClose',
      color: '#F56C6C',
      description: '流程结束节点'
    },
    {
      type: 'APPROVAL',
      label: '审批',
      category: 'APPROVAL',
      icon: 'User',
      color: '#409EFF',
      description: '审批节点'
    },
    {
      type: 'CONDITION',
      label: '条件',
      category: 'CONDITION',
      icon: 'Switch',
      color: '#E6A23C',
      description: '条件分支节点'
    },
    {
      type: 'PARALLEL_BRANCH',
      label: '并行',
      category: 'PARALLEL_BRANCH',
      icon: 'Connection',
      color: '#9C27B0',
      description: '并行分支节点'
    },
    {
      type: 'PARALLEL_JOIN',
      label: '汇聚',
      category: 'PARALLEL_JOIN',
      icon: 'ArrowRight',
      color: '#9C27B0',
      description: '并行汇聚节点'
    }
  ]

  // 审批方式
  const approvalTypes = [
    { value: 'SINGLE', label: '单人审批' },
    { value: 'GRAB', label: '抢任务' },
    { value: 'MULTI_COUNTER', label: '会签' },
    { value: 'MULTI_OR', label: '或签' }
  ]

  // 加载流程模型
  async function loadModel(definitionId) {
    try {
      const res = await wfApi.modeler.getModel(definitionId)
      processModel.value = res.data
      isDirty.value = false
      return res.data
    } catch (error) {
      console.error('加载流程模型失败:', error)
      throw error
    }
  }

  // 保存节点
  async function saveNodes(definitionId, nodes) {
    try {
      const res = await wfApi.modeler.saveNodes(definitionId, nodes)
      isDirty.value = false
      return res
    } catch (error) {
      console.error('保存节点失败:', error)
      throw error
    }
  }

  // 保存连线
  async function saveLines(definitionId, lines) {
    try {
      await wfApi.modeler.saveLines(definitionId, lines)
      isDirty.value = false
    } catch (error) {
      console.error('保存连线失败:', error)
      throw error
    }
  }

  // 校验模型
  async function validateModel(model) {
    try {
      return await wfApi.modeler.validate(model)
    } catch (error) {
      console.error('校验模型失败:', error)
      throw error
    }
  }

  // 发布流程
  async function publishModel(definitionId, changeLog) {
    try {
      await wfApi.modeler.publish(definitionId, changeLog)
      isDirty.value = false
    } catch (error) {
      console.error('发布流程失败:', error)
      throw error
    }
  }

  // 选择节点
  function selectNode(node) {
    selectedNode.value = node
    selectedLine.value = null
  }

  // 选择连线
  function selectLine(line) {
    selectedLine.value = line
    selectedNode.value = null
  }

  // 清除选择
  function clearSelection() {
    selectedNode.value = null
    selectedLine.value = null
  }

  // 标记为已修改
  function markDirty() {
    isDirty.value = true
  }

  // 创建新节点
  function createNode(type, position) {
    const nodeType = nodeTypes.find(n => n.type === type)
    return {
      id: `node_${Date.now()}`,
      type: type,
      position: position,
      data: {
        nodeCode: `${type.toLowerCase()}_${Date.now()}`,
        nodeName: nodeType?.label || type,
        nodeCategory: nodeType?.category || type,
        approvalType: 'SINGLE',
        timeoutDuration: 1440,
        timeoutAction: 'TRANSFER'
      }
    }
  }

  return {
    processModel,
    selectedNode,
    selectedLine,
    isDirty,
    nodeTypes,
    approvalTypes,
    loadModel,
    saveNodes,
    saveLines,
    validateModel,
    publishModel,
    selectNode,
    selectLine,
    clearSelection,
    markDirty,
    createNode
  }
})
