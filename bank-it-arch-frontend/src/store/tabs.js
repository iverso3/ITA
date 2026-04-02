import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// Route path to display title mapping
const routeTitles = {
  '/': '首页',
  '/cmdb/overview': 'CMDB总览',
  '/cmdb/servers': '服务器',
  '/cmdb/network': '网络设备',
  '/cmdb/storage': '存储设备',
  '/cmdb/cloud': '云资源',
  '/arch/application': '应用架构',
  '/tech/overview': '技术架构总览',
  '/tech/stack': '技术栈',
  '/tech/deployment': '部署架构',
  '/data/entity': '数据实体',
  '/data/flow': '数据流',
  '/data/distribution': '数据分布',
  '/wf/todo': '待办任务',
  '/wf/done': '已办任务',
  '/wf/definition': '流程定义',
  '/wf/instance': '流程实例',
  '/wf/my-applications': '我的申请',
  '/wf/modeler': '流程建模',
  '/report/statistics': '统计报表',
  '/report/panorama': '架构全景图',
  '/system/user': '用户管理',
  '/system/role': '角色管理',
  '/system/department': '部门管理',
  '/system/team': '团队管理',
  '/system/menu': '菜单管理',
  '/system/permission': '权限管理',
  '/system/dict': '字典管理',
  '/system/log': '操作日志',
  '/flowRole/flowRole': '流程角色管理',
  '/flowRole/userRel': '用户流程角色设置',
  '/oss/software': '开源软件',
  '/oss/stdBook': '使用台账',
  '/oss/impl-apply': '引入申请',
  '/oss/impl-apply/form': '引入申请流程',
  '/meta/model': '模型设计器',
  '/meta/version': '版本管理',
  '/meta/dynamic-form': '动态表单',
  '/meta/dynamic-table': '动态表格',
  '/meta/topology': '拓扑查询'
}

// Pattern-based route titles (for dynamic routes like /arch/application/:id)
const routePatterns = [
  { pattern: /^\/arch\/application\/\d+$/, title: '应用详情' },
  { pattern: /^\/wf\/task\/\d+$/, title: '任务详情' },
  { pattern: /^\/wf\/instance\/\d+$/, title: '流程实例详情' },
  { pattern: /^\/wf\/modeler\/\d+$/, title: '流程建模' },
  { pattern: /^\/meta\/dynamic-form\/.+$/, title: '动态表单' },
  { pattern: /^\/meta\/dynamic-table\/.+$/, title: '动态表格' }
]

// Get title for a path, supporting dynamic routes
const getTitleForPath = (path) => {
  // First try exact match
  if (routeTitles[path]) {
    return routeTitles[path]
  }
  // Then try pattern matching
  for (const item of routePatterns) {
    if (item.pattern.test(path)) {
      return item.title
    }
  }
  return null
}

export const useTabsStore = defineStore('tabs', () => {
  const tabs = ref([
    {
      path: '/',
      title: '首页',
      closable: false
    }
  ])

  const activeTabPath = ref('/')

  const activeTab = computed(() => {
    return tabs.value.find(t => t.path === activeTabPath.value) || tabs.value[0]
  })

  const hasTab = (path) => {
    return tabs.value.some(t => t.path === path)
  }

  const addTab = (path, title) => {
    if (hasTab(path)) {
      activeTabPath.value = path
      return
    }
    const tabTitle = title || getTitleForPath(path) || path
    tabs.value.push({
      path,
      title: tabTitle,
      closable: path !== '/'
    })
    activeTabPath.value = path
  }

  const removeTab = (path) => {
    if (path === '/') return // Cannot close Dashboard

    const index = tabs.value.findIndex(t => t.path === path)
    if (index === -1) return

    tabs.value.splice(index, 1)

    // If removed tab was active, activate the previous tab
    if (activeTabPath.value === path) {
      const newIndex = Math.max(0, index - 1)
      activeTabPath.value = tabs.value[newIndex].path
      return tabs.value[newIndex].path // Return new active path for navigation
    }
    return null
  }

  const closeOtherTabs = (path) => {
    tabs.value = tabs.value.filter(t => t.path === '/' || t.path === path)
    activeTabPath.value = path
  }

  const closeAllTabs = () => {
    tabs.value = tabs.value.filter(t => !t.closable)
    activeTabPath.value = '/'
  }

  const setActiveTab = (path) => {
    if (hasTab(path)) {
      activeTabPath.value = path
    }
  }

  const updateTabTitle = (path, title) => {
    const tab = tabs.value.find(t => t.path === path)
    if (tab) {
      tab.title = title
    }
  }

  return {
    tabs,
    activeTabPath,
    activeTab,
    hasTab,
    addTab,
    removeTab,
    closeOtherTabs,
    closeAllTabs,
    setActiveTab,
    updateTabTitle
  }
})