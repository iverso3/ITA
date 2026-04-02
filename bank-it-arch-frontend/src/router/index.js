import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/cmdb/overview',
    name: 'CmdbOverview',
    component: () => import('@/views/cmdb/Overview.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/cmdb/servers',
    name: 'CmdbServers',
    component: () => import('@/views/cmdb/Servers.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/cmdb/network',
    name: 'CmdbNetwork',
    component: () => import('@/views/cmdb/Network.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/cmdb/storage',
    name: 'CmdbStorage',
    component: () => import('@/views/cmdb/Storage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/cmdb/cloud',
    name: 'CmdbCloud',
    component: () => import('@/views/cmdb/Cloud.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/arch/application',
    name: 'ArchApplication',
    component: () => import('@/views/arch/Application.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/arch/application/:id',
    name: 'ArchApplicationDetail',
    component: () => import('@/views/arch/ApplicationDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tech/overview',
    name: 'TechOverview',
    component: () => import('@/views/tech/Overview.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tech/stack',
    name: 'TechStack',
    component: () => import('@/views/tech/Stack.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tech/deployment',
    name: 'TechDeployment',
    component: () => import('@/views/tech/Deployment.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/data/entity',
    name: 'DataEntity',
    component: () => import('@/views/data/Entity.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/data/flow',
    name: 'DataFlow',
    component: () => import('@/views/data/Flow.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/data/distribution',
    name: 'DataDistribution',
    component: () => import('@/views/data/Distribution.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/wf/todo',
    name: 'WfTodo',
    component: () => import('@/views/wf/Todo.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/wf/task/:id',
    name: 'WfTaskDetail',
    component: () => import('@/views/wf/TaskDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/wf/done',
    name: 'WfDone',
    component: () => import('@/views/wf/Done.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/wf/my-applications',
    name: 'WfMyApplications',
    component: () => import('@/views/wf/MyApplications.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/wf/modeler/:id?',
    name: 'WfModeler',
    component: () => import('@/views/wf/Modeler.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/wf/definition',
    name: 'WfDefinition',
    component: () => import('@/views/wf/DefinitionList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/wf/instance',
    name: 'WfInstance',
    component: () => import('@/views/wf/InstanceList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/wf/instance/:id',
    name: 'WfInstanceDetail',
    component: () => import('@/views/wf/InstanceDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/report/statistics',
    name: 'ReportStatistics',
    component: () => import('@/views/report/Statistics.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/report/panorama',
    name: 'ReportPanorama',
    component: () => import('@/views/report/Panorama.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/system/user',
    name: 'SystemUser',
    component: () => import('@/views/system/User.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/system/role',
    name: 'SystemRole',
    component: () => import('@/views/system/Role.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/system/department',
    name: 'SystemDepartment',
    component: () => import('@/views/system/Department.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/system/team',
    name: 'SystemTeam',
    component: () => import('@/views/system/Team.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/system/menu',
    name: 'SystemMenu',
    component: () => import('@/views/system/Menu.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/system/permission',
    name: 'SystemPermission',
    component: () => import('@/views/system/Permission.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/system/dict',
    name: 'SystemDict',
    component: () => import('@/views/system/Dict.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/system/log',
    name: 'SystemLog',
    component: () => import('@/views/system/Log.vue'),
    meta: { requiresAuth: true }
  },
  // 流程角色管理模块
  {
    path: '/flowRole/flowRole',
    name: 'FlowRole',
    component: () => import('@/views/flowRole/FlowRole.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/flowRole/userRel',
    name: 'FlowRoleUserRel',
    component: () => import('@/views/flowRole/FlowRoleUserRel.vue'),
    meta: { requiresAuth: true }
  },
  // 开源软件管理模块
  {
    path: '/oss/software',
    name: 'OssSoftware',
    component: () => import('@/views/oss/SoftwareList.vue'),
    meta: { requiresAuth: true }
  },
  // 开源软件引入申请模块
  {
    path: '/oss/impl-apply',
    name: 'OssImplApply',
    component: () => import('@/views/oss/ImplApplyList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/oss/impl-apply/form',
    name: 'OssImplApplyForm',
    component: () => import('@/views/oss/ImplApplyForm.vue'),
    meta: { requiresAuth: true }
  },
  // 开源软件使用台账管理
  {
    path: '/oss/stdBook',
    name: 'OssStdBook',
    component: () => import('@/views/oss/StdBook.vue'),
    meta: { requiresAuth: true }
  },
  // Meta模块 - 元模型管理
  {
    path: '/meta/model',
    name: 'MetaModel',
    component: () => import('@/views/meta/ModelDesigner.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/meta/version',
    name: 'MetaVersion',
    component: () => import('@/views/meta/VersionManage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/meta/dynamic-form/:modelCode?',
    name: 'MetaDynamicForm',
    component: () => import('@/views/meta/DynamicForm.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/meta/dynamic-table/:modelCode?',
    name: 'MetaDynamicTable',
    component: () => import('@/views/meta/DynamicTable.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/meta/topology',
    name: 'MetaTopology',
    component: () => import('@/views/meta/TopologyGraph.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, from, next) => {
  const token = getToken()

  if (to.meta.requiresAuth === false) {
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  next()
})

export default router
