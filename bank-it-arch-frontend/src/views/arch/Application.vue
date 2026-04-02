<template>
  <div class="arch-application">
    <div class="app-layout">
      <!-- Left Sidebar - Two Level Hierarchy -->
      <div class="app-sidebar layered-sidebar">
        <div class="sidebar-header">
          <el-input
            v-model="sidebarKeyword"
            placeholder="输入关键字后按Enter搜索"
            clearable
            @keyup.enter="handleSidebarSearch"
            @input="handleSidebarSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <el-scrollbar class="sidebar-scroll">
          <!-- Search Results Panel -->
          <div v-if="searchResults.length > 0" class="search-results">
            <div class="search-results-header">
              <span>搜索结果 ({{ searchResults.length }})</span>
              <el-button text size="small" @click="clearSearch">清除</el-button>
            </div>
            <div
              v-for="item in searchResults"
              :key="item.id"
              class="search-result-item"
              @click="handleSearchResultClick(item)"
            >
              <span class="result-name">{{ item.label }}</span>
              <div class="result-tags">
                <span class="tag tag-layer">{{ item.systemLayer }}</span>
                <span class="tag tag-group">{{ item.parentAppName }}</span>
              </div>
            </div>
          </div>

          <!-- Tree Structure -->
          <div v-else class="tree-container">
            <div
              v-for="rootNode in sidebarData"
              :key="rootNode.id"
              class="tree-root"
            >
              <!-- Root Node: 总行应用系统 -->
              <div
                class="tree-node tree-root-node"
                :class="{ 'is-expanded': expandedKeys.includes(rootNode.id) }"
                @click="toggleExpand(rootNode.id)"
              >
                <el-icon class="node-arrow"><CaretRight /></el-icon>
                <span class="node-label">{{ rootNode.label }}</span>
                <span class="node-count">{{ rootNode.count }}</span>
              </div>

              <!-- Level 1: 应用分层 -->
              <div
                v-show="expandedKeys.includes(rootNode.id)"
                class="tree-level level-1"
              >
                <div
                  v-for="layer in rootNode.children"
                  :key="layer.id"
                  class="tree-branch"
                >
                  <!-- Layer Node -->
                  <div
                    class="tree-node tree-layer-node"
                    :class="{
                      'is-expanded': expandedKeys.includes(layer.id),
                      'is-active': activeNodeId === layer.id
                    }"
                    @click="toggleExpand(layer.id)"
                  >
                    <el-icon class="node-arrow">
                      <CaretRight v-if="layer.children && layer.children.length" />
                      <span v-else class="no-children-dot"></span>
                    </el-icon>
                    <span class="node-label">{{ layer.label }}</span>
                    <span class="node-count">{{ layer.count }}</span>
                    <span class="tag tag-layer">应用分层</span>
                  </div>

                  <!-- Level 2: 应用群组 -->
                  <div
                    v-show="expandedKeys.includes(layer.id)"
                    class="tree-level level-2"
                  >
                    <div
                      v-for="group in layer.children"
                      :key="group.id"
                      class="tree-branch"
                    >
                      <!-- Group Node -->
                      <div
                        class="tree-node tree-group-node"
                        :class="{ 'is-active': activeNodeId === group.id }"
                        @click="handleGroupNodeClick(group, layer)"
                      >
                        <el-icon class="node-arrow">
                          <CaretRight v-if="group.children && group.children.length" />
                          <span v-else class="no-children-dot"></span>
                        </el-icon>
                        <span class="node-label">{{ group.label }}</span>
                        <span class="node-count">{{ group.count }}</span>
                        <span class="tag tag-group">应用群组</span>
                      </div>

                      <!-- Level 3: 具体应用 -->
                      <div
                        v-show="expandedKeys.includes(layer.id) && expandedKeys.includes(group.id)"
                        class="tree-level level-3"
                      >
                        <div
                          v-for="app in group.children"
                          :key="app.id"
                          class="tree-node tree-app-node"
                          :class="{ 'is-active': activeAppId === app.id }"
                          @click="handleAppNodeClick(app)"
                        >
                          <span class="app-dot"></span>
                          <span class="node-label">{{ app.label }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <!-- Main Content -->
      <div class="app-main">
        <!-- Main List Card -->
        <div class="page-card">
          <!-- Toolbar -->
          <div class="toolbar">
            <div class="toolbar-left">
              <el-input
                v-model="searchKeyword"
                placeholder="输入应用编号/应用名称/英文全称"
                style="width: 280px;"
                clearable
                @keyup.enter="handleQuery"
              >
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
              <el-button type="primary" @click="handleQuery">查询</el-button>
              <el-button @click="handleFilter">筛选</el-button>
            </div>
            <div class="toolbar-right">
              <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新建应用</el-button>
              <el-button @click="handleImport"><el-icon><Upload /></el-icon> 导入</el-button>
              <el-button @click="handleExport"><el-icon><Download /></el-icon> 导出</el-button>
              <el-button @click="handleConfigExport"><el-icon><Setting /></el-icon> 配置导出</el-button>
            </div>
          </div>

          <!-- Table -->
          <el-table :data="appList" v-loading="loading" stripe class="app-table">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="appCode" label="应用编号" width="100">
              <template #default="{ row }">
                <code style="color: var(--accent);">{{ row.appCode }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="appName" label="应用名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="appNameEn" label="英文简称" width="120" show-overflow-tooltip />
            <el-table-column prop="status" label="应用状态" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="departmentName" label="应用主管部门" width="130" show-overflow-tooltip />
            <el-table-column prop="implementationDivision" label="实施处室" width="120" show-overflow-tooltip />
            <el-table-column prop="implementationTeam" label="实施项目组" width="130" show-overflow-tooltip />
            <el-table-column prop="importanceLevel" label="是否重要应用" width="120">
              <template #default="{ row }">
                <span :class="row.importanceLevel === '核心' || row.importanceLevel === '重要' ? 'text-danger' : ''">
                  {{ row.importanceLevel === '核心' || row.importanceLevel === '重要' ? '是' : '否' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="recordStatus" label="记录状态" width="100">
              <template #default="{ row }">
                <span class="text-success">{{ row.recordStatus || '已发布' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="version" label="版本号" width="80">
              <template #default="{ row }">
                <span class="text-muted">v{{ row.version || '1.0' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <div class="operation-btns">
                  <el-button type="primary" text size="small" @click.stop="handleDetail(row)">查看</el-button>
                  <el-button type="primary" text size="small" @click.stop="handleEdit(row)">编辑</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- Pagination -->
          <div class="pagination-wrapper">
            <span class="pagination-total">共 {{ pagination.total }} 条</span>
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[20, 50, 100]"
              :total="pagination.total"
              layout="sizes, prev, pager, next, jumper"
              @size-change="loadData"
              @current-change="loadData"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Filter Dialog -->
    <el-dialog v-model="filterDialogVisible" title="筛选" width="600px" destroy-on-close>
      <el-form :model="filterForm" label-width="120px">
        <el-form-item label="应用类型">
          <el-select v-model="filterForm.appType" clearable style="width: 100%;">
            <el-option label="交易类" value="交易" />
            <el-option label="渠道类" value="渠道" />
            <el-option label="管理类" value="管理" />
            <el-option label="数据类" value="数据" />
            <el-option label="基础设施" value="基础设施" />
          </el-select>
        </el-form-item>
        <el-form-item label="重要级别">
          <el-select v-model="filterForm.importanceLevel" clearable style="width: 100%;">
            <el-option label="核心" value="核心" />
            <el-option label="重要" value="重要" />
            <el-option label="一般" value="一般" />
            <el-option label="辅助" value="辅助" />
          </el-select>
        </el-form-item>
        <el-form-item label="应用状态">
          <el-select v-model="filterForm.status" clearable style="width: 100%;">
            <el-option label="规划中" value="PLANNING" />
            <el-option label="开发中" value="DEVELOPMENT" />
            <el-option label="测试中" value="TESTING" />
            <el-option label="生产运行" value="PRODUCTION" />
            <el-option label="已下线" value="DEPRECATED" />
          </el-select>
        </el-form-item>
        <el-form-item label="主管部门">
          <el-select v-model="filterForm.departmentId" clearable style="width: 100%;">
            <el-option label="信息科技部" :value="1" />
            <el-option label="运维部" :value="2" />
            <el-option label="数据中心" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="系统所属层">
          <el-select v-model="filterForm.systemLayer" clearable style="width: 100%;">
            <el-option label="未分类用层" value="未分类用层" />
            <el-option label="数据中台层" value="数据中台层" />
            <el-option label="基础资源层" value="基础资源层" />
            <el-option label="业务中台层" value="业务中台层" />
            <el-option label="管理决策层" value="管理决策层" />
            <el-option label="用户交互层" value="用户交互层" />
            <el-option label="理财子公司" value="理财子公司" />
            <el-option label="产品服务层" value="产品服务层" />
            <el-option label="技术支撑层" value="技术支撑层" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否互联网应用">
          <el-radio-group v-model="filterForm.isInternetApp">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
            <el-radio :label="-1">不限</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否重要应用">
          <el-radio-group v-model="filterForm.isImportant">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
            <el-radio :label="-1">不限</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="filterDialogVisible = false">取消</el-button>
        <el-button @click="handleResetFilter">重置</el-button>
        <el-button type="primary" @click="handleApplyFilter">应用</el-button>
      </template>
    </el-dialog>

    <!-- Config Export Dialog -->
    <el-dialog v-model="exportDialogVisible" title="设置导出表头" width="700px" destroy-on-close>
      <div class="export-config">
        <el-checkbox v-model="checkAll" :indeterminate="isIndeterminate" @change="handleCheckAllChange">全选</el-checkbox>
        <el-divider />
        <div v-for="group in exportFieldGroups" :key="group.title" class="export-group">
          <div class="export-group-title">{{ group.title }}</div>
          <el-checkbox-group v-model="selectedExportFields">
            <el-checkbox v-for="field in group.fields" :key="field.key" :label="field.key" :disabled="field.required">
              {{ field.label }}
            </el-checkbox>
          </el-checkbox-group>
        </div>
      </div>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDoExport">确定导出</el-button>
      </template>
    </el-dialog>

    <!-- Import Dialog -->
    <el-dialog v-model="importDialogVisible" title="导入应用" width="500px" destroy-on-close>
      <div class="import-area">
        <el-upload
          ref="uploadRef"
          drag
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.xls"
          :on-change="handleFileChange"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <template #tip>
            <div class="el-upload__tip">只能上传xlsx/xls文件</div>
          </template>
        </el-upload>
        <div class="import-template">
          <el-button size="small" @click="handleDownloadTemplate">下载导入模板</el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDoImport">确定导入</el-button>
      </template>
    </el-dialog>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增应用' : '编辑应用'" width="1000px" destroy-on-close>
      <el-tabs v-model="formActiveTab" class="form-tabs">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="appForm" :rules="formRules" ref="formRef" label-width="140px">
            <el-form-item label="应用名称" prop="appName"><el-input v-model="appForm.appName" placeholder="请输入应用名称" /></el-form-item>
            <el-form-item label="英文全称" prop="appNameEn"><el-input v-model="appForm.appNameEn" placeholder="请输入英文全称" /></el-form-item>
            <el-form-item label="英文简称" prop="appNameEn"><el-input v-model="appForm.appNameEn" placeholder="请输入英文简称" /></el-form-item>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
              <el-form-item label="应用类型" prop="appType">
                <el-select v-model="appForm.appType" style="width: 100%;">
                  <el-option label="交易类" value="交易" />
                  <el-option label="渠道类" value="渠道" />
                  <el-option label="管理类" value="管理" />
                  <el-option label="数据类" value="数据" />
                  <el-option label="基础设施" value="基础设施" />
                </el-select>
              </el-form-item>
              <el-form-item label="重要级别" prop="importanceLevel">
                <el-select v-model="appForm.importanceLevel" style="width: 100%;">
                  <el-option label="核心" value="核心" />
                  <el-option label="重要" value="重要" />
                  <el-option label="一般" value="一般" />
                  <el-option label="辅助" value="辅助" />
                </el-select>
              </el-form-item>
              <el-form-item label="系统所属层" prop="systemLayer">
                <el-select v-model="appForm.systemLayer" style="width: 100%;">
                  <el-option label="未分类用层" value="未分类用层" />
                  <el-option label="数据中台层" value="数据中台层" />
                  <el-option label="基础资源层" value="基础资源层" />
                  <el-option label="业务中台层" value="业务中台层" />
                  <el-option label="管理决策层" value="管理决策层" />
                  <el-option label="用户交互层" value="用户交互层" />
                  <el-option label="理财子公司" value="理财子公司" />
                  <el-option label="产品服务层" value="产品服务层" />
                  <el-option label="技术支撑层" value="技术支撑层" />
                </el-select>
              </el-form-item>
              <el-form-item label="应用所属系统" prop="parentAppId">
                <el-select
                  v-model="appForm.parentAppId"
                  style="width: 100%;"
                  clearable
                  filterable
                  placeholder="请选择所属系统"
                >
                  <el-option
                    v-for="app in parentAppList"
                    :key="app.id"
                    :label="app.appName"
                    :value="app.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="生命周期" prop="lifecycle">
                <el-select v-model="appForm.lifecycle" style="width: 100%;">
                  <el-option label="规划中" value="PLANNING" />
                  <el-option label="开发中" value="DEVELOPMENT" />
                  <el-option label="测试中" value="TESTING" />
                  <el-option label="生产运行" value="PRODUCTION" />
                  <el-option label="已废弃" value="DEPRECATED" />
                </el-select>
              </el-form-item>
              <el-form-item label="业务域" prop="businessDomain"><el-input v-model="appForm.businessDomain" /></el-form-item>
              <el-form-item label="应用状态" prop="status">
                <el-select v-model="appForm.status" style="width: 100%;">
                  <el-option label="规划中" value="PLANNING" />
                  <el-option label="开发中" value="DEVELOPMENT" />
                  <el-option label="测试中" value="TESTING" />
                  <el-option label="生产" value="PRODUCTION" />
                  <el-option label="已下线" value="DEPRECATED" />
                </el-select>
              </el-form-item>
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
              <el-form-item label="主管部门" prop="departmentId">
                <el-select v-model="appForm.departmentId" style="width: 100%;">
                  <el-option label="信息科技部" :value="1" />
                  <el-option label="运维部" :value="2" />
                  <el-option label="数据中心" :value="3" />
                </el-select>
              </el-form-item>
              <el-form-item label="实施单位" prop="implementationUnit"><el-input v-model="appForm.implementationUnit" /></el-form-item>
              <el-form-item label="实施处室" prop="implementationDivision"><el-input v-model="appForm.implementationDivision" /></el-form-item>
              <el-form-item label="实施项目组" prop="implementationTeam"><el-input v-model="appForm.implementationTeam" /></el-form-item>
              <el-form-item label="投产时间" prop="goLiveDate"><el-input v-model="appForm.goLiveDate" placeholder="如: 2026-06-01" /></el-form-item>
              <el-form-item label="下线日期" prop="retireDate"><el-input v-model="appForm.retireDate" placeholder="如: 2026-12-01" /></el-form-item>
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
              <el-form-item label="主业务功能领域" prop="mainBusinessDomain"><el-input v-model="appForm.mainBusinessDomain" /></el-form-item>
              <el-form-item label="辅业务功能领域" prop="secondaryBusinessDomain"><el-input v-model="appForm.secondaryBusinessDomain" /></el-form-item>
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px;">
              <el-form-item label="是否互联网应用">
                <el-radio-group v-model="appForm.isInternetApp">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否支付应用">
                <el-radio-group v-model="appForm.isPaymentApp">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否电子银行">
                <el-radio-group v-model="appForm.isElectronicBankingApp">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否网上银行">
                <el-radio-group v-model="appForm.isOnlineBankingApp">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否移动应用">
                <el-radio-group v-model="appForm.isMobileApp">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否票据应用">
                <el-radio-group v-model="appForm.isBillApp">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否互联网金融">
                <el-radio-group v-model="appForm.isInternetFinanceApp">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="服务对象" prop="serviceObject"><el-input v-model="appForm.serviceObject" /></el-form-item>
            </div>
            <el-form-item label="应用简介" prop="description"><el-input v-model="appForm.description" type="textarea" :rows="3" /></el-form-item>
            <el-form-item label="备注" prop="remark"><el-input v-model="appForm.remark" type="textarea" :rows="2" /></el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 安全等级 -->
        <el-tab-pane label="安全等级" name="security">
          <el-form :model="appForm" label-width="200px">
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
              <el-form-item label="所属系统等级保护安全等级" prop="systemProtectionLevel">
                <el-select v-model="appForm.systemProtectionLevel" style="width: 100%;">
                  <el-option label="一级" value="一级" />
                  <el-option label="二级" value="二级" />
                  <el-option label="三级" value="三级" />
                  <el-option label="四级" value="四级" />
                </el-select>
              </el-form-item>
              <el-form-item label="等级保护安全等级" prop="protectionLevel">
                <el-select v-model="appForm.protectionLevel" style="width: 100%;">
                  <el-option label="第一级" value="第一级" />
                  <el-option label="第二级" value="第二级" />
                  <el-option label="第三级" value="第三级" />
                  <el-option label="第四级" value="第四级" />
                  <el-option label="第五级" value="第五级" />
                </el-select>
              </el-form-item>
            </div>
          </el-form>
        </el-tab-pane>

        <!-- 技术信息 -->
        <el-tab-pane label="技术信息" name="tech">
          <el-form :model="appForm" label-width="180px">
            <div class="form-section-title">基础实施信息</div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
              <el-form-item label="实施方式" prop="implementationMethod">
                <el-select v-model="appForm.implementationMethod" style="width: 100%;">
                  <el-option label="外购产品" value="外购产品" />
                  <el-option label="定制化产品" value="定制化产品" />
                </el-select>
              </el-form-item>
              <el-form-item label="实施类型" prop="implementationType">
                <el-select v-model="appForm.implementationType" style="width: 100%;">
                  <el-option label="内部管理类" value="内部管理类" />
                  <el-option label="业务处理类" value="业务处理类" />
                  <el-option label="渠道类" value="渠道类" />
                </el-select>
              </el-form-item>
            </div>

            <div class="form-section-title">安全基础信息</div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
              <el-form-item label="登录用户字段名称" prop="loginUserField"><el-input v-model="appForm.loginUserField" /></el-form-item>
              <el-form-item label="登录密码字段名称" prop="loginPasswordField"><el-input v-model="appForm.loginPasswordField" /></el-form-item>
              <el-form-item label="是否部署在DMZ区">
                <el-radio-group v-model="appForm.isDeployedDmz">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否存在上传功能">
                <el-radio-group v-model="appForm.hasUploadFunction">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="账号密码错误冻结机制" prop="pwdErrorFreezeMechanism"><el-input v-model="appForm.pwdErrorFreezeMechanism" /></el-form-item>
              <el-form-item label="是否存在主动外发请求">
                <el-radio-group v-model="appForm.hasOutboundRequest">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否存在下载功能">
                <el-radio-group v-model="appForm.hasDownloadFunction">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否APP应用">
                <el-radio-group v-model="appForm.isAppApplication">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>
            <el-form-item label="外发业务场景描述" prop="outboundRequestDesc"><el-input v-model="appForm.outboundRequestDesc" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="上传的功能描述和URL" prop="uploadFunctionDesc"><el-input v-model="appForm.uploadFunctionDesc" type="textarea" :rows="2" /></el-form-item>
            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px;">
              <el-form-item label="文件上传路径" prop="uploadFilePath"><el-input v-model="appForm.uploadFilePath" /></el-form-item>
              <el-form-item label="执行权限" prop="uploadPathExecutable">
                <el-select v-model="appForm.uploadPathExecutable" style="width: 100%;">
                  <el-option label="可执行" value="可执行" />
                  <el-option label="不可执行" value="不可执行" />
                </el-select>
              </el-form-item>
              <el-form-item label="上传文件类型" prop="uploadFileTypes"><el-input v-model="appForm.uploadFileTypes" /></el-form-item>
            </div>
            <el-form-item label="下载的功能描述和URL" prop="downloadFunctionDesc"><el-input v-model="appForm.downloadFunctionDesc" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="下载文件类型" prop="downloadFileTypes"><el-input v-model="appForm.downloadFileTypes" /></el-form-item>
            <el-form-item label="开源有关信息" prop="openSourceInfo"><el-input v-model="appForm.openSourceInfo" placeholder="如: shiro, log4j, quartz, activiti" /></el-form-item>
            <el-form-item label="访问主页" prop="accessUrl"><el-input v-model="appForm.accessUrl" /></el-form-item>

            <div class="form-section-title">其他信息</div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
              <el-form-item label="域名" prop="domainName"><el-input v-model="appForm.domainName" /></el-form-item>
              <el-form-item label="网络结构模式" prop="networkMode">
                <el-select v-model="appForm.networkMode" style="width: 100%;">
                  <el-option label="B/S" value="B/S" />
                  <el-option label="C/S" value="C/S" />
                  <el-option label="混合" value="混合" />
                </el-select>
              </el-form-item>
              <el-form-item label="访问方式" prop="accessType">
                <el-select v-model="appForm.accessType" style="width: 100%;">
                  <el-option label="浏览器" value="浏览器" />
                  <el-option label="客户端" value="客户端" />
                  <el-option label="APP" value="APP" />
                </el-select>
              </el-form-item>
              <el-form-item label="实施商信息" prop="vendorInfo"><el-input v-model="appForm.vendorInfo" /></el-form-item>
              <el-form-item label="认证方式" prop="authMethod"><el-input v-model="appForm.authMethod" /></el-form-item>
              <el-form-item label="是否与外部第三方系统对接">
                <el-radio-group v-model="appForm.hasThirdPartyIntegration">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="互联网线路类型">
                <el-radio-group v-model="appForm.isInternetLine">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>
            <el-form-item label="与外部第三方系统对接补充说明" prop="thirdPartyIntegrationDesc"><el-input v-model="appForm.thirdPartyIntegrationDesc" type="textarea" :rows="2" /></el-form-item>

            <div class="form-section-title">信创情况</div>
            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px;">
              <el-form-item label="应用是否上信创容器云" prop="xcCloudStatus">
                <el-select v-model="appForm.xcCloudStatus" style="width: 100%;">
                  <el-option label="已上云" value="已上云" />
                  <el-option label="未上云" value="未上云" />
                </el-select>
              </el-form-item>
              <el-form-item label="服务器信创情况" prop="serverXcStatus">
                <el-select v-model="appForm.serverXcStatus" style="width: 100%;">
                  <el-option label="全栈信创" value="全栈信创" />
                  <el-option label="部分信创" value="部分信创" />
                  <el-option label="非信创" value="非信创" />
                </el-select>
              </el-form-item>
              <el-form-item label="是否全栈信创">
                <el-radio-group v-model="appForm.isFullXc">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否使用第三方产品">
                <el-radio-group v-model="appForm.hasThirdPartyProduct">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="第三方产品是否信创">
                <el-radio-group v-model="appForm.thirdPartyIsXc">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="XC分类" prop="xcClassification">
                <el-select v-model="appForm.xcClassification" style="width: 100%;">
                  <el-option label="一般" value="一般" />
                  <el-option label="重要" value="重要" />
                  <el-option label="核心" value="核心" />
                </el-select>
              </el-form-item>
              <el-form-item label="数据库服务器信创情况" prop="dbServerXcStatus">
                <el-select v-model="appForm.dbServerXcStatus" style="width: 100%;">
                  <el-option label="全栈信创" value="全栈信创" />
                  <el-option label="部分信创" value="部分信创" />
                  <el-option label="非信创" value="非信创" />
                </el-select>
              </el-form-item>
              <el-form-item label="数据库服务器操作系统" prop="dbServerOs">
                <el-select v-model="appForm.dbServerOs" style="width: 100%;">
                  <el-option label="麒麟" value="麒麟" />
                  <el-option label="统信" value="统信" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
              <el-form-item label="数据库信创情况" prop="databaseXcStatus">
                <el-select v-model="appForm.databaseXcStatus" style="width: 100%;">
                  <el-option label="全栈信创" value="全栈信创" />
                  <el-option label="部分信创" value="部分信创" />
                  <el-option label="非信创" value="非信创" />
                </el-select>
              </el-form-item>
            </div>
            <el-form-item label="服务器信创情况说明" prop="serverXcDesc"><el-input v-model="appForm.serverXcDesc" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="数据库信创情况说明" prop="databaseXcDesc"><el-input v-model="appForm.databaseXcDesc" placeholder="如: 使用GDB数据库" /></el-form-item>
            <el-form-item label="非全栈信创情况" prop="partialXcDesc"><el-input v-model="appForm.partialXcDesc" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="第三方产品信创情况说明" prop="thirdPartyXcDesc"><el-input v-model="appForm.thirdPartyXcDesc" /></el-form-item>
            <el-form-item label="信创整体情况说明" prop="xcOverallDesc"><el-input v-model="appForm.xcOverallDesc" type="textarea" :rows="2" /></el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 服务信息 -->
        <el-tab-pane label="服务信息" name="service">
          <el-form :model="appForm" label-width="160px">
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
              <el-form-item label="客户类型" prop="customerType">
                <el-select v-model="appForm.customerType" style="width: 100%;">
                  <el-option label="行内客户" value="行内客户" />
                  <el-option label="外部客户" value="外部客户" />
                  <el-option label="混合" value="混合" />
                </el-select>
              </el-form-item>
              <el-form-item label="服务时间类型" prop="serviceTimeType">
                <el-select v-model="appForm.serviceTimeType" style="width: 100%;">
                  <el-option label="7*24" value="7*24" />
                  <el-option label="5*8" value="5*8" />
                  <el-option label="定制" value="定制" />
                </el-select>
              </el-form-item>
              <el-form-item label="内部用户范围" prop="internalUserScope"><el-input v-model="appForm.internalUserScope" /></el-form-item>
            </div>
            <el-form-item label="服务窗口补充说明" prop="serviceWindowDesc"><el-input v-model="appForm.serviceWindowDesc" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="使用范围补充说明" prop="usageScopeDesc"><el-input v-model="appForm.usageScopeDesc" type="textarea" :rows="2" /></el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 运维信息 -->
        <el-tab-pane label="运维信息" name="ops">
          <el-form :model="appForm" label-width="180px">
            <div class="form-section-title">容灾配置</div>
            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px;">
              <el-form-item label="同城RPO" prop="cityRpo"><el-input v-model="appForm.cityRpo" /></el-form-item>
              <el-form-item label="同城RTO" prop="cityRto"><el-input v-model="appForm.cityRto" /></el-form-item>
              <el-form-item label="同城双活类型" prop="cityActiveType"><el-input v-model="appForm.cityActiveType" /></el-form-item>
              <el-form-item label="异地RPO" prop="remoteRpo"><el-input v-model="appForm.remoteRpo" /></el-form-item>
              <el-form-item label="异地RTO" prop="remoteRto"><el-input v-model="appForm.remoteRto" /></el-form-item>
              <el-form-item label="异地双活类型" prop="remoteActiveType"><el-input v-model="appForm.remoteActiveType" /></el-form-item>
              <el-form-item label="是否具备同城环境">
                <el-radio-group v-model="appForm.hasCityEnvironment">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否具备灾备环境">
                <el-radio-group v-model="appForm.hasDrEnvironment">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="灾备等级" prop="drLevel">
                <el-select v-model="appForm.drLevel" style="width: 100%;">
                  <el-option label="A类" value="A类" />
                  <el-option label="B类" value="B类" />
                  <el-option label="C类" value="C类" />
                  <el-option label="D类" value="D类" />
                </el-select>
              </el-form-item>
              <el-form-item label="灾备恢复能力等级" prop="drRecoveryLevel">
                <el-select v-model="appForm.drRecoveryLevel" style="width: 100%;">
                  <el-option label="1级" value="1级" />
                  <el-option label="2级" value="2级" />
                  <el-option label="3级" value="3级" />
                  <el-option label="4级" value="4级" />
                </el-select>
              </el-form-item>
            </div>

            <div class="form-section-title">运维配置</div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
              <el-form-item label="运维等级" prop="opsLevel">
                <el-select v-model="appForm.opsLevel" style="width: 100%;">
                  <el-option label="A类" value="A类" />
                  <el-option label="B类" value="B类" />
                  <el-option label="C类" value="C类" />
                  <el-option label="D类" value="D类" />
                </el-select>
              </el-form-item>
              <el-form-item label="旧运维等级" prop="oldOpsLevel">
                <el-select v-model="appForm.oldOpsLevel" style="width: 100%;">
                  <el-option label="A类" value="A类" />
                  <el-option label="B类" value="B类" />
                  <el-option label="C类" value="C类" />
                  <el-option label="D类" value="D类" />
                </el-select>
              </el-form-item>
              <el-form-item label="运维单位" prop="opsUnit"><el-input v-model="appForm.opsUnit" /></el-form-item>
              <el-form-item label="远程访问权限分类" prop="remoteAccessClass">
                <el-select v-model="appForm.remoteAccessClass" style="width: 100%;">
                  <el-option label="A类" value="A类" />
                  <el-option label="B类" value="B类" />
                  <el-option label="C类" value="C类" />
                </el-select>
              </el-form-item>
              <el-form-item label="是否变更自动化">
                <el-radio-group v-model="appForm.isChangeAutomation">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="变更投产时点" prop="changeDeployTime"><el-input v-model="appForm.changeDeployTime" placeholder="如: 18:00-20:00" /></el-form-item>
              <el-form-item label="主要业务时段" prop="mainBusinessHours"><el-input v-model="appForm.mainBusinessHours" placeholder="如: 08:30-18:00" /></el-form-item>
              <el-form-item label="是否容器化部署">
                <el-radio-group v-model="appForm.isContainerized">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>
            <el-form-item label="变更投产时点补充说明" prop="changeDeployTimeDesc"><el-input v-model="appForm.changeDeployTimeDesc" /></el-form-item>
            <el-form-item label="电子数据资产提取审批部门" prop="dataAssetApprovalDept"><el-input v-model="appForm.dataAssetApprovalDept" /></el-form-item>

            <div class="form-section-title">部署配置</div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
              <el-form-item label="部署环境" prop="deploymentEnvironment">
                <el-select v-model="appForm.deploymentEnvironment" style="width: 100%;">
                  <el-option label="生产环境" value="生产环境" />
                  <el-option label="测试环境" value="测试环境" />
                  <el-option label="开发环境" value="开发环境" />
                  <el-option label="准生产环境" value="准生产环境" />
                </el-select>
              </el-form-item>
              <el-form-item label="部署地点" prop="deploymentLocation"><el-input v-model="appForm.deploymentLocation" placeholder="如: 南海生产机房" /></el-form-item>
            </div>
            <el-form-item label="部署地点补充说明" prop="deploymentLocationDesc"><el-input v-model="appForm.deploymentLocationDesc" /></el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Filter, Plus, Clock, Upload, Download, Setting, Refresh, UploadFilled, CaretRight } from '@element-plus/icons-vue'
import { archAppApi } from '@/api'
import { useTabsStore } from '@/store/tabs'
import * as XLSX from 'xlsx'

const router = useRouter()
const tabsStore = useTabsStore()

// Parent app list for selection
const parentAppList = ref([])
const loadParentAppList = async () => {
  try {
    const res = await archAppApi.list({ page: 1, pageSize: 1000 })
    parentAppList.value = res.data.records || []
  } catch (e) {
    console.error('加载父应用列表失败', e)
    parentAppList.value = []
  }
}

// Sidebar - Two Level Hierarchy
const sidebarKeyword = ref('')
const sidebarTreeRef = ref(null)
const sidebarData = ref([])
const expandedKeys = ref(['head-office-root']) // 默认展开总行应用系统
const activeNodeId = ref(null)
const activeAppId = ref(null)
const searchResults = ref([])
const currentFilter = reactive({
  systemLayer: '',  // 应用分层
  parentAppName: '' // 应用群组
})

// Load sidebar tree data
const loadSidebarTree = async () => {
  try {
    const res = await archAppApi.getLayeredTree()
    sidebarData.value = res.data || []
    // 默认展开根节点
    if (sidebarData.value.length > 0) {
      expandedKeys.value = [sidebarData.value[0].id]
    }
  } catch (e) {
    console.error('加载侧边栏树失败', e)
    // 使用空数据
    sidebarData.value = []
  }
}

// Toggle expand/collapse
const toggleExpand = (key) => {
  const index = expandedKeys.value.indexOf(key)
  if (index > -1) {
    expandedKeys.value.splice(index, 1)
  } else {
    expandedKeys.value.push(key)
  }
}

// Search functionality
const handleSidebarSearch = async () => {
  if (!sidebarKeyword.value || sidebarKeyword.value.trim() === '') {
    searchResults.value = []
    return
  }
  try {
    const res = await archAppApi.searchApps(sidebarKeyword.value)
    searchResults.value = res.data || []
  } catch (e) {
    console.error('搜索失败', e)
    searchResults.value = []
  }
}

const clearSearch = () => {
  sidebarKeyword.value = ''
  searchResults.value = []
}

// Handle search result click
const handleSearchResultClick = (item) => {
  activeAppId.value = item.id
  searchKeyword.value = item.label
  pagination.page = 1
  loadData()
}

// Handle group node click - filter by 应用群组
const handleGroupNodeClick = (group, layer) => {
  expandedKeys.value.includes(group.id)
    ? toggleExpand(group.id)
    : expandedKeys.value.push(group.id)
  activeNodeId.value = group.id
  currentFilter.systemLayer = layer.label
  currentFilter.parentAppName = group.label
  searchKeyword.value = ''
  pagination.page = 1
  loadData()
}

// Handle app node click
const handleAppNodeClick = (app) => {
  activeAppId.value = app.id
  handleDetail(app)
}

// Legacy sidebar node click (for compatibility)
const handleSidebarNodeClick = (data) => {
  if (data.id === 'my') {
    searchKeyword.value = ''
    filterForm.appType = ''
    filterForm.importanceLevel = ''
    filterForm.status = ''
    pagination.page = 1
    loadData()
  } else if (data.id === 'unpublished') {
    searchKeyword.value = ''
    filterForm.status = 'PLANNING'
    pagination.page = 1
    loadData()
  } else if (data.id === 'institution') {
    searchKeyword.value = ''
    filterForm.systemLayer = '总行级'
    pagination.page = 1
    loadData()
  }
}

// Search & Filter
const searchKeyword = ref('')
const filterDialogVisible = ref(false)
const filterForm = reactive({
  appType: '',
  importanceLevel: '',
  status: '',
  departmentId: null,
  systemLayer: '',
  isInternetApp: -1,
  isImportant: -1
})

// Table
const loading = ref(false)
const appList = ref([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

// Export
const exportDialogVisible = ref(false)
const checkAll = ref(true)
const isIndeterminate = ref(false)
const selectedExportFields = ref([
  'appCode', 'appName', 'appNameEn', 'status', 'departmentName',
  'implementationUnit', 'implementationDivision', 'implementationTeam',
  'importanceLevel', 'systemLayer'
])

const exportFieldGroups = [
  {
    title: '基本属性',
    fields: [
      { key: 'appCode', label: '应用编号', required: true },
      { key: 'appName', label: '应用名称', required: true },
      { key: 'appNameEn', label: '英文简称', required: true },
      { key: 'systemLayer', label: '系统所属层' },
      { key: 'remark', label: '备注' },
      { key: 'parentAppName', label: '应用所属系统' },
      { key: 'implementationUnit', label: '实施单位' },
      { key: 'goLiveDate', label: '投产时间' },
      { key: 'retireDate', label: '下线日期' },
      { key: 'mainBusinessDomain', label: '主业务功能领域' },
      { key: 'secondaryBusinessDomain', label: '辅业务功能领域' },
      { key: 'description', label: '应用简介' }
    ]
  },
  {
    title: '分类信息',
    fields: [
      { key: 'status', label: '应用状态', required: true },
      { key: 'appType', label: '应用类型' },
      { key: 'importanceLevel', label: '重要级别' },
      { key: 'departmentName', label: '应用主管部门' },
      { key: 'isInternetApp', label: '是否互联网应用' },
      { key: 'serviceObject', label: '服务对象' },
      { key: 'isPaymentApp', label: '是否支付应用' },
      { key: 'isElectronicBankingApp', label: '是否电子银行应用' },
      { key: 'isOnlineBankingApp', label: '是否网上银行应用' },
      { key: 'isMobileApp', label: '是否移动应用' },
      { key: 'recordStatus', label: '记录状态' },
      { key: 'version', label: '版本号' }
    ]
  },
  {
    title: '实施信息',
    fields: [
      { key: 'implementationDivision', label: '实施处室', required: true },
      { key: 'implementationTeam', label: '实施项目组', required: true }
    ]
  }
]

const allExportKeys = computed(() => exportFieldGroups.flatMap(g => g.fields.map(f => f.key)))

// Import
const importDialogVisible = ref(false)
const uploadRef = ref(null)
const importFile = ref(null)

// Form
const dialogVisible = ref(false)
const dialogMode = ref('add')
const formActiveTab = ref('basic')
const formRef = ref(null)

const defaultForm = {
  id: null, appName: '', appNameEn: '', appType: '交易', importanceLevel: '一般',
  systemLayer: '', lifecycle: 'PLANNING', businessDomain: '', status: 'PLANNING',
  departmentId: null, departmentName: '', implementationUnit: '', implementationDivision: '',
  implementationTeam: '', goLiveDate: '', retireDate: '',
  mainBusinessDomain: '', secondaryBusinessDomain: '', serviceObject: '',
  parentAppId: null,
  description: '', remark: '',
  isInternetApp: 0, isPaymentApp: 0, isElectronicBankingApp: 0, isOnlineBankingApp: 0,
  isMobileApp: 0, isBillApp: 0, isInternetFinanceApp: 0,
  systemProtectionLevel: '', protectionLevel: '',
  implementationMethod: '', implementationType: '',
  loginUserField: '', loginPasswordField: '', isDeployedDmz: 0, hasUploadFunction: 0,
  pwdErrorFreezeMechanism: '', hasOutboundRequest: 0, outboundRequestDesc: '',
  uploadFunctionDesc: '', uploadFilePath: '', uploadPathExecutable: '', uploadFileTypes: '',
  hasDownloadFunction: 0, downloadFunctionDesc: '', downloadFileTypes: '', isAppApplication: 0,
  openSourceInfo: '', accessUrl: '',
  domainName: '', networkMode: '', vendorInfo: '', authMethod: '',
  hasThirdPartyIntegration: 0, thirdPartyIntegrationDesc: '', isInternetLine: 0,
  xcCloudStatus: '', serverXcDesc: '', serverXcStatus: '', hasThirdPartyProduct: 0,
  thirdPartyIsXc: 0, isFullXc: 0, dbServerXcStatus: '', dbServerOs: '',
  databaseXcStatus: '', databaseXcDesc: '', partialXcDesc: '', xcClassification: '',
  thirdPartyXcDesc: '', xcOverallDesc: '',
  customerType: '', serviceTimeType: '', serviceWindowDesc: '', internalUserScope: '', usageScopeDesc: '',
  cityRpo: '', cityRto: '', cityActiveType: '', hasCityEnvironment: 0,
  remoteRpo: '', remoteRto: '', remoteActiveType: '', hasDrEnvironment: 0,
  opsLevel: '', oldOpsLevel: '', opsUnit: '', remoteAccessClass: '',
  isChangeAutomation: 0, changeDeployTime: '', changeDeployTimeDesc: '',
  mainBusinessHours: '', dataAssetApprovalDept: '', isContainerized: 0,
  deploymentEnvironment: '', deploymentLocation: '', deploymentLocationDesc: '',
  drLevel: '', drRecoveryLevel: ''
}

const appForm = reactive({ ...defaultForm })

const formRules = {
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  appType: [{ required: true, message: '请选择应用类型', trigger: 'change' }]
}

// Methods
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (filterForm.appType) params.appType = filterForm.appType
    if (filterForm.importanceLevel) params.importanceLevel = filterForm.importanceLevel
    if (filterForm.status) params.status = filterForm.status
    if (filterForm.departmentId) params.departmentId = filterForm.departmentId
    if (filterForm.systemLayer) params.systemLayer = filterForm.systemLayer
    if (filterForm.isInternetApp === 1) params.isInternetApp = 1
    if (filterForm.isImportant === 1) params.isImportant = 1
    // 支持通过应用分层（systemLayer）和应用群组（parentAppName）筛选
    if (currentFilter.systemLayer) params.systemLayer = currentFilter.systemLayer
    if (currentFilter.parentAppName) params.parentAppName = currentFilter.parentAppName

    const res = await archAppApi.list(params)
    appList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pagination.page = 1
  loadData()
}

const handleFilter = () => {
  filterDialogVisible.value = true
}

const handleResetFilter = () => {
  Object.assign(filterForm, {
    appType: '',
    importanceLevel: '',
    status: '',
    departmentId: null,
    systemLayer: '',
    isInternetApp: -1,
    isImportant: -1
  })
}

const handleApplyFilter = () => {
  filterDialogVisible.value = false
  pagination.page = 1
  loadData()
}

const handleAdd = () => {
  dialogMode.value = 'add'
  Object.assign(appForm, defaultForm)
  formActiveTab.value = 'basic'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  if (row && row.application) {
    Object.assign(appForm, row.application)
  } else if (row) {
    Object.assign(appForm, row)
  }
  dialogVisible.value = true
}

const handleDetail = (row) => {
  const path = `/arch/application/${row.id}`
  tabsStore.addTab(path, row.appName)
  router.push(path)
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (dialogMode.value === 'add') {
      await archAppApi.create(appForm)
    } else {
      await archAppApi.update(appForm.id, appForm)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleHistoryName = () => {
  ElMessage.info('历史名称查询功能开发中')
}

const handleImport = () => {
  importFile.value = null
  importDialogVisible.value = true
}

const handleFileChange = (file) => {
  importFile.value = file.raw
}

const handleDownloadTemplate = async () => {
  try {
    loading.value = true
    const res = await archAppApi.getImportTemplate()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'application_import_template.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (e) {
    ElMessage.error('模板下载失败')
  } finally {
    loading.value = false
  }
}

const handleDoImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  try {
    const formData = new FormData()
    formData.append('file', importFile.value)
    const res = await archAppApi.import(formData)
    const importResult = res.data || {}
    const updated = importResult.updated || 0
    const created = importResult.created || 0
    ElMessage.success(`导入完成：新增 ${created} 条，更新 ${updated} 条`)
    importDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e?.message || e?.response?.data?.message || '导入失败')
  }
}

const handleExport = async () => {
  loading.value = true
  try {
    const params = { page: 1, pageSize: 10000 }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await archAppApi.list(params)
    const data = res.data.records || []
    exportToExcel(data, '应用列表导出')
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

const handleConfigExport = () => {
  exportDialogVisible.value = true
}

const handleCheckAllChange = (val) => {
  selectedExportFields.value = val ? allExportKeys.value : []
  isIndeterminate.value = false
}

const handleDoExport = async () => {
  exportDialogVisible.value = false
  loading.value = true
  try {
    const params = { page: 1, pageSize: 10000 }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await archAppApi.list(params)
    const data = res.data.records || []

    const fieldMap = {
      appCode: '应用编号', appName: '应用名称', appNameEn: '英文简称',
      status: '应用状态', departmentName: '应用主管部门', implementationDivision: '实施处室',
      implementationTeam: '实施项目组', importanceLevel: '重要级别', recordStatus: '记录状态',
      version: '版本号', systemLayer: '系统所属层', remark: '备注',
      parentAppName: '应用所属系统', implementationUnit: '实施单位', goLiveDate: '投产时间',
      retireDate: '下线日期', mainBusinessDomain: '主业务功能领域', description: '应用简介',
      appType: '应用类型', serviceObject: '服务对象', isInternetApp: '是否互联网应用',
      secondaryBusinessDomain: '辅业务功能领域',
      isPaymentApp: '是否支付应用', isElectronicBankingApp: '是否电子银行应用',
      isOnlineBankingApp: '是否网上银行应用', isMobileApp: '是否移动应用'
    }

    const exportData = data.map(item => {
      const row = {}
      selectedExportFields.value.forEach(key => {
        let value = item[key]
        if (key === 'status') value = getStatusText(value)
        if (key === 'isInternetApp') value = value === 1 ? '是' : '否'
        if (key === 'isPaymentApp') value = value === 1 ? '是' : '否'
        if (key === 'isElectronicBankingApp') value = value === 1 ? '是' : '否'
        if (key === 'isOnlineBankingApp') value = value === 1 ? '是' : '否'
        if (key === 'isMobileApp') value = value === 1 ? '是' : '否'
        row[fieldMap[key] || key] = value || ''
      })
      return row
    })

    exportToExcel(exportData, '应用列表导出')
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

const exportToExcel = (data, filename) => {
  const worksheet = XLSX.utils.json_to_sheet(data)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, '应用列表')
  XLSX.writeFile(workbook, `${filename}_${new Date().toISOString().slice(0, 10)}.xlsx`)
}

const handleSyncConfig = () => {
  ElMessage.info('正在同步配置库...')
  setTimeout(() => {
    ElMessage.success('同步完成')
  }, 1500)
}

const getStatusType = (status) => {
  const map = {
    'PRODUCTION': 'success',
    'DEVELOPMENT': 'warning',
    'TESTING': 'info',
    'PLANNING': '',
    'DEPRECATED': 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'PRODUCTION': '已上线',
    'DEVELOPMENT': '建设中',
    'TESTING': '测试中',
    'PLANNING': '规划中',
    'DEPRECATED': '已下线'
  }
  return map[status] || status || ''
}

onMounted(() => {
  loadSidebarTree()
  loadData()
  loadParentAppList()
})
</script>

<style scoped lang="scss">
.arch-application {
  height: 100%;
  overflow: hidden;
}

.app-layout {
  display: flex;
  height: 100%;
  gap: 0;
  overflow: hidden;
}

.app-sidebar {
  background: var(--bg-card);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow: hidden;

  // Two-level hierarchy sidebar
  &.layered-sidebar {
    width: 280px;
    min-width: 280px;
  }
}

.sidebar-header {
  padding: 12px;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;

  :deep(.el-input__wrapper) {
    background: var(--bg-dark);
    border: none;
    box-shadow: none;
  }

  :deep(.el-input__inner) {
    font-size: 12px;
    &::placeholder {
      color: var(--text-muted);
    }
  }
}

.sidebar-scroll {
  flex: 1;
  padding: 8px 0;
  overflow-y: auto;
}

// Search Results
.search-results {
  padding: 8px;

  &-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    font-size: 12px;
    color: var(--text-secondary);
    border-bottom: 1px solid var(--border);
    margin-bottom: 8px;
  }
}

.search-result-item {
  padding: 10px 12px;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 4px;
  transition: background 0.2s;

  &:hover {
    background: var(--bg-dark);
  }

  .result-name {
    display: block;
    font-size: 13px;
    color: var(--text-primary);
    margin-bottom: 6px;
  }

  .result-tags {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }
}

// Tree Container
.tree-container {
  padding: 0 8px;
}

// Tree Nodes
.tree-node {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
  user-select: none;

  &:hover {
    background: var(--bg-dark);
  }

  &.is-active {
    background: rgba(230, 162, 60, 0.15);
  }

  .node-arrow {
    width: 16px;
    height: 16px;
    margin-right: 6px;
    color: var(--text-muted);
    transition: transform 0.2s;
    flex-shrink: 0;

    svg {
      width: 100%;
      height: 100%;
    }
  }

  .no-children-dot {
    width: 6px;
    height: 6px;
    background: var(--text-muted);
    border-radius: 50%;
    margin-left: 5px;
    margin-right: 5px;
  }

  .node-label {
    flex: 1;
    font-size: 13px;
    color: var(--text-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .node-count {
    font-size: 11px;
    color: var(--text-muted);
    background: var(--bg-dark);
    padding: 2px 6px;
    border-radius: 10px;
    margin-left: 8px;
    flex-shrink: 0;
  }
}

// Root Node: 总行应用系统
.tree-root-node {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  padding: 12px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 4px;

  .node-arrow {
    color: var(--accent);
  }
}

// Level indicators
.tree-level {
  // Level 1: 应用分层
  &.level-1 {
    padding-left: 0;
  }

  // Level 2: 应用群组
  &.level-2 {
    padding-left: 16px;
  }

  // Level 3: 具体应用
  &.level-3 {
    padding-left: 32px;
  }
}

// Tree Branch
.tree-branch {
  margin-bottom: 2px;
}

// Layer Node (应用分层) - Red Tag
.tree-layer-node {
  .node-label {
    font-weight: 500;
  }
}

// Group Node (应用群组) - Green/Teal Tag
.tree-group-node {
  padding-left: 24px;

  .node-label {
    font-weight: 400;
    color: var(--text-secondary);
  }
}

// App Node (具体应用)
.tree-app-node {
  padding-left: 40px;
  font-size: 12px;

  .app-dot {
    width: 5px;
    height: 5px;
    background: var(--accent);
    border-radius: 50%;
    margin-right: 8px;
    flex-shrink: 0;
  }

  .node-label {
    color: var(--text-secondary);
    font-size: 12px;
  }

  &:hover .node-label {
    color: var(--accent);
  }
}

// Tags
.tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 10px;
  border-radius: 3px;
  font-weight: 500;
  flex-shrink: 0;

  &.tag-layer {
    // Red/Coral for 应用分层
    background: linear-gradient(135deg, #E64A19, #D32F2F);
    color: #fff;
    box-shadow: 0 1px 3px rgba(230, 74, 25, 0.3);
  }

  &.tag-group {
    // Teal/Green for 应用群组
    background: linear-gradient(135deg, #00897B, #00695C);
    color: #fff;
    box-shadow: 0 1px 3px rgba(0, 137, 123, 0.3);
  }
}

// Expanded state arrow rotation
.tree-node.is-expanded > .node-arrow {
  transform: rotate(90deg);
}

.app-main {
  flex: 1;
  padding: var(--space-lg);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.page-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
  flex-wrap: wrap;
  gap: 8px;

  &-left {
    display: flex;
    gap: 8px;
    align-items: center;
  }

  &-right {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }
}

.app-table {
  flex: 1;
  overflow: hidden;

  :deep(.el-table__header th) {
    background: var(--bg-dark);
    color: var(--text-primary);
    font-weight: 600;
  }

  :deep(.el-table__body-wrapper) {
    overflow-y: auto;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--space-lg);
  padding-top: var(--space-lg);
  border-top: 1px solid var(--border);

  .pagination-total {
    color: var(--text-secondary);
    font-size: 0.875rem;
  }
}

.text-danger {
  color: var(--danger);
}

.text-success {
  color: var(--success);
}

.text-muted {
  color: var(--text-muted);
}

.operation-btns {
  display: flex;
  gap: 4px;
}

.export-config {
  max-height: 500px;
  overflow-y: auto;
}

.export-group {
  margin-bottom: 16px;

  &-title {
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 8px;
    padding-left: 8px;
    border-left: 3px solid var(--accent);
  }

  :deep(.el-checkbox) {
    margin: 4px 12px 4px 0;
    width: 140px;
  }
}

.import-area {
  text-align: center;

  :deep(.el-upload-dragger) {
    padding: 40px 20px;
  }
}

.import-template {
  margin-top: 16px;
  text-align: center;
}

.form-tabs {
  :deep(.el-tabs__header) {
    margin: 0 0 16px;
  }
  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }
  :deep(.el-tabs__item) {
    padding: 0 16px;
    height: 36px;
    line-height: 36px;
    font-size: 0.875rem;
  }
}

.form-section-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 16px 0 12px;
  padding-left: 8px;
  border-left: 3px solid var(--accent);
}
</style>
