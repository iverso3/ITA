<template>
  <div class="app-detail animate-fade-in">
    <!-- Page Header -->
    <div class="page-header">
      <div class="page-header__top">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
      </div>
      <h1 class="page-header__title">应用详情</h1>
      <p class="page-header__desc">{{ appData?.appName || '' }} - {{ appData?.appCode || '' }}</p>
    </div>

    <!-- Tabs -->
    <div class="page-card" style="padding: 0;">
      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="基本信息" name="basic">
          <div class="tab-content-inner">
            <!-- 基本属性 -->
            <div class="section-title">基本属性</div>
            <div class="info-grid">
              <div class="info-row">
                <span class="info-label">系统所属层</span>
                <span class="info-value">{{ appData?.systemLayer || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">应用所属系统</span>
                <span class="info-value">{{ parentAppName || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">应用名称</span>
                <span class="info-value">{{ appData?.appName || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">英文全称</span>
                <span class="info-value">{{ appData?.appNameEn || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">英文简称</span>
                <span class="info-value">{{ appData?.appNameEn || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">应用状态</span>
                <span class="info-value">
                  <el-tag size="small" :type="getStatusType(appData?.status)">{{ appData?.status || '-' }}</el-tag>
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">应用主管部门</span>
                <span class="info-value">{{ appData?.departmentName || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">实施单位</span>
                <span class="info-value">{{ appData?.implementationUnit || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">实施处室</span>
                <span class="info-value">{{ appData?.implementationDivision || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">实施项目组</span>
                <span class="info-value">{{ appData?.implementationTeam || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">投产时间</span>
                <span class="info-value">{{ appData?.goLiveDate || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">下线日期</span>
                <span class="info-value">{{ appData?.retireDate || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">主业务功能领域</span>
                <span class="info-value">{{ appData?.mainBusinessDomain || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">辅业务功能领域</span>
                <span class="info-value">{{ appData?.secondaryBusinessDomain || '-' }}</span>
              </div>
            </div>

            <!-- 应用简介 -->
            <div class="section-title">应用简介</div>
            <div class="info-textarea">
              {{ appData?.description || '暂无描述' }}
            </div>

            <!-- 备注 -->
            <div class="section-title">备注</div>
            <div class="info-textarea">
              {{ appData?.remark || '暂无备注' }}
            </div>

            <el-divider />

            <!-- 分类信息 -->
            <div class="section-title">分类信息</div>
            <div class="info-grid">
              <div class="info-row">
                <span class="info-label">是否互联网应用</span>
                <span class="info-value">{{ appData?.isInternetApp === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">服务对象</span>
                <span class="info-value">{{ appData?.serviceObject || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否支付应用</span>
                <span class="info-value">{{ appData?.isPaymentApp === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否电子银行应用</span>
                <span class="info-value">{{ appData?.isElectronicBankingApp === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否网上银行应用</span>
                <span class="info-value">{{ appData?.isOnlineBankingApp === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否移动应用</span>
                <span class="info-value">{{ appData?.isMobileApp === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否票据应用</span>
                <span class="info-value">{{ appData?.isBillApp === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否互联网金融应用</span>
                <span class="info-value">{{ appData?.isInternetFinanceApp === 1 ? '是' : '否' }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="安全等级" name="security">
          <div class="tab-content-inner">
            <div class="section-title">基本分组</div>
            <div class="info-grid">
              <div class="info-row">
                <span class="info-label">所属系统等级保护安全等级</span>
                <span class="info-value">{{ appData?.systemProtectionLevel || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">等级保护安全等级</span>
                <span class="info-value">{{ appData?.protectionLevel || '-' }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="技术信息" name="tech">
          <div class="tab-content-inner">
            <!-- 基础实施信息 -->
            <div class="section-title">基础实施信息</div>
            <div class="info-grid">
              <div class="info-row">
                <span class="info-label">实施方式</span>
                <span class="info-value">{{ appData?.implementationMethod || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">实施类型</span>
                <span class="info-value">{{ appData?.implementationType || '-' }}</span>
              </div>
            </div>

            <el-divider />

            <!-- 安全基础信息 -->
            <div class="section-title">安全基础信息</div>
            <div class="info-grid info-grid-3col">
              <div class="info-row">
                <span class="info-label">登录用户字段名称</span>
                <span class="info-value">{{ appData?.loginUserField || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">登录密码字段名称</span>
                <span class="info-value">{{ appData?.loginPasswordField || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否部署在DMZ区</span>
                <span class="info-value">{{ appData?.isDeployedDmz === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否存在上传功能</span>
                <span class="info-value">{{ appData?.hasUploadFunction === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">账号密码错误冻结机制</span>
                <span class="info-value">{{ appData?.pwdErrorFreezeMechanism || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否存在主动外发请求业务场景</span>
                <span class="info-value">{{ appData?.hasOutboundRequest === 1 ? '是' : '否' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">外发业务场景描述</span>
                <span class="info-value">{{ appData?.outboundRequestDesc || '-' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">上传的功能描述和URL</span>
                <span class="info-value">{{ appData?.uploadFunctionDesc || '-' }}</span>
              </div>
            </div>

            <div class="info-grid info-grid-3col">
              <div class="info-row">
                <span class="info-label">文件上传路径</span>
                <span class="info-value">{{ appData?.uploadFilePath || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">文件上传路径执行权限</span>
                <span class="info-value">{{ appData?.uploadPathExecutable || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">上传文件类型</span>
                <span class="info-value">{{ appData?.uploadFileTypes || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否存在下载功能</span>
                <span class="info-value">{{ appData?.hasDownloadFunction === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否APP应用</span>
                <span class="info-value">{{ appData?.isAppApplication === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">开源有关信息</span>
                <span class="info-value">{{ appData?.openSourceInfo || '-' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">下载的功能描述和URL</span>
                <span class="info-value">{{ appData?.downloadFunctionDesc || '-' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">下载文件类型</span>
                <span class="info-value">{{ appData?.downloadFileTypes || '-' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">访问主页</span>
                <span class="info-value">
                  <a v-if="appData?.accessUrl" :href="appData.accessUrl" target="_blank" class="link-url">{{ appData.accessUrl }}</a>
                  <span v-else>-</span>
                </span>
              </div>
            </div>

            <el-divider />

            <!-- 其他信息 -->
            <div class="section-title">其他信息</div>
            <div class="info-grid info-grid-3col">
              <div class="info-row">
                <span class="info-label">域名</span>
                <span class="info-value">{{ appData?.domainName || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">网络结构模式</span>
                <span class="info-value">{{ appData?.networkMode || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">访问方式</span>
                <span class="info-value">{{ appData?.accessType || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">实施商信息</span>
                <span class="info-value">{{ appData?.vendorInfo || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">认证方式</span>
                <span class="info-value">{{ appData?.authMethod || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否与外部第三方系统对接</span>
                <span class="info-value">{{ appData?.hasThirdPartyIntegration === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">互联网线路类型</span>
                <span class="info-value">{{ appData?.isInternetLine === 1 ? '是' : '否' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">与外部第三方系统对接补充说明</span>
                <span class="info-value">{{ appData?.thirdPartyIntegrationDesc || '-' }}</span>
              </div>
            </div>

            <el-divider />

            <!-- 信创情况 -->
            <div class="section-title">信创情况</div>
            <div class="info-grid info-grid-3col">
              <div class="info-row">
                <span class="info-label">应用是否上信创容器云</span>
                <span class="info-value">{{ appData?.xcCloudStatus || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">服务器信创情况</span>
                <span class="info-value">{{ appData?.serverXcStatus || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否全栈信创</span>
                <span class="info-value">{{ appData?.isFullXc === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否使用第三方产品</span>
                <span class="info-value">{{ appData?.hasThirdPartyProduct === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">第三方产品是否信创产品</span>
                <span class="info-value">{{ appData?.thirdPartyIsXc === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">XC分类</span>
                <span class="info-value">{{ appData?.xcClassification || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">数据库服务器信创情况</span>
                <span class="info-value">{{ appData?.dbServerXcStatus || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">数据库服务器操作系统</span>
                <span class="info-value">{{ appData?.dbServerOs || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">数据库信创情况</span>
                <span class="info-value">{{ appData?.databaseXcStatus || '-' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">服务器信创情况说明</span>
                <span class="info-value">{{ appData?.serverXcDesc || '-' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">数据库信创情况说明</span>
                <span class="info-value">{{ appData?.databaseXcDesc || '-' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">非全栈信创情况</span>
                <span class="info-value">{{ appData?.partialXcDesc || '-' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">第三方产品信创情况说明</span>
                <span class="info-value">{{ appData?.thirdPartyXcDesc || '-' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">信创整体情况说明</span>
                <span class="info-value">{{ appData?.xcOverallDesc || '-' }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="服务信息" name="service">
          <div class="tab-content-inner">
            <div class="section-title">基本属性</div>
            <div class="info-grid">
              <div class="info-row">
                <span class="info-label">客户类型</span>
                <span class="info-value">{{ appData?.customerType || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">服务时间类型</span>
                <span class="info-value">{{ appData?.serviceTimeType || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">内部用户范围</span>
                <span class="info-value">{{ appData?.internalUserScope || '-' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">服务窗口补充说明</span>
                <span class="info-value">{{ appData?.serviceWindowDesc || '-' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">使用范围补充说明</span>
                <span class="info-value">{{ appData?.usageScopeDesc || '-' }}</span>
              </div>
            </div>

            <el-divider />

            <div class="section-title">服务列表</div>
            <el-table :data="detailData?.services || []" stripe>
              <el-table-column prop="serviceName" label="服务名称" />
              <el-table-column prop="protocol" label="协议" width="80" />
              <el-table-column prop="port" label="端口" width="80" />
              <el-table-column prop="description" label="描述" />
            </el-table>
            <el-empty v-if="!detailData?.services?.length" description="暂无服务信息" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="运维信息" name="ops">
          <div class="tab-content-inner">
            <div class="section-title">基本分组</div>
            <div class="info-grid">
              <div class="info-row">
                <span class="info-label">同城RPO</span>
                <span class="info-value">{{ appData?.cityRpo || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">同城RTO</span>
                <span class="info-value">{{ appData?.cityRto || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">同城双活类型</span>
                <span class="info-value">{{ appData?.cityActiveType || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否具备同城环境</span>
                <span class="info-value">{{ appData?.hasCityEnvironment === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">异地RPO</span>
                <span class="info-value">{{ appData?.remoteRpo || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">异地RTO</span>
                <span class="info-value">{{ appData?.remoteRto || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">异地双活类型</span>
                <span class="info-value">{{ appData?.remoteActiveType || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否具备灾备环境</span>
                <span class="info-value">{{ appData?.hasDrEnvironment === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">运维等级</span>
                <span class="info-value">{{ appData?.opsLevel || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">旧运维等级</span>
                <span class="info-value">{{ appData?.oldOpsLevel || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">运维单位</span>
                <span class="info-value">{{ appData?.opsUnit || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">远程访问权限分类</span>
                <span class="info-value">{{ appData?.remoteAccessClass || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否变更自动化</span>
                <span class="info-value">{{ appData?.isChangeAutomation === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">变更投产时点</span>
                <span class="info-value">{{ appData?.changeDeployTime || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">主要业务时段</span>
                <span class="info-value">{{ appData?.mainBusinessHours || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">是否容器化部署</span>
                <span class="info-value">{{ appData?.isContainerized === 1 ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">部署环境</span>
                <span class="info-value">{{ appData?.deploymentEnvironment || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">部署地点</span>
                <span class="info-value">{{ appData?.deploymentLocation || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">灾备等级</span>
                <span class="info-value">{{ appData?.drLevel || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">灾备恢复能力等级</span>
                <span class="info-value">{{ appData?.drRecoveryLevel || '-' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">变更投产时点补充说明</span>
                <span class="info-value">{{ appData?.changeDeployTimeDesc || '-' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">电子数据资产提取审批部门</span>
                <span class="info-value">{{ appData?.dataAssetApprovalDept || '-' }}</span>
              </div>
            </div>

            <div class="info-full-row">
              <div class="info-row">
                <span class="info-label">部署地点补充说明</span>
                <span class="info-value">{{ appData?.deploymentLocationDesc || '-' }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { archAppApi } from '@/api'

const router = useRouter()
const route = useRoute()
const activeTab = ref('basic')
const appData = ref(null)
const detailData = ref(null)
const parentAppName = ref('')

const appId = computed(() => route.params.id)

const loadData = async () => {
  try {
    const res = await archAppApi.detail(appId.value)
    appData.value = res.data?.application || res.data || {}
    detailData.value = res.data || {}

    // Load parent app name if exists
    if (appData.value.parentAppId) {
      try {
        const parentRes = await archAppApi.detail(appData.value.parentAppId)
        parentAppName.value = parentRes.data?.application?.appName || parentRes.data?.appName || ''
      } catch {
        parentAppName.value = ''
      }
    }
  } catch (e) {
    ElMessage.error('加载应用详情失败')
  }
}

const goBack = () => {
  router.push('/arch/application')
}

const getStatusType = (status) => {
  if (!status) return 'info'
  const map = {
    'PRODUCTION': 'success',
    'DEVELOPMENT': 'warning',
    'TESTING': 'info',
    'PLANNING': '',
    'DEPRECATED': 'danger'
  }
  return map[status] || 'info'
}

onMounted(() => {
  if (appId.value) {
    loadData()
  }
})
</script>

<style scoped lang="scss">
.app-detail {
  padding: var(--space-xl);
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: var(--space-xl);

  &__top {
    margin-bottom: var(--space-md);
  }

  &__title {
    font-size: 1.75rem;
    margin-bottom: var(--space-sm);
    display: flex;
    align-items: center;
    gap: var(--space-md);

    &::before {
      content: '';
      width: 4px;
      height: 28px;
      background: linear-gradient(180deg, var(--accent) 0%, var(--accent-dark) 100%);
      border-radius: 2px;
    }
  }

  &__desc {
    color: var(--text-secondary);
    font-size: 0.9375rem;
    padding-left: 12px;
  }
}

.detail-tabs {
  :deep(.el-tabs__header) {
    margin: 0;
    padding: 0 var(--space-xl);
    border-bottom: 1px solid var(--border);
  }

  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__item) {
    padding: 0 var(--space-lg);
    height: 48px;
    line-height: 48px;
    color: var(--text-secondary);
    font-size: 0.9375rem;

    &.is-active {
      color: var(--accent);
      font-weight: 600;
    }

    &:hover {
      color: var(--text-primary);
    }
  }

  :deep(.el-tabs__active-bar) {
    height: 3px;
    background: var(--accent);
    border-radius: 2px 2px 0 0;
  }

  :deep(.el-tabs__content) {
    padding: 0;
  }
}

.tab-content-inner {
  padding: var(--space-xl);
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-lg);
  padding-left: var(--space-md);
  border-left: 3px solid var(--accent);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-md) var(--space-2xl);
  margin-bottom: var(--space-xl);
}

.info-grid-3col {
  grid-template-columns: repeat(3, 1fr);
}

.info-full-row {
  margin-bottom: var(--space-md);

  .info-row {
    border-bottom: 1px solid var(--border-light);
    padding: var(--space-sm) 0;

    .info-label {
      width: 200px;
      flex-shrink: 0;
      color: var(--text-secondary);
      font-size: 0.875rem;
    }

    .info-value {
      flex: 1;
      color: var(--text-primary);
      font-size: 0.875rem;
      word-break: break-word;
    }
  }
}

.link-url {
  color: var(--accent);
  text-decoration: none;
  word-break: break-all;

  &:hover {
    text-decoration: underline;
  }
}

.info-row {
  display: flex;
  padding: var(--space-sm) 0;
  border-bottom: 1px solid var(--border-light);

  .info-label {
    width: 140px;
    flex-shrink: 0;
    color: var(--text-secondary);
    font-size: 0.875rem;
  }

  .info-value {
    flex: 1;
    color: var(--text-primary);
    font-size: 0.875rem;
    word-break: break-word;
  }
}

.info-textarea {
  background: var(--bg-dark-2);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: var(--space-md);
  color: var(--text-primary);
  font-size: 0.875rem;
  line-height: 1.8;
  margin-bottom: var(--space-xl);
  min-height: 80px;
}

.empty-tip {
  text-align: center;
  color: var(--text-muted);
  padding: var(--space-2xl);
  font-size: 0.9375rem;
}

:deep(.el-divider) {
  border-color: var(--border-light);
  margin: var(--space-xl) 0;
}

// Night mode support
:root.theme-night {
  .info-textarea {
    background: var(--bg-dark-2);
  }

  .info-row {
    border-bottom-color: var(--border);
  }
}
</style>
