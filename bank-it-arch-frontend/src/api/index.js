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
      console.error('API Error:', res.message, res)
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    console.error('Request Error:', error.message, error.response?.data)
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
  dependencies: (id) => api.get(`/arch/applications/${id}/dependencies`),
  export: (params) => api.get('/arch/applications/export', { params, responseType: 'blob' }),
  import: (data) => api.post('/arch/applications/import', data, { headers: { 'Content-Type': 'multipart/form-data' } }),
  getLayeredTree: () => api.get('/arch/applications/layered-tree'),
  searchApps: (keyword) => api.get('/arch/applications/search', { params: { keyword } })
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
  // 流程定义
  definitions: (params) => api.get('/wf/definitions', { params }),
  definitionDetail: (id) => api.get(`/wf/definitions/${id}`),
  definitionCreate: (data) => api.post('/wf/definitions', data),
  definitionUpdate: (id, data) => api.put(`/wf/definitions/${id}`, data),
  definitionToggleStatus: (id) => api.patch(`/wf/definitions/${id}/status`),

  // 流程建模
  modeler: {
    getModel: (definitionId) => api.get(`/wf/modeler/${definitionId}`),
    saveNodes: (definitionId, nodes) => api.put(`/wf/modeler/${definitionId}/nodes`, nodes),
    saveLines: (definitionId, lines) => api.put(`/wf/modeler/${definitionId}/lines`, lines),
    validate: (model) => api.post('/wf/modeler/validate', model),
    publish: (definitionId, changeLog) => api.post(`/wf/modeler/${definitionId}/publish`, { changeLog })
  },

  // 流程实例
  instances: (params) => api.get('/wf/instances', { params }),
  instanceDetail: (id) => api.get(`/wf/instances/${id}`),
  instanceHistory: (id) => api.get(`/wf/instances/${id}/history`),
  instanceTrace: (id) => api.get(`/wf/instances/${id}/trace`),
  instanceTasks: (id) => api.get(`/wf/instances/${id}/tasks`),
  startProcess: (data) => api.post('/wf/instances', data),
  withdrawInstance: (id) => api.post(`/wf/instances/${id}/withdraw`),
  terminateInstance: (id, reason) => api.post(`/wf/instances/${id}/terminate`, { reason }),
  myApplications: (params) => api.get('/wf/instances/my-applications', { params }),
  myDoneList: (params) => api.get('/wf/instances/my-done', { params }),

  // 任务操作
  todoList: (params) => api.get('/wf/tasks/todo', { params }),
  doneList: (params) => api.get('/wf/tasks/done', { params }),
  taskDetail: (id) => api.get(`/wf/tasks/${id}/detail`),
  rejectNodes: (id) => api.get(`/wf/tasks/${id}/reject-nodes`),
  approve: (id, data) => api.post(`/wf/tasks/${id}/approve`, data),
  reject: (id, data) => api.post(`/wf/tasks/${id}/reject`, data),
  returnTask: (id, data) => api.post(`/wf/tasks/${id}/return`, data),
  transferTask: (id, data) => api.post(`/wf/tasks/${id}/transfer`, data),
  delegateTask: (id, data) => api.post(`/wf/tasks/${id}/delegate`, data),
  claimTask: (id, data) => api.post(`/wf/tasks/${id}/claim`, data)
}

// ===== 系统管理 =====
export const sysUserApi = {
  list: (params) => api.get('/system/users', { params }),
  listAll: () => api.get('/system/users/all'),
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

// ===== Meta元模型管理 =====
export const metaModelApi = {
  list: () => api.get('/v1/meta/models/list'),
  page: (params) => api.get('/v1/meta/models/page', { params }),
  detail: (id) => api.get(`/v1/meta/models/${id}`),
  create: (data) => api.post('/v1/meta/models', data),
  update: (id, data) => api.put(`/v1/meta/models/${id}`, data),
  delete: (id) => api.delete(`/v1/meta/models/${id}`),
  getByCode: (code) => api.get(`/v1/meta/models/code/${code}`),
  listByType: (type) => api.get(`/v1/meta/models/type/${type}`),
  publish: (id) => api.post(`/v1/meta/publish/submit/${id}`),
  archive: (id) => api.post(`/v1/meta/publish/archive/${id}`),
  rollback: (id, version) => api.post(`/v1/meta/publish/rollback/${id}/${version}`)
}

export const metaFieldApi = {
  listByModel: (modelId) => api.get(`/v1/meta/fields/model/${modelId}`),
  queryableFields: (modelId) => api.get(`/v1/meta/fields/model/${modelId}/queryable`),
  sensitiveFields: (modelId) => api.get(`/v1/meta/fields/model/${modelId}/sensitive`),
  create: (modelId, data) => api.post(`/v1/meta/fields/${modelId}`, data),
  update: (modelId, fieldId, data) => api.put(`/v1/meta/fields/${modelId}/${fieldId}`, data),
  delete: (modelId, fieldId) => api.delete(`/v1/meta/fields/${modelId}/${fieldId}`),
  batchCreate: (modelId, data) => api.post(`/v1/meta/fields/${modelId}/batch`, data),
  reorder: (modelId, fieldIds) => api.put(`/v1/meta/fields/${modelId}/reorder`, fieldIds)
}

export const metaGroupApi = {
  listByModel: (modelId) => api.get(`/v1/meta/groups/model/${modelId}`),
  groupsWithFields: (modelId) => api.get(`/v1/meta/groups/model/${modelId}/with-fields`),
  create: (modelId, data) => api.post(`/v1/meta/groups/${modelId}`, data),
  update: (modelId, groupId, data) => api.put(`/v1/meta/groups/${modelId}/${groupId}`, data),
  delete: (modelId, groupId) => api.delete(`/v1/meta/groups/${modelId}/${groupId}`)
}

export const metaRelationshipApi = {
  listByModel: (modelId) => api.get(`/v1/meta/relationships/model/${modelId}`),
  create: (data) => api.post('/v1/meta/relationships', data),
  update: (id, data) => api.put(`/v1/meta/relationships/${id}`, data),
  delete: (id) => api.delete(`/v1/meta/relationships/${id}`)
}

export const metaEntityApi = {
  list: (modelId, params) => api.get(`/v1/meta/entities/page/${modelId}`, { params }),
  detail: (id) => api.get(`/v1/meta/entities/${id}`),
  getByBusinessId: (modelId, businessId) => api.get(`/v1/meta/entities/business/${modelId}/${businessId}`),
  create: (data) => api.post('/v1/meta/entities', data),
  batchCreate: (modelId, data) => api.post(`/v1/meta/entities/batch/${modelId}`, data),
  update: (id, data) => api.put(`/v1/meta/entities/${id}`, data),
  delete: (id) => api.delete(`/v1/meta/entities/${id}`),
  batchDelete: (modelId, ids) => api.delete(`/v1/meta/entities/batch/${modelId}`, { data: ids }),
  bindRelation: (sourceEntityId, relCode, targetEntityId) =>
    api.post(`/v1/meta/entities/${sourceEntityId}/relations/${relCode}/${targetEntityId}`),
  unbindRelation: (sourceEntityId, relCode, targetEntityId) =>
    api.delete(`/v1/meta/entities/${sourceEntityId}/relations/${relCode}/${targetEntityId}`),
  getRelations: (entityId) => api.get(`/v1/meta/entities/${entityId}/relations`),
  getRelatedEntities: (entityId, relCode) => api.get(`/v1/meta/entities/${entityId}/relations/${relCode}`),
  setFieldValue: (entityId, fieldCode, value) =>
    api.put(`/v1/meta/entities/${entityId}/fields/${fieldCode}`, { value }),
  setFieldValues: (entityId, values) =>
    api.put(`/v1/meta/entities/${entityId}/fields`, values),
  getAllFieldValues: (entityId) => api.get(`/v1/meta/entities/${entityId}/fields`)
}

export const metaDynamicApi = {
  query: (modelCode, data) => api.post(`/v1/meta/dynamic/query/${modelCode}`, data),
  get: (modelCode, id, selectFields) => api.get(`/v1/meta/dynamic/get/${modelCode}/${id}`, { params: { selectFields } }),
  create: (modelCode, data) => api.post(`/v1/meta/dynamic/create/${modelCode}`, data),
  update: (id, data) => api.put(`/v1/meta/dynamic/update/${id}`, data),
  delete: (id) => api.delete(`/v1/meta/dynamic/delete/${id}`),
  formConfig: (modelCode, formType) => api.get(`/v1/meta/dynamic/form-config/${modelCode}`, { params: { formType } }),
  tableConfig: (modelCode) => api.get(`/v1/meta/dynamic/table-config/${modelCode}`),
  fieldConfig: (modelCode, fieldCode) => api.get(`/v1/meta/dynamic/field-config/${modelCode}/${fieldCode}`),
  options: (dictCode) => api.get(`/v1/meta/dynamic/options/${dictCode}`)
}

export const metaGraphApi = {
  entityGraph: (entityId, depth) => api.get(`/v1/meta/graph/entity/${entityId}`, { params: { depth } }),
  modelGraph: (modelId) => api.get(`/v1/meta/graph/model/${modelId}`),
  recursiveQuery: (entityId, relCode, maxDepth) =>
    api.get(`/v1/meta/graph/recursive/${entityId}`, { params: { relCode, maxDepth } }),
  shortestPath: (sourceEntityId, targetEntityId) =>
    api.get(`/v1/meta/graph/shortest-path`, { params: { sourceEntityId, targetEntityId } }),
  batchQuery: (entityIds, depth) => api.post(`/v1/meta/graph/batch`, entityIds, { params: { depth } }),
  detectCycle: (modelId) => api.get(`/v1/meta/graph/cycle/${modelId}`),
  isolatedEntities: (modelId) => api.get(`/v1/meta/graph/isolated/${modelId}`),
  stats: (modelId) => api.get(`/v1/meta/graph/stats/${modelId}`)
}

export const metaPublishApi = {
  history: (modelId) => api.get(`/v1/meta/publish/history/${modelId}`),
  active: (modelId) => api.get(`/v1/meta/publish/active/${modelId}`),
  compare: (versionId1, versionId2) => api.get(`/v1/meta/publish/compare/${versionId1}/${versionId2}`),
  submit: (modelId) => api.post(`/v1/meta/publish/submit/${modelId}`),
  approve: (versionId, comment) => api.post(`/v1/meta/publish/approve/${versionId}`, { params: { approveComment: comment } }),
  execute: (versionId) => api.post(`/v1/meta/publish/execute/${versionId}`),
  archive: (versionId) => api.post(`/v1/meta/publish/archive/${versionId}`),
  rollback: (recordId) => api.post(`/v1/meta/publish/rollback/${recordId}`),
  rollbackToVersion: (modelId, targetVersion) => api.post(`/v1/meta/publish/rollback/${modelId}/${targetVersion}`)
}

// ===== 开源软件管理 =====
export const ossSoftwareApi = {
  list: (params) => api.get('/oss/software/list', { params }),
  detail: (id) => api.get(`/oss/software/${id}`),
  create: (data) => api.post('/oss/software', data),
  update: (id, data) => api.put(`/oss/software/${id}`, data),
  delete: (id) => api.delete(`/oss/software/${id}`),
  export: (params) => api.get('/oss/software/export', { params })
}

// ===== OSS使用台账 =====
export const ossUseStandingBookApi = {
  // Main表
  listMain: (params) => api.get('/oss/standing-book/main/list', { params }),
  getMainById: (id) => api.get(`/oss/standing-book/main/${id}`),
  createMain: (data) => api.post('/oss/standing-book/main', data),
  updateMain: (id, data) => api.put(`/oss/standing-book/main/${id}`, data),
  deleteMain: (id) => api.delete(`/oss/standing-book/main/${id}`),
  exportMain: (params) => api.get('/oss/standing-book/main/export', { params }),
  importMain: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/oss/standing-book/main/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  // Detail表
  listDetails: (params) => api.get('/oss/standing-book/details', { params }),
  getDetailById: (id) => api.get(`/oss/standing-book/details/${id}`),
  createDetail: (data) => api.post('/oss/standing-book/details', data),
  updateDetail: (id, data) => api.put(`/oss/standing-book/details/${id}`, data),
  deleteDetail: (id) => api.delete(`/oss/standing-book/details/${id}`)
}

// ===== 开源软件版本管理 =====
export const ossSoftwareBaselineApi = {
  list: (params) => api.get('/oss/software/baseline/list', { params }),
  detail: (id) => api.get(`/oss/software/baseline/${id}`),
  create: (data) => api.post('/oss/software/baseline', data),
  update: (id, data) => api.put(`/oss/software/baseline/${id}`, data),
  delete: (id) => api.delete(`/oss/software/baseline/${id}`),
  export: (params) => api.get('/oss/software/baseline/export', { params }),
  // 介质文档
  mediaList: (params) => api.get('/oss/software/baseline/media/list', { params }),
  download: (filePath) => api.get('/oss/software/baseline/media/download', { params: { filePath }, responseType: 'blob' })
}

// ===== 开源软件引入申请 =====
export const ossImplApplyApi = {
  list: (params) => api.get('/oss/impl/apply/list', { params }),
  detail: (id) => api.get(`/oss/impl/apply/${id}`),
  detailByNo: (implApplyNo) => api.get(`/oss/impl/apply/no/${implApplyNo}`),
  detailByUuid: (uuid) => api.get(`/oss/impl/apply/by-uuid/${uuid}`),
  create: (data) => api.post('/oss/impl/apply', data),
  update: (id, data) => api.put(`/oss/impl/apply/${id}`, data),
  delete: (id) => api.delete(`/oss/impl/apply/${id}`),
  // 获取软件列表（用于新版本引入下拉）
  softwareList: (params) => api.get('/oss/impl/apply/software-list', { params }),
  // 拓展信息
  supplementary: (implApplyNo) => api.get(`/oss/impl/apply/supplementary/${implApplyNo}`),
  updateSupplementary: (implApplyNo, data) => api.put(`/oss/impl/apply/supplementary/${implApplyNo}`, data),
  // 检查软件名称和版本是否已存在（用于首次引入校验）
  checkDuplicate: (swName, swVersion) => api.get('/oss/impl/apply/check-duplicate', { params: { swName, swVersion } }),
  // 检查软件特定版本是否已存在于版本清单表（用于新版本引入校验）
  checkVersionExists: (swId, swVersion) => api.get('/oss/impl/apply/check-version-exists', { params: { swId, swVersion } }),
  // 启动审批流程
  startProcess: (data) => api.post('/oss/impl/apply/start-process', data),
  // 获取审批轨迹
  trace: (implApplyNo) => api.get(`/oss/impl/apply/trace/${implApplyNo}`)
}

// ===== 流程角色管理 =====
export const flowRoleApi = {
  list: (params) => api.get('/flow-role', { params }),
  listAll: () => api.get('/flow-role/all'),
  detail: (id) => api.get(`/flow-role/${id}`),
  create: (data) => api.post('/flow-role', data),
  update: (id, data) => api.put(`/flow-role/${id}`, data),
  delete: (id) => api.delete(`/flow-role/${id}`)
}

// ===== 用户流程角色管理 =====
export const flowRoleUserRelApi = {
  list: (params) => api.get('/flow-role-user-rel', { params }),
  detail: (id) => api.get(`/flow-role-user-rel/${id}`),
  create: (data) => api.post('/flow-role-user-rel', data),
  update: (id, data) => api.put(`/flow-role-user-rel/${id}`, data),
  delete: (id) => api.delete(`/flow-role-user-rel/${id}`),
  check: (userId, flowRoleId) => api.get('/flow-role-user-rel/check', { params: { userId, flowRoleId } })
}
