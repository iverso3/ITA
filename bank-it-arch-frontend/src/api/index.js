import axios from 'axios'
import { getToken } from '@/utils/auth'

const api = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    console.error('Request Error:', error.message)
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      console.error('API Error:', res.message)
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    console.error('Request Error:', error.message)
    return Promise.reject(error)
  }
)

export default api

// ===== 认证 =====
export const authApi = {
  login: (data) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
  me: () => api.get('/auth/me')
}

// ===== CMDB =====
export const cmdbServerApi = {
  list: (params) => api.get('/cmdb/servers', { params }),
  detail: (id) => api.get(`/cmdb/servers/${id}`),
  create: (data) => api.post('/cmdb/servers', data),
  update: (id, data) => api.put(`/cmdb/servers/${id}`, data),
  delete: (id) => api.delete(`/cmdb/servers/${id}`)
}

export const cmdbNetworkApi = {
  list: (params) => api.get('/cmdb/networks', { params }),
  detail: (id) => api.get(`/cmdb/networks/${id}`),
  create: (data) => api.post('/cmdb/networks', data),
  update: (id, data) => api.put(`/cmdb/networks/${id}`, data),
  delete: (id) => api.delete(`/cmdb/networks/${id}`)
}

export const cmdbStorageApi = {
  list: (params) => api.get('/cmdb/storages', { params }),
  detail: (id) => api.get(`/cmdb/storages/${id}`),
  create: (data) => api.post('/cmdb/storages', data),
  update: (id, data) => api.put(`/cmdb/storages/${id}`, data),
  delete: (id) => api.delete(`/cmdb/storages/${id}`)
}

export const cmdbCloudApi = {
  list: (params) => api.get('/cmdb/cloud-resources', { params }),
  detail: (id) => api.get(`/cmdb/cloud-resources/${id}`),
  create: (data) => api.post('/cmdb/cloud-resources', data),
  update: (id, data) => api.put(`/cmdb/cloud-resources/${id}`, data),
  delete: (id) => api.delete(`/cmdb/cloud-resources/${id}`)
}

// ===== 应用架构 =====
export const archAppApi = {
  list: (params) => api.get('/arch/applications', { params }),
  detail: (id) => api.get(`/arch/applications/${id}`),
  create: (data) => api.post('/arch/applications', data),
  update: (id, data) => api.put(`/arch/applications/${id}`, data),
  delete: (id) => api.delete(`/arch/applications/${id}`),
  modules: (id) => api.get(`/arch/applications/${id}/modules`),
  services: (id) => api.get(`/arch/applications/${id}/services`),
  dependencies: (id) => api.get(`/arch/applications/${id}/dependencies`)
}

export const archModuleApi = {
  create: (data) => api.post('/arch/modules', data),
  update: (id, data) => api.put(`/arch/modules/${id}`, data),
  delete: (id) => api.delete(`/arch/modules/${id}`)
}

export const archServiceApi = {
  create: (data) => api.post('/arch/services', data),
  update: (id, data) => api.put(`/arch/services/${id}`, data),
  delete: (id) => api.delete(`/arch/services/${id}`)
}

export const archDependencyApi = {
  create: (data) => api.post('/arch/dependencies', data),
  delete: (id) => api.delete(`/arch/dependencies/${id}`),
  graph: () => api.get('/arch/dependencies/graph')
}

// ===== 技术架构 =====
export const techStackApi = {
  list: (params) => api.get('/tech/stacks', { params }),
  tree: () => api.get('/tech/stacks/tree'),
  detail: (id) => api.get(`/tech/stacks/${id}`),
  create: (data) => api.post('/tech/stacks', data),
  update: (id, data) => api.put(`/tech/stacks/${id}`, data),
  delete: (id) => api.delete(`/tech/stacks/${id}`)
}

export const deploymentApi = {
  listByApp: (appId) => api.get(`/deployment/applications/${appId}`),
  detail: (id) => api.get(`/deployment/${id}`),
  create: (data) => api.post('/deployment', data),
  update: (id, data) => api.put(`/deployment/${id}`, data),
  delete: (id) => api.delete(`/deployment/${id}`)
}

// ===== 数据架构 =====
export const dataEntityApi = {
  list: (params) => api.get('/data/entities', { params }),
  detail: (id) => api.get(`/data/entities/${id}`),
  create: (data) => api.post('/data/entities', data),
  update: (id, data) => api.put(`/data/entities/${id}`, data),
  delete: (id) => api.delete(`/data/entities/${id}`)
}

export const dataFlowApi = {
  list: (params) => api.get('/data/flows', { params }),
  create: (data) => api.post('/data/flows', data),
  update: (id, data) => api.put(`/data/flows/${id}`, data),
  delete: (id) => api.delete(`/data/flows/${id}`),
  graph: () => api.get('/data/flows/graph')
}

// ===== 审批流程 =====
export const wfApi = {
  definitions: (params) => api.get('/wf/definitions', { params }),
  definitionDetail: (id) => api.get(`/wf/definitions/${id}`),
  instances: (params) => api.get('/wf/instances', { params }),
  instanceDetail: (id) => api.get(`/wf/instances/${id}`),
  instanceHistory: (id) => api.get(`/wf/instances/${id}/history`),
  todoList: (params) => api.get('/wf/tasks/todo', { params }),
  doneList: (params) => api.get('/wf/tasks/done', { params }),
  approve: (id, data) => api.post(`/wf/tasks/${id}/approve`, data),
  reject: (id, data) => api.post(`/wf/tasks/${id}/reject`, data),
  returnTask: (id, data) => api.post(`/wf/tasks/${id}/return`, data),
  transferTask: (id, data) => api.post(`/wf/tasks/${id}/transfer`, data),
  delegateTask: (id, data) => api.post(`/wf/tasks/${id}/delegate`, data),
  withdrawInstance: (id) => api.post(`/wf/instances/${id}/withdraw`)
}

// ===== 系统管理 =====
export const sysUserApi = {
  list: (params) => api.get('/system/users', { params }),
  detail: (id) => api.get(`/system/users/${id}`),
  create: (data) => api.post('/system/users', data),
  update: (id, data) => api.put(`/system/users/${id}`, data),
  delete: (id) => api.delete(`/system/users/${id}`),
  toggleStatus: (id, data) => api.patch(`/system/users/${id}/status`, data),
  me: () => api.get('/system/users/me'),
  resetPassword: (id) => api.post(`/system/users/${id}/reset-password`),
  changePassword: (id, data) => api.post(`/system/users/${id}/change-password`, data)
}

export const sysRoleApi = {
  list: () => api.get('/system/roles'),
  detail: (id) => api.get(`/system/roles/${id}`),
  create: (data) => api.post('/system/roles', data),
  update: (id, data) => api.put(`/system/roles/${id}`, data),
  delete: (id) => api.delete(`/system/roles/${id}`),
  getRoleMenuTree: (id) => api.get(`/system/roles/${id}/menu-tree`),
  assignRoleMenus: (id, data) => api.put(`/system/roles/${id}/menus`, data)
}

export const sysDeptApi = {
  tree: () => api.get('/system/departments/tree'),
  list: () => api.get('/system/departments'),
  detail: (id) => api.get(`/system/departments/${id}`),
  create: (data) => api.post('/system/departments', data),
  update: (id, data) => api.put(`/system/departments/${id}`, data),
  delete: (id) => api.delete(`/system/departments/${id}`)
}

export const sysTeamApi = {
  list: (params) => api.get('/system/teams', { params }),
  detail: (id) => api.get(`/system/teams/${id}`),
  create: (data) => api.post('/system/teams', data),
  update: (id, data) => api.put(`/system/teams/${id}`, data),
  delete: (id) => api.delete(`/system/teams/${id}`)
}

export const sysMenuApi = {
  tree: () => api.get('/system/menus/tree'),
  list: () => api.get('/system/menus'),
  detail: (id) => api.get(`/system/menus/${id}`),
  create: (data) => api.post('/system/menus', data),
  update: (id, data) => api.put(`/system/menus/${id}`, data),
  delete: (id) => api.delete(`/system/menus/${id}`)
}

export const sysPermissionApi = {
  list: () => api.get('/system/permissions'),
  detail: (id) => api.get(`/system/permissions/${id}`),
  create: (data) => api.post('/system/permissions', data),
  update: (id, data) => api.put(`/system/permissions/${id}`, data),
  delete: (id) => api.delete(`/system/permissions/${id}`)
}

export const sysDictApi = {
  list: () => api.get('/system/dicts'),
  detail: (id) => api.get(`/system/dicts/${id}`),
  create: (data) => api.post('/system/dicts', data),
  update: (id, data) => api.put(`/system/dicts/${id}`, data),
  delete: (id) => api.delete(`/system/dicts/${id}`)
}

export const sysDictItemApi = {
  listByDict: (dictId) => api.get(`/system/dict-items/${dictId}`),
  create: (data) => api.post('/system/dict-items', data),
  update: (id, data) => api.put(`/system/dict-items/${id}`, data),
  delete: (id) => api.delete(`/system/dict-items/${id}`)
}

export const sysLogApi = {
  list: (params) => api.get('/system/logs', { params })
}

// ===== 报表中心 =====
export const reportApi = {
  systemStats: () => api.get('/report/statistics/systems'),
  resourceStats: () => api.get('/report/statistics/resources'),
  techDistribution: () => api.get('/report/statistics/tech-distribution'),
  departmentStats: () => api.get('/report/statistics/department'),
  systemPanorama: () => api.get('/report/panorama/systems'),
  techStackPanorama: () => api.get('/report/panorama/tech-stacks'),
  dataDistribution: () => api.get('/report/panorama/data-distribution'),
  dataFlowPanorama: () => api.get('/report/panorama/data-flows')
}
