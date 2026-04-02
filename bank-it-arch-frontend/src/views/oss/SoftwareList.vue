<template>
  <div class="oss-software-page">
    <div class="page-card">
      <!-- Tabs -->
      <el-tabs v-model="activeTab" class="oss-tabs">
        <el-tab-pane label="软件清单" name="software">
          <!-- Query Form - Horizontal Layout -->
          <div class="query-section">
            <div class="query-row">
              <div class="query-item">
                <label class="query-label">开源软件名称</label>
                <el-input
                  v-model="queryForm.swName"
                  placeholder="请输入"
                  clearable
                  class="query-input"
                />
              </div>
              <div class="query-item">
                <label class="query-label">软件分类</label>
                <el-select v-model="queryForm.swCategory" placeholder="请选择" clearable class="query-input">
                  <el-option label="开源基础软件" value="BASE" />
                  <el-option label="开源工具软件" value="TOOL" />
                  <el-option label="开源组件" value="CMPNT" />
                </el-select>
              </div>
              <div class="query-item">
                <label class="query-label">软件类型</label>
                <el-select v-model="queryForm.swType" placeholder="请选择" clearable class="query-input">
                  <el-option label="主推软件" value="MAIN" />
                  <el-option label="非主推软件" value="LIMIT" />
                  <el-option label="已退出软件" value="QUIT" />
                </el-select>
              </div>
              <div class="query-item">
                <label class="query-label">责任团队</label>
                <el-select v-model="queryForm.rspTeamId" placeholder="请选择" clearable filterable class="query-input">
                  <el-option v-for="team in teamList" :key="team.id" :label="team.teamName" :value="team.id" />
                </el-select>
              </div>
              <div class="query-item">
                <label class="query-label">责任人</label>
                <el-input v-model="queryForm.rspUserName" placeholder="请输入" clearable class="query-input" />
              </div>
              <div class="query-actions">
                <el-button @click="handleReset" class="btn-reset">重置</el-button>
                <el-button type="primary" @click="handleQuery" class="btn-search">
                  <el-icon><Search /></el-icon> 查询
                </el-button>
              </div>
            </div>
          </div>

          <!-- Toolbar -->
          <div class="toolbar">
            <div class="toolbar-left">
              <span class="data-count">共 <em>{{ pagination.total }}</em> 条</span>
            </div>
            <div class="toolbar-right">
              <el-button @click="handleExport" class="btn-export">
                <el-icon><Download /></el-icon> 导出
              </el-button>
              <el-button type="primary" @click="handleAdd" class="btn-add">
                <el-icon><Plus /></el-icon> 新增
              </el-button>
            </div>
          </div>

          <!-- Table -->
          <el-table :data="tableData" v-loading="loading" stripe class="data-table">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="swName" label="开源软件名称" min-width="160" show-overflow-tooltip />
            <el-table-column prop="swCategory" label="软件分类" width="120" align="center">
              <template #default="{ row }">
                <span class="tag" :class="'tag-' + row.swCategory">{{ getSwCategoryText(row.swCategory) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="swType" label="软件类型" width="100" align="center">
              <template #default="{ row }">
                <span class="type-tag" :class="row.swType === 'MAIN' ? 'type-main' : 'type-limit'">
                  {{ getSwTypeText(row.swType) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="productType" label="产品类型" width="120" show-overflow-tooltip />
            <el-table-column prop="recommendedVersion" label="主推荐版本" width="130" align="center">
              <template #default="{ row }">
                <code class="version-code">{{ row.recommendedVersion || '-' }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="rspTeamName" label="责任团队" width="160" show-overflow-tooltip />
            <el-table-column prop="rspUserName" label="责任人" width="90" align="center" />
            <el-table-column label="操作" width="220" fixed="right" align="center">
              <template #default="{ row }">
                <div class="operation-btns">
                  <el-button type="primary" link @click.stop="handleView(row)">查看</el-button>
                  <el-button type="primary" link @click.stop="handleVersionList(row)">版本</el-button>
                  <el-button type="primary" link @click.stop="handleEdit(row)">编辑</el-button>
                  <el-button type="primary" link @click.stop="handleDocument(row)">文档</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- Pagination -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next"
              @size-change="loadData"
              @current-change="loadData"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="版本清单" name="version">
          <!-- Query Form - Horizontal Layout -->
          <div class="query-section">
            <div class="query-row">
              <div class="query-item">
                <label class="query-label">开源软件名称</label>
                <div class="query-input-group">
                  <el-input
                    v-model="versionQueryForm.swName"
                    placeholder="请输入"
                    clearable
                    class="query-input"
                  />
                  <el-checkbox v-model="versionExactMatch" class="exact-checkbox">精确</el-checkbox>
                </div>
              </div>
              <div class="query-item">
                <label class="query-label">开源软件版本</label>
                <el-input v-model="versionQueryForm.swVersion" placeholder="请输入" clearable class="query-input" />
              </div>
              <div class="query-item">
                <label class="query-label">软件分类</label>
                <el-select v-model="versionQueryForm.swCategory" placeholder="请选择" clearable class="query-input">
                  <el-option label="开源基础软件" value="BASE" />
                  <el-option label="开源工具软件" value="TOOL" />
                  <el-option label="开源组件" value="CMPNT" />
                </el-select>
              </div>
              <div class="query-item">
                <label class="query-label">软件类型</label>
                <el-select v-model="versionQueryForm.swType" placeholder="请选择" clearable class="query-input">
                  <el-option label="主推软件" value="MAIN" />
                  <el-option label="非主推软件" value="LIMIT" />
                  <el-option label="已退出软件" value="QUIT" />
                </el-select>
              </div>
              <div class="query-item">
                <label class="query-label">版本类型</label>
                <el-select v-model="versionQueryForm.verType" placeholder="请选择" clearable class="query-input">
                  <el-option label="白名单" value="WHITELIST" />
                  <el-option label="灰名单" value="GREYLIST" />
                  <el-option label="黑名单" value="BLACKLIST" />
                </el-select>
              </div>
              <div class="query-item">
                <label class="query-label">主推版本</label>
                <el-select v-model="versionQueryForm.isMainUse" placeholder="请选择" clearable class="query-input">
                  <el-option label="是" value="1" />
                  <el-option label="否" value="0" />
                </el-select>
              </div>
              <div class="query-item">
                <label class="query-label">开源许可证</label>
                <el-select v-model="versionQueryForm.licAbbr" placeholder="请选择" clearable class="query-input">
                  <el-option label="Apache-2.0" value="Apache-2.0" />
                  <el-option label="MIT" value="MIT" />
                  <el-option label="GPL" value="GPL" />
                  <el-option label="BSD" value="BSD" />
                  <el-option label="MPL-2.0" value="MPL-2.0" />
                </el-select>
              </div>
              <div class="query-item">
                <label class="query-label">引入团队</label>
                <el-select v-model="versionQueryForm.implTeamId" placeholder="请选择" clearable filterable class="query-input">
                  <el-option v-for="team in teamList" :key="team.id" :label="team.teamName" :value="team.id" />
                </el-select>
              </div>
              <div class="query-actions">
                <el-button @click="handleVersionReset" class="btn-reset">重置</el-button>
                <el-button type="primary" @click="handleVersionQuery" class="btn-search">
                  <el-icon><Search /></el-icon> 查询
                </el-button>
              </div>
            </div>
          </div>

          <!-- Toolbar -->
          <div class="toolbar">
            <div class="toolbar-left">
              <span class="data-count">共 <em>{{ versionPagination.total }}</em> 条</span>
            </div>
            <div class="toolbar-right">
              <el-button class="btn-action">订阅</el-button>
              <el-button @click="handleVersionAdd" class="btn-add">
                <el-icon><Plus /></el-icon> 新增
              </el-button>
              <el-button @click="handleVersionExport" class="btn-export">
                <el-icon><Download /></el-icon> 数据导出
              </el-button>
              <el-button class="btn-action">手工操作</el-button>
            </div>
          </div>

          <!-- Table -->
          <el-table :data="versionTableData" v-loading="versionLoading" stripe class="data-table">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="swName" label="开源软件名称" min-width="120" show-overflow-tooltip />
            <el-table-column prop="swVersion" label="版本号" width="100" align="center">
              <template #default="{ row }">
                <code class="version-code">{{ row.swVersion || '-' }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="swCategory" label="软件分类" width="100" align="center">
              <template #default="{ row }">
                <span class="tag" :class="'tag-' + row.swCategory">{{ getSwCategoryText(row.swCategory) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="swType" label="软件类型" width="90" align="center">
              <template #default="{ row }">
                <span class="type-tag" :class="row.swType === 'MAIN' ? 'type-main' : 'type-limit'">
                  {{ getSwTypeText(row.swType) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="verType" label="版本类型" width="80" align="center">
              <template #default="{ row }">
                <span class="ver-type-tag" :class="'ver-' + row.verType">{{ getVerTypeText(row.verType) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="isMainUse" label="主推" width="60" align="center">
              <template #default="{ row }">
                <span :class="row.isMainUse === '1' ? 'text-success' : 'text-muted'">
                  {{ row.isMainUse === '1' ? '是' : '否' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="licAbbr" label="许可证" width="100" show-overflow-tooltip />
            <el-table-column prop="applicableScene" label="适用场景" width="100" align="center">
              <template #default="{ row }">
                {{ getApplicableSceneText(row.applicableScene) }}
              </template>
            </el-table-column>
            <el-table-column prop="applicableFunctionRange" label="适用范围" width="100" align="center">
              <template #default="{ row }">
                {{ getFunctionRangeText(row.applicableFunctionRange) }}
              </template>
            </el-table-column>
            <el-table-column prop="implTeamName" label="引入团队" width="140" show-overflow-tooltip />
            <el-table-column prop="implUserName" label="引入申请人" width="90" align="center" />
            <el-table-column prop="effectDatetime" label="生效时间" width="110" align="center">
              <template #default="{ row }">
                {{ row.effectDatetime ? row.effectDatetime.slice(0, 10) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="100" show-overflow-tooltip />
            <el-table-column label="操作" width="150" fixed="right" align="center">
              <template #default="{ row }">
                <div class="operation-btns">
                  <el-button type="primary" link @click.stop="handleVersionView(row)">查看</el-button>
                  <el-button type="primary" link @click.stop="handleVersionEdit(row)">编辑</el-button>
                  <el-button type="primary" link @click.stop="handleVersionDocument(row)">介质</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- Pagination -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="versionPagination.page"
              v-model:page-size="versionPagination.pageSize"
              :page-sizes="[20, 50, 100]"
              :total="versionPagination.total"
              layout="total, sizes, prev, pager, next"
              @size-change="loadVersionData"
              @current-change="loadVersionData"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="介质文档清单" name="document">
          <!-- Query Form - Horizontal Layout -->
          <div class="query-section">
            <div class="query-row">
              <div class="query-item">
                <label class="query-label">开源软件名称</label>
                <el-input v-model="mediaQueryForm.swName" placeholder="请输入" clearable class="query-input" />
              </div>
              <div class="query-item">
                <label class="query-label">开源软件版本</label>
                <el-input v-model="mediaQueryForm.swVersion" placeholder="请输入" clearable class="query-input" />
              </div>
              <div class="query-item">
                <label class="query-label">版本类型</label>
                <el-select v-model="mediaQueryForm.verType" placeholder="请选择" clearable class="query-input">
                  <el-option label="白名单" value="WHITELIST" />
                  <el-option label="灰名单" value="GREYLIST" />
                  <el-option label="黑名单" value="BLACKLIST" />
                </el-select>
              </div>
              <div class="query-item">
                <label class="query-label">主推版本</label>
                <el-select v-model="mediaQueryForm.isMainUse" placeholder="请选择" clearable class="query-input">
                  <el-option label="是" value="1" />
                  <el-option label="否" value="0" />
                </el-select>
              </div>
              <div class="query-item">
                <label class="query-label">软件分类</label>
                <el-select v-model="mediaQueryForm.swCategory" placeholder="请选择" clearable class="query-input">
                  <el-option label="开源基础软件" value="BASE" />
                  <el-option label="开源工具软件" value="TOOL" />
                  <el-option label="开源组件" value="CMPNT" />
                </el-select>
              </div>
              <div class="query-actions">
                <el-button @click="handleMediaReset" class="btn-reset">
                  <el-icon><Refresh /></el-icon> 重置
                </el-button>
                <el-button type="primary" @click="handleMediaQuery" class="btn-search">
                  <el-icon><Search /></el-icon> 查询
                </el-button>
              </div>
            </div>
          </div>

          <!-- Toolbar -->
          <div class="toolbar">
            <div class="toolbar-left">
              <span class="data-count">共 <em>{{ mediaPagination.total }}</em> 条</span>
            </div>
          </div>

          <!-- Table -->
          <el-table :data="mediaTableData" v-loading="mediaLoading" stripe class="data-table">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="swName" label="开源软件名称" min-width="120" show-overflow-tooltip />
            <el-table-column prop="swVersion" label="软件版本" width="100" align="center">
              <template #default="{ row }">
                <code class="version-code">{{ row.swVersion || '-' }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="verType" label="版本类型" width="80" align="center">
              <template #default="{ row }">
                <span class="ver-type-tag" :class="'ver-' + row.verType">{{ getVerTypeText(row.verType) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="isMainUse" label="是否主推" width="80" align="center">
              <template #default="{ row }">
                <span :class="row.isMainUse === '1' ? 'text-success' : 'text-muted'">
                  {{ row.isMainUse === '1' ? '是' : '否' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="mediaType" label="介质类型" width="100" align="center">
              <template #default="{ row }">
                <span class="media-type-tag">{{ row.mediaType || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="mediaName" label="介质名称" min-width="200" show-overflow-tooltip />
            <el-table-column label="操作" width="100" fixed="right" align="center">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  link
                  :disabled="!row.mediaUrl"
                  @click.stop="handleMediaDownload(row)"
                >
                  <el-icon><Download /></el-icon> 下载
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- Pagination -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="mediaPagination.page"
              v-model:page-size="mediaPagination.pageSize"
              :page-sizes="[20, 50, 100]"
              :total="mediaPagination.total"
              layout="total, sizes, prev, pager, next"
              @size-change="loadMediaData"
              @current-change="loadMediaData"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增软件' : '编辑软件'" width="720px" destroy-on-close>
      <el-form :model="softwareForm" :rules="formRules" ref="formRef" label-width="130px" class="software-form">
        <div class="form-grid">
          <el-form-item label="开源软件名称" prop="swName">
            <el-input v-model="softwareForm.swName" placeholder="请输入软件名称" />
          </el-form-item>
          <el-form-item label="软件分类" prop="swCategory">
            <el-select v-model="softwareForm.swCategory" style="width: 100%;">
              <el-option label="开源基础软件" value="BASE" />
              <el-option label="开源工具软件" value="TOOL" />
              <el-option label="开源组件" value="CMPNT" />
            </el-select>
          </el-form-item>
          <el-form-item label="软件类型" prop="swType">
            <el-select v-model="softwareForm.swType" style="width: 100%;">
              <el-option label="主推软件" value="MAIN" />
              <el-option label="非主推软件" value="LIMIT" />
              <el-option label="已退出软件" value="QUIT" />
            </el-select>
          </el-form-item>
          <el-form-item label="主推荐版本" prop="recommendedVersion">
            <el-input v-model="softwareForm.recommendedVersion" placeholder="如: 1.20.0" />
          </el-form-item>
          <el-form-item label="责任团队" prop="rspTeamName">
            <el-input v-model="softwareForm.rspTeamName" placeholder="请输入责任团队" />
          </el-form-item>
          <el-form-item label="责任人" prop="rspUserId">
            <el-select v-model="softwareForm.rspUserId" style="width: 100%;" filterable placeholder="请选择">
              <el-option v-for="user in userList" :key="user.id" :label="`${user.username}-${user.realName}`" :value="user.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="产品类型" prop="productType">
            <el-select v-model="softwareForm.productType" style="width: 100%;">
              <el-option label="云计算" value="CLOUD" />
              <el-option label="数据库" value="DATABASE" />
              <el-option label="人工智能" value="AI" />
              <el-option label="开发测试工具" value="DEV_TOOL" />
              <el-option label="运行维护工具" value="OPS" />
              <el-option label="安全工具" value="SECURITY" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="应用场景" prop="applicationScene">
          <el-input v-model="softwareForm.applicationScene" type="textarea" :rows="3" placeholder="请描述软件的主要应用场景" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="软件清单详情" width="800px" destroy-on-close>
      <el-tabs v-model="detailActiveTab" class="detail-tabs">
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="detailData" label-width="130px" class="detail-form">
            <div class="detail-grid">
              <el-form-item label="开源软件名称">{{ detailData.swName || '-' }}</el-form-item>
              <el-form-item label="软件分类">{{ getSwCategoryText(detailData.swCategory) || '-' }}</el-form-item>
              <el-form-item label="软件类型">{{ getSwTypeText(detailData.swType) || '-' }}</el-form-item>
              <el-form-item label="主推荐版本">{{ detailData.recommendedVersion || '-' }}</el-form-item>
              <el-form-item label="责任团队">{{ detailData.rspTeamName || '-' }}</el-form-item>
              <el-form-item label="责任人">{{ detailData.rspUserName || '-' }}</el-form-item>
              <el-form-item label="产品类型">{{ getProductTypeText(detailData.productType) || '-' }}</el-form-item>
            </div>
            <el-form-item label="应用场景" class="scene-item">{{ detailData.applicationScene || '-' }}</el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="操作记录" name="history">
          <div class="tab-placeholder">
            <el-empty description="操作记录功能开发中..." />
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Version Detail Dialog -->
    <el-dialog v-model="versionDetailVisible" :title="versionDialogMode === 'view' ? '版本详情' : (versionDialogMode === 'add' ? '新增版本' : '编辑版本')" width="850px" destroy-on-close>
      <el-tabs v-model="versionDetailActiveTab" class="detail-tabs">
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="versionForm" :rules="versionFormRules" ref="versionFormRef" label-width="140px" class="version-form">
            <div class="form-grid">
              <el-form-item label="开源软件名称" prop="swName">
                <el-input v-model="versionForm.swName" :disabled="versionDialogMode !== 'add'" placeholder="请输入" />
              </el-form-item>
              <el-form-item label="开源软件版本" prop="swVersion">
                <el-input v-model="versionForm.swVersion" :disabled="versionDialogMode !== 'add'" placeholder="如: 1.20.0" />
              </el-form-item>
              <el-form-item label="软件分类" prop="swCategory">
                <el-select v-model="versionForm.swCategory" style="width: 100%;">
                  <el-option label="开源基础软件" value="BASE" />
                  <el-option label="开源工具软件" value="TOOL" />
                  <el-option label="开源组件" value="CMPNT" />
                </el-select>
              </el-form-item>
              <el-form-item label="软件类型" prop="swType">
                <el-select v-model="versionForm.swType" style="width: 100%;">
                  <el-option label="主推软件" value="MAIN" />
                  <el-option label="非主推软件" value="LIMIT" />
                  <el-option label="已退出软件" value="QUIT" />
                </el-select>
              </el-form-item>
              <el-form-item label="版本类型" prop="verType">
                <el-select v-model="versionForm.verType" style="width: 100%;">
                  <el-option label="白名单" value="WHITELIST" />
                  <el-option label="灰名单" value="GREYLIST" />
                  <el-option label="黑名单" value="BLACKLIST" />
                </el-select>
              </el-form-item>
              <el-form-item label="是否主推使用版本" prop="isMainUse">
                <el-select v-model="versionForm.isMainUse" style="width: 100%;">
                  <el-option label="是" value="1" />
                  <el-option label="否" value="0" />
                </el-select>
              </el-form-item>
              <el-form-item label="开源许可证" prop="licAbbr">
                <el-select v-model="versionForm.licAbbr" style="width: 100%;">
                  <el-option label="Apache-2.0" value="Apache-2.0" />
                  <el-option label="MIT" value="MIT" />
                  <el-option label="GPL" value="GPL" />
                  <el-option label="BSD" value="BSD" />
                  <el-option label="MPL-2.0" value="MPL-2.0" />
                </el-select>
              </el-form-item>
              <el-form-item label="适用场景" prop="applicableScene">
                <el-select v-model="versionForm.applicableScene" style="width: 100%;">
                  <el-option label="全场景适用" value="ALL_SCENE" />
                  <el-option label="限定内部使用" value="INNER_SCENE" />
                </el-select>
              </el-form-item>
              <el-form-item label="适用职能范围" prop="applicableFunctionRange">
                <el-select v-model="versionForm.applicableFunctionRange" style="width: 100%;">
                  <el-option label="全行使用" value="ALL_BANK_USE" />
                  <el-option label="仅分行使用" value="ONLY_BRANCH_USE" />
                </el-select>
              </el-form-item>
              <el-form-item label="生效日期" prop="effectDatetime">
                <el-date-picker v-model="versionForm.effectDatetime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%;" placeholder="选择日期时间" />
              </el-form-item>
              <el-form-item label="引入团队" prop="implTeamName">
                <el-input v-model="versionForm.implTeamName" placeholder="请输入引入团队" />
              </el-form-item>
              <el-form-item label="引入申请人" prop="implUserName">
                <el-input v-model="versionForm.implUserName" placeholder="请输入" />
              </el-form-item>
            </div>
            <el-form-item label="引入申请单号" prop="implApplyNo">
              <el-input v-model="versionForm.implApplyNo" placeholder="请输入申请单号" />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="versionForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="介质信息" name="media">
          <div class="tab-placeholder">
            <el-empty description="介质信息功能开发中..." />
          </div>
        </el-tab-pane>
        <el-tab-pane label="操作记录" name="history">
          <div class="tab-placeholder">
            <el-empty description="操作记录功能开发中..." />
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="versionDetailVisible = false">取消</el-button>
        <el-button type="primary" @click="handleVersionSubmit" v-if="versionDialogMode !== 'view'">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Download, Search, Refresh } from '@element-plus/icons-vue'
import { ossSoftwareApi, ossSoftwareBaselineApi, sysTeamApi, sysUserApi } from '@/api'
import * as XLSX from 'xlsx'

const activeTab = ref('software')
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

const queryForm = reactive({
  swName: '',
  swCategory: '',
  swType: '',
  rspTeamId: '',
  rspUserName: ''
})

const teamList = ref([])
const userList = ref([])

const dialogVisible = ref(false)
const dialogMode = ref('add')
const formRef = ref(null)
const detailVisible = ref(false)
const detailActiveTab = ref('basic')
const detailData = ref({})

const defaultForm = {
  swName: '',
  swCategory: 'BASE',
  swType: 'MAIN',
  rspTeamId: '',
  rspTeamName: '',
  rspUserId: '',
  rspUserName: '',
  productType: '',
  recommendedVersion: '',
  applicationScene: ''
}

const softwareForm = reactive({ ...defaultForm })

const formRules = {
  swName: [{ required: true, message: '请输入开源软件名称', trigger: 'blur' }],
  swCategory: [{ required: true, message: '请选择软件分类', trigger: 'change' }],
  swType: [{ required: true, message: '请选择软件类型', trigger: 'change' }],
  rspTeamName: [{ required: true, message: '请输入责任团队', trigger: 'blur' }],
  rspUserId: [{ required: true, message: '请选择责任人', trigger: 'change' }],
  productType: [{ required: true, message: '请选择产品类型', trigger: 'change' }]
}

// Version list state
const versionLoading = ref(false)
const versionTableData = ref([])
const versionPagination = reactive({ page: 1, pageSize: 20, total: 0 })
const versionExactMatch = ref(false)

const versionQueryForm = reactive({
  swName: '',
  swVersion: '',
  swCategory: '',
  swType: '',
  verType: '',
  isMainUse: '',
  licAbbr: '',
  implTeamId: ''
})

const versionDetailVisible = ref(false)
const versionDialogMode = ref('view')
const versionDetailActiveTab = ref('basic')
const versionFormRef = ref(null)

const defaultVersionForm = {
  swId: '',
  swName: '',
  swVersion: '',
  swCategory: 'BASE',
  swType: 'MAIN',
  verType: 'WHITELIST',
  implTeamId: '',
  implTeamName: '',
  implUserId: '',
  implUserName: '',
  implApplyNo: '',
  licId: '',
  licAbbr: '',
  effectDatetime: '',
  isMainUse: '0',
  applicableScene: 'ALL_SCENE',
  applicableFunctionRange: 'ALL_BANK_USE',
  remark: ''
}

const versionForm = reactive({ ...defaultVersionForm })

const versionFormRules = {
  swName: [{ required: true, message: '请输入开源软件名称', trigger: 'blur' }],
  swVersion: [{ required: true, message: '请输入开源软件版本', trigger: 'blur' }],
  swCategory: [{ required: true, message: '请选择软件分类', trigger: 'change' }],
  swType: [{ required: true, message: '请选择软件类型', trigger: 'change' }],
  verType: [{ required: true, message: '请选择版本类型', trigger: 'change' }],
  isMainUse: [{ required: true, message: '请选择是否主推', trigger: 'change' }],
  licAbbr: [{ required: true, message: '请选择开源许可证', trigger: 'change' }],
  applicableScene: [{ required: true, message: '请选择适用场景', trigger: 'change' }],
  applicableFunctionRange: [{ required: true, message: '请选择适用职能范围', trigger: 'change' }],
  effectDatetime: [{ required: true, message: '请选择生效日期', trigger: 'change' }],
  implTeamName: [{ required: true, message: '请输入引入团队', trigger: 'blur' }],
  implUserName: [{ required: true, message: '请输入引入申请人', trigger: 'blur' }]
}

// Media document state
const mediaLoading = ref(false)
const mediaTableData = ref([])
const mediaPagination = reactive({ page: 1, pageSize: 20, total: 0 })

const mediaQueryForm = reactive({
  swName: '',
  swVersion: '',
  verType: '',
  isMainUse: '',
  swCategory: ''
})

const getSwCategoryText = (val) => {
  const map = { 'BASE': '开源基础软件', 'TOOL': '开源工具软件', 'CMPNT': '开源组件' }
  return map[val] || val
}

const getSwTypeText = (val) => {
  const map = { 'MAIN': '主推', 'LIMIT': '非主推', 'QUIT': '已退出' }
  return map[val] || val
}

const getProductTypeText = (val) => {
  const map = {
    'CLOUD': '云计算', 'DATABASE': '数据库', 'AI': '人工智能',
    'DEV_TOOL': '开发测试工具', 'OPS': '运行维护工具', 'SECURITY': '安全工具'
  }
  return map[val] || val
}

const getVerTypeText = (val) => {
  const map = { 'WHITELIST': '白名单', 'GREYLIST': '灰名单', 'BLACKLIST': '黑名单' }
  return map[val] || val
}

const getApplicableSceneText = (val) => {
  const map = { 'ALL_SCENE': '全场景适用', 'INNER_SCENE': '限定内部使用' }
  return map[val] || val
}

const getFunctionRangeText = (val) => {
  const map = { 'ALL_BANK_USE': '全行使用', 'ONLY_BRANCH_USE': '仅分行使用' }
  return map[val] || val
}

const loadTeamList = async () => {
  try {
    const res = await sysTeamApi.list({ page: 1, pageSize: 100 })
    teamList.value = res.data?.records || []
  } catch (e) {
    teamList.value = []
  }
}

const loadUserList = async () => {
  try {
    const res = await sysUserApi.list({ page: 1, pageSize: 100 })
    userList.value = res.data?.records || []
  } catch (e) {
    userList.value = []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      swName: queryForm.swName || undefined,
      swCategory: queryForm.swCategory || undefined,
      swType: queryForm.swType || undefined,
      rspTeamId: queryForm.rspTeamId || undefined,
      rspUserName: queryForm.rspUserName || undefined
    }
    const res = await ossSoftwareApi.list(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  queryForm.swName = ''
  queryForm.swCategory = ''
  queryForm.swType = ''
  queryForm.rspTeamId = ''
  queryForm.rspUserName = ''
  pagination.page = 1
  loadData()
}

const handleAdd = () => {
  dialogMode.value = 'add'
  Object.assign(softwareForm, defaultForm)
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  Object.assign(softwareForm, {
    id: row.id,
    swName: row.swName,
    swCategory: row.swCategory,
    swType: row.swType,
    rspTeamId: row.rspTeamId,
    rspTeamName: row.rspTeamName,
    rspUserId: row.rspUserId,
    rspUserName: row.rspUserName,
    productType: row.productType,
    recommendedVersion: row.recommendedVersion,
    applicationScene: row.applicationScene
  })
  dialogVisible.value = true
}

const handleView = async (row) => {
  try {
    const res = await ossSoftwareApi.detail(row.id)
    detailData.value = res.data || row
  } catch (e) {
    detailData.value = row
  }
  detailVisible.value = true
}

const handleVersionList = (row) => {
  // 切换到版本清单Tab并带入查询条件
  activeTab.value = 'version'
  versionQueryForm.swName = row.swName
  versionQueryForm.swCategory = row.swCategory
  versionExactMatch.value = true
  handleVersionQuery()
}

const handleDocument = (row) => {
  // 切换到介质文档清单Tab并带入查询条件
  activeTab.value = 'document'
  mediaQueryForm.swName = row.swName
  mediaQueryForm.swVersion = row.recommendedVersion || ''
  mediaQueryForm.swCategory = row.swCategory
  handleMediaQuery()
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (dialogMode.value === 'add') {
      await ossSoftwareApi.create(softwareForm)
    } else {
      await ossSoftwareApi.update(softwareForm.id, softwareForm)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    if (e !== false) ElMessage.error('操作失败')
  }
}

const handleExport = async () => {
  loading.value = true
  try {
    const params = {
      page: 1,
      pageSize: 10000,
      swName: queryForm.swName || undefined,
      swCategory: queryForm.swCategory || undefined,
      swType: queryForm.swType || undefined,
      rspTeamId: queryForm.rspTeamId || undefined,
      rspUserName: queryForm.rspUserName || undefined
    }
    const res = await ossSoftwareApi.list(params)
    const data = res.data?.records || []

    const exportData = data.map(item => ({
      '开源软件名称': item.swName || '',
      '软件分类': getSwCategoryText(item.swCategory),
      '软件类型': getSwTypeText(item.swType),
      '产品类型': getProductTypeText(item.productType),
      '主推荐版本': item.recommendedVersion || '',
      '责任团队': item.rspTeamName || '',
      '责任人': item.rspUserName || '',
      '应用场景': item.applicationScene || ''
    }))

    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '软件清单')
    XLSX.writeFile(workbook, `开源软件清单_${new Date().toISOString().slice(0, 10)}.xlsx`)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

// Version list functions
const loadVersionData = async () => {
  versionLoading.value = true
  try {
    const params = {
      page: versionPagination.page,
      pageSize: versionPagination.pageSize,
      swName: versionQueryForm.swName || undefined,
      exactMatch: versionExactMatch.value || undefined,
      swVersion: versionQueryForm.swVersion || undefined,
      swCategory: versionQueryForm.swCategory || undefined,
      swType: versionQueryForm.swType || undefined,
      verType: versionQueryForm.verType || undefined,
      isMainUse: versionQueryForm.isMainUse || undefined,
      licAbbr: versionQueryForm.licAbbr || undefined,
      implTeamId: versionQueryForm.implTeamId || undefined
    }
    const res = await ossSoftwareBaselineApi.list(params)
    versionTableData.value = res.data?.records || []
    versionPagination.total = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载版本数据失败')
  } finally {
    versionLoading.value = false
  }
}

const handleVersionQuery = () => {
  versionPagination.page = 1
  loadVersionData()
}

const handleVersionReset = () => {
  versionQueryForm.swName = ''
  versionQueryForm.swVersion = ''
  versionQueryForm.swCategory = ''
  versionQueryForm.swType = ''
  versionQueryForm.verType = ''
  versionQueryForm.isMainUse = ''
  versionQueryForm.licAbbr = ''
  versionQueryForm.implTeamId = ''
  versionExactMatch.value = false
  versionPagination.page = 1
  loadVersionData()
}

const handleVersionAdd = () => {
  versionDialogMode.value = 'add'
  Object.assign(versionForm, defaultVersionForm)
  versionDetailActiveTab.value = 'basic'
  versionDetailVisible.value = true
}

const handleVersionView = async (row) => {
  versionDialogMode.value = 'view'
  versionDetailActiveTab.value = 'basic'
  try {
    const res = await ossSoftwareBaselineApi.detail(row.id)
    Object.assign(versionForm, res.data || row)
  } catch (e) {
    Object.assign(versionForm, row)
  }
  versionDetailVisible.value = true
}

const handleVersionEdit = async (row) => {
  versionDialogMode.value = 'edit'
  versionDetailActiveTab.value = 'basic'
  try {
    const res = await ossSoftwareBaselineApi.detail(row.id)
    Object.assign(versionForm, res.data || row)
  } catch (e) {
    Object.assign(versionForm, row)
  }
  versionDetailVisible.value = true
}

const handleVersionSubmit = async () => {
  try {
    await versionFormRef.value.validate()
    if (versionDialogMode.value === 'add') {
      await ossSoftwareBaselineApi.create(versionForm)
    } else {
      await ossSoftwareBaselineApi.update(versionForm.id, versionForm)
    }
    ElMessage.success('操作成功')
    versionDetailVisible.value = false
    loadVersionData()
  } catch (e) {
    if (e !== false) ElMessage.error('操作失败')
  }
}

const handleVersionExport = async () => {
  versionLoading.value = true
  try {
    const params = {
      page: 1,
      pageSize: 10000,
      swName: versionQueryForm.swName || undefined,
      exactMatch: versionExactMatch.value || undefined,
      swVersion: versionQueryForm.swVersion || undefined,
      swCategory: versionQueryForm.swCategory || undefined,
      swType: versionQueryForm.swType || undefined,
      verType: versionQueryForm.verType || undefined,
      isMainUse: versionQueryForm.isMainUse || undefined,
      licAbbr: versionQueryForm.licAbbr || undefined,
      implTeamId: versionQueryForm.implTeamId || undefined
    }
    const res = await ossSoftwareBaselineApi.list(params)
    const data = res.data?.records || []

    const exportData = data.map(item => ({
      '开源软件名称': item.swName || '',
      '版本号': item.swVersion || '',
      '软件分类': getSwCategoryText(item.swCategory),
      '软件类型': getSwTypeText(item.swType),
      '版本类型': getVerTypeText(item.verType),
      '主推版本': item.isMainUse === '1' ? '是' : '否',
      '许可证': item.licAbbr || '',
      '适用场景': getApplicableSceneText(item.applicableScene),
      '适用范围': getFunctionRangeText(item.applicableFunctionRange),
      '引入团队': item.implTeamName || '',
      '引入申请人': item.implUserName || '',
      '生效时间': item.effectDatetime ? item.effectDatetime.slice(0, 10) : '',
      '备注': item.remark || ''
    }))

    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '版本清单')
    XLSX.writeFile(workbook, `开源软件版本清单_${new Date().toISOString().slice(0, 10)}.xlsx`)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    versionLoading.value = false
  }
}

const handleVersionDocument = (row) => {
  // 切换到介质文档清单Tab并带入查询条件
  activeTab.value = 'document'
  mediaQueryForm.swName = row.swName
  mediaQueryForm.swVersion = row.swVersion
  mediaQueryForm.swCategory = row.swCategory
  handleMediaQuery()
}

// Media document functions
const loadMediaData = async () => {
  mediaLoading.value = true
  try {
    const params = {
      page: mediaPagination.page,
      pageSize: mediaPagination.pageSize,
      swName: mediaQueryForm.swName || undefined,
      swVersion: mediaQueryForm.swVersion || undefined,
      verType: mediaQueryForm.verType || undefined,
      isMainUse: mediaQueryForm.isMainUse || undefined,
      swCategory: mediaQueryForm.swCategory || undefined
    }
    const res = await ossSoftwareBaselineApi.mediaList(params)
    mediaTableData.value = res.data?.records || []
    mediaPagination.total = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载介质文档数据失败')
  } finally {
    mediaLoading.value = false
  }
}

const handleMediaQuery = () => {
  mediaPagination.page = 1
  loadMediaData()
}

const handleMediaReset = () => {
  mediaQueryForm.swName = ''
  mediaQueryForm.swVersion = ''
  mediaQueryForm.verType = ''
  mediaQueryForm.isMainUse = ''
  mediaQueryForm.swCategory = ''
  mediaPagination.page = 1
  loadMediaData()
}

const handleMediaDownload = async (row) => {
  if (!row.mediaUrl) {
    ElMessage.warning('下载地址为空')
    return
  }
  try {
    const response = await ossSoftwareBaselineApi.download(row.mediaUrl)
    // Create download link
    const blob = new Blob([response])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.mediaName || 'download'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

onMounted(() => {
  loadTeamList()
  loadUserList()
  loadData()
  loadVersionData()
  loadMediaData()
})

// 监听activeTab变化时加载对应tab的数据
watch(activeTab, (newTab) => {
  if (newTab === 'version') {
    loadVersionData()
  } else if (newTab === 'document') {
    loadMediaData()
  }
})
</script>

<style scoped lang="scss">
.oss-software-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 0;
}

.page-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: transparent;
}

.oss-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;

  :deep(.el-tabs__header) {
    margin: 0 0 16px;
    border-bottom: 1px solid var(--border);
  }

  :deep(.el-tabs__content) {
    flex: 1;
    overflow: auto;
  }

  :deep(.el-tab-pane) {
    height: 100%;
  }
}

// Query Section - Horizontal Layout
.query-section {
  background: var(--bg-card);
  border-radius: 8px;
  padding: 20px 24px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.query-row {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}

.query-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 140px;
}

.query-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.query-input {
  width: 100%;
}

.query-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
  padding-bottom: 0;
}

// Buttons
.btn-reset {
  &::before {
    content: '';
    margin-right: 4px;
  }
}

.btn-search {
  display: flex;
  align-items: center;
  gap: 4px;
}

.btn-export {
  &::before {
    content: '';
    margin-right: 4px;
  }
}

.btn-add {
  display: flex;
  align-items: center;
  gap: 4px;
}

// Toolbar
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 0 4px;

  &-left {
    display: flex;
    align-items: center;
  }

  &-right {
    display: flex;
    gap: 8px;
  }
}

.data-count {
  font-size: 13px;
  color: var(--text-secondary);

  em {
    color: var(--accent);
    font-style: normal;
    font-weight: 600;
  }
}

// Table
.data-table {
  background: var(--bg-card);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

  :deep(.el-table__header th) {
    background: var(--bg-dark);
    color: var(--text-primary);
    font-weight: 600;
    font-size: 13px;
    padding: 12px 8px;
  }

  :deep(.el-table__body td) {
    padding: 10px 8px;
    font-size: 13px;
  }

  :deep(.el-table__row) {
    transition: background 0.15s;
    &:hover > td {
      background: var(--bg-dark) !important;
    }
  }
}

// Tags
.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;

  &.tag-BASE {
    background: rgba(64, 158, 255, 0.1);
    color: #409eff;
  }

  &.tag-TOOL {
    background: rgba(103, 194, 58, 0.1);
    color: #67c23a;
  }

  &.tag-CMPNT {
    background: rgba(230, 162, 60, 0.1);
    color: #e6a23c;
  }
}

.type-tag {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 500;

  &.type-main {
    background: rgba(103, 194, 58, 0.15);
    color: #67c23a;
  }

  &.type-limit {
    background: rgba(144, 147, 153, 0.15);
    color: #909399;
  }
}

.version-code {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 12px;
  color: var(--accent);
  background: var(--bg-dark);
  padding: 2px 6px;
  border-radius: 3px;
}

.operation-btns {
  display: flex;
  gap: 2px;
  justify-content: center;

  .el-button {
    padding: 4px 8px;
    font-size: 13px;

    & + .el-button {
      margin-left: 0;
    }
  }
}

// Pagination
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding: 12px 0;

  :deep(.el-pagination) {
    font-weight: normal;
  }
}

// Tab placeholder
.tab-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 400px;
  background: var(--bg-card);
  border-radius: 8px;
}

// Dialog Forms
.software-form {
  .form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
    margin-bottom: 16px;
  }
}

.detail-form {
  .detail-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px 32px;
    margin-bottom: 20px;
  }

  .scene-item {
    :deep(.el-form-item__content) {
      padding: 12px;
      background: var(--bg-dark);
      border-radius: 6px;
      line-height: 1.8;
    }
  }
}

.detail-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 20px;
  }
}

// Version form styles
.version-form {
  .form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
    margin-bottom: 16px;
  }
}

// Version type tag
.ver-type-tag {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 500;

  &.ver-WHITELIST {
    background: rgba(103, 194, 58, 0.15);
    color: #67c23a;
  }

  &.ver-GREYLIST {
    background: rgba(230, 162, 60, 0.15);
    color: #e6a23c;
  }

  &.ver-BLACKLIST {
    background: rgba(245, 108, 108, 0.15);
    color: #f56c6c;
  }
}

// Text helpers
.text-success {
  color: #67c23a;
  font-weight: 500;
}

.text-muted {
  color: #909399;
}

// Media type tag
.media-type-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

// Query input group with checkbox
.query-input-group {
  display: flex;
  align-items: center;
  gap: 8px;

  .exact-checkbox {
    white-space: nowrap;
    margin-left: 4px;
  }
}

// Toolbar action button
.btn-action {
  &:hover {
    color: var(--accent);
    border-color: var(--accent);
  }
}
</style>
