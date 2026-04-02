package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.ArchApplicationMapper;
import com.bank.itarch.model.dto.ApplicationDetailDTO;
import com.bank.itarch.model.entity.ArchApplication;
import com.bank.itarch.model.entity.ArchApplicationModule;
import com.bank.itarch.model.entity.ArchDependency;
import com.bank.itarch.model.entity.ArchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.bank.itarch.model.dto.ArchApplicationImportDTO;
import com.bank.itarch.model.dto.ImportResult;
import com.bank.itarch.util.ApplicationImportExcelUtil;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArchApplicationService extends ServiceImpl<ArchApplicationMapper, ArchApplication> {

    private final ArchApplicationModuleService moduleService;
    private final ArchServiceService serviceService;
    private final ArchDependencyService dependencyService;

    public PageResult<ArchApplication> pageQuery(PageQuery query, String keyword, String status, String lifecycle, Long departmentId, String systemLayer, String parentAppName) {
        Page<ArchApplication> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<ArchApplication> wrapper = new LambdaQueryWrapper<>();
        // 模糊搜索：应用编号、应用名称、英文全称
        wrapper.and(StringUtils.hasText(keyword), w -> w
                .like(ArchApplication::getAppCode, keyword)
                .or().like(ArchApplication::getAppName, keyword)
                .or().like(ArchApplication::getAppNameEn, keyword))
               .eq(StringUtils.hasText(status), ArchApplication::getStatus, status)
               .eq(StringUtils.hasText(lifecycle), ArchApplication::getLifecycle, lifecycle)
               .eq(departmentId != null, ArchApplication::getDepartmentId, departmentId)
               .eq(StringUtils.hasText(systemLayer), ArchApplication::getSystemLayer, systemLayer)
               // parentAppName需要通过join或子查询实现，这里简化处理
               .orderByDesc(ArchApplication::getCreateTime);
        Page<ArchApplication> result = page(page, wrapper);

        // 如果指定了parentAppName，在结果中过滤
        List<ArchApplication> records = result.getRecords();
        if (StringUtils.hasText(parentAppName)) {
            records = records.stream()
                .filter(app -> {
                    if (app.getParentAppId() != null) {
                        ArchApplication parent = getById(app.getParentAppId());
                        return parent != null && parent.getAppName().equals(parentAppName);
                    }
                    return false;
                })
                .collect(Collectors.toList());
        }

        return PageResult.of(records, (long) records.size(), query.getPage(), query.getPageSize());
    }

    public ArchApplication detail(Long id) {
        ArchApplication entity = getById(id);
        if (entity == null) throw new BusinessException(3001, "应用不存在");
        return entity;
    }

    public ApplicationDetailDTO getDetail(Long id) {
        ArchApplication entity = getById(id);
        if (entity == null) throw new BusinessException(3001, "应用不存在");

        List<ArchApplicationModule> modules = moduleService.listByAppId(id);
        List<ArchService> services = serviceService.listByAppId(id);
        List<ArchDependency> dependencies = dependencyService.listByAppId(id);

        ApplicationDetailDTO dto = new ApplicationDetailDTO();
        dto.setApplication(entity);
        dto.setModules(modules);
        dto.setServices(services);
        dto.setDependencies(dependencies);
        return dto;
    }

    public ArchApplication create(ArchApplication entity) {
        long count = count() + 1;
        entity.setAppCode(String.format("APP-%05d", count));
        save(entity);
        return entity;
    }

    public ArchApplication update(Long id, ArchApplication entity) {
        if (getById(id) == null) throw new BusinessException(3001, "应用不存在");
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(3001, "应用不存在");
        removeById(id);
    }

    public List<ArchApplication> exportList(String keyword, String status, String lifecycle, Long departmentId) {
        LambdaQueryWrapper<ArchApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), w -> w
                .like(ArchApplication::getAppCode, keyword)
                .or().like(ArchApplication::getAppName, keyword)
                .or().like(ArchApplication::getAppNameEn, keyword))
               .eq(StringUtils.hasText(status), ArchApplication::getStatus, status)
               .eq(StringUtils.hasText(lifecycle), ArchApplication::getLifecycle, lifecycle)
               .eq(departmentId != null, ArchApplication::getDepartmentId, departmentId)
               .orderByDesc(ArchApplication::getCreateTime);
        return list(wrapper);
    }

    private static final String TEMPLATE_DIR = "template/";

    /**
     * 生成导入模板文件，返回文件路径
     */
    public String generateImportTemplate() throws IOException {
        String filename = "application_import_template_"
            + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
        String filePath = TEMPLATE_DIR + filename;

        // 确保目录存在
        File dir = new File(TEMPLATE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            ApplicationImportExcelUtil.generateTemplate(fos);
        }
        return filePath;
    }

    @Transactional
    public ImportResult importList(MultipartFile file) throws Exception {
        // 1. 解析 Excel
        List<ArchApplicationImportDTO> rows;
        try {
            rows = ApplicationImportExcelUtil.parseExcel(file.getInputStream());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, e.getMessage());
        } catch (Exception e) {
            throw new BusinessException(400, "Excel解析失败: " + e.getMessage());
        }

        if (rows.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        // 2. 校验空行
        List<ImportResult.ImportError> errors = new ArrayList<>();
        List<ArchApplicationImportDTO> validRows = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            ArchApplicationImportDTO row = rows.get(i);
            if (!StringUtils.hasText(row.getAppCode()) || !StringUtils.hasText(row.getAppName())) {
                errors.add(new ImportResult.ImportError(i + 2, "appCode/appName 不能为空"));
            } else {
                validRows.add(row);
            }
        }

        // 3. Upsert
        int updatedCount = 0;
        int createdCount = 0;
        for (ArchApplicationImportDTO dto : validRows) {
            LambdaQueryWrapper<ArchApplication> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ArchApplication::getAppCode, dto.getAppCode());
            ArchApplication existing = baseMapper.selectOne(wrapper);

            if (existing != null) {
                applyDtoToEntity(existing, dto);
                baseMapper.updateById(existing);
                updatedCount++;
            } else {
                ArchApplication entity = new ArchApplication();
                applyDtoToEntity(entity, dto);
                entity.setAppCode(dto.getAppCode());
                baseMapper.insert(entity);
                createdCount++;
            }
        }

        if (!errors.isEmpty()) {
            return ImportResult.error(rows.size(), errors);
        }
        return ImportResult.ok(rows.size(), validRows.size(), updatedCount, createdCount);
    }

    private void applyDtoToEntity(ArchApplication entity, ArchApplicationImportDTO dto) {
        if (dto.getAppName() != null) entity.setAppName(dto.getAppName());
        if (dto.getAppNameEn() != null) entity.setAppNameEn(dto.getAppNameEn());
        if (dto.getAppType() != null) entity.setAppType(dto.getAppType());
        if (dto.getImportanceLevel() != null) entity.setImportanceLevel(dto.getImportanceLevel());
        if (dto.getSystemLayer() != null) entity.setSystemLayer(dto.getSystemLayer());
        if (dto.getLifecycle() != null) entity.setLifecycle(dto.getLifecycle());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        else entity.setStatus("PLANNING");
        if (dto.getDepartmentName() != null) entity.setDepartmentName(dto.getDepartmentName());
        if (dto.getImplementationUnit() != null) entity.setImplementationUnit(dto.getImplementationUnit());
        if (dto.getImplementationDivision() != null) entity.setImplementationDivision(dto.getImplementationDivision());
        if (dto.getImplementationTeam() != null) entity.setImplementationTeam(dto.getImplementationTeam());
        if (dto.getGoLiveDate() != null) entity.setGoLiveDate(dto.getGoLiveDate());
        if (dto.getRetireDate() != null) entity.setRetireDate(dto.getRetireDate());

        if (dto.getIsInternetApp() != null) entity.setIsInternetApp(dto.getIsInternetApp());
        if (dto.getIsPaymentApp() != null) entity.setIsPaymentApp(dto.getIsPaymentApp());
        if (dto.getIsElectronicBankingApp() != null) entity.setIsElectronicBankingApp(dto.getIsElectronicBankingApp());
        if (dto.getIsOnlineBankingApp() != null) entity.setIsOnlineBankingApp(dto.getIsOnlineBankingApp());
        if (dto.getIsMobileApp() != null) entity.setIsMobileApp(dto.getIsMobileApp());
        if (dto.getIsBillApp() != null) entity.setIsBillApp(dto.getIsBillApp());
        if (dto.getIsInternetFinanceApp() != null) entity.setIsInternetFinanceApp(dto.getIsInternetFinanceApp());

        // parentAppName -> parentAppId
        if (StringUtils.hasText(dto.getParentAppName())) {
            LambdaQueryWrapper<ArchApplication> parentWrapper = new LambdaQueryWrapper<>();
            parentWrapper.eq(ArchApplication::getAppName, dto.getParentAppName()).last("LIMIT 1");
            ArchApplication parent = baseMapper.selectOne(parentWrapper);
            if (parent != null) {
                entity.setParentAppId(parent.getId());
            }
        }

        // 处理 ext_attrs JSON
        Map<String, Object> extAttrs = new HashMap<>();
        if (StringUtils.hasText(entity.getExtAttrs())) {
            try {
                extAttrs = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    entity.getExtAttrs(), Map.class);
            } catch (Exception e) {
                extAttrs = new HashMap<>();
            }
        }

        setExtAttr(extAttrs, dto.getMainBusinessDomain(), "mainBusinessDomain");
        setExtAttr(extAttrs, dto.getSecondaryBusinessDomain(), "secondaryBusinessDomain");
        setExtAttr(extAttrs, dto.getDescription(), "description");
        setExtAttr(extAttrs, dto.getRemark(), "remark");
        setExtAttr(extAttrs, dto.getServiceObject(), "serviceObject");
        setExtAttr(extAttrs, dto.getSystemProtectionLevel(), "systemProtectionLevel");
        setExtAttr(extAttrs, dto.getProtectionLevel(), "protectionLevel");
        setExtAttr(extAttrs, dto.getImplementationMethod(), "implementationMethod");
        setExtAttr(extAttrs, dto.getImplementationType(), "implementationType");
        setExtAttr(extAttrs, dto.getLoginUserField(), "loginUserField");
        setExtAttr(extAttrs, dto.getLoginPasswordField(), "loginPasswordField");
        setExtAttr(extAttrs, dto.getIsDeployedDmz(), "isDeployedDmz");
        setExtAttr(extAttrs, dto.getHasUploadFunction(), "hasUploadFunction");
        setExtAttr(extAttrs, dto.getPwdErrorFreezeMechanism(), "pwdErrorFreezeMechanism");
        setExtAttr(extAttrs, dto.getHasOutboundRequest(), "hasOutboundRequest");
        setExtAttr(extAttrs, dto.getOutboundRequestDesc(), "outboundRequestDesc");
        setExtAttr(extAttrs, dto.getUploadFunctionDesc(), "uploadFunctionDesc");
        setExtAttr(extAttrs, dto.getUploadFilePath(), "uploadFilePath");
        setExtAttr(extAttrs, dto.getUploadPathExecutable(), "uploadPathExecutable");
        setExtAttr(extAttrs, dto.getUploadFileTypes(), "uploadFileTypes");
        setExtAttr(extAttrs, dto.getHasDownloadFunction(), "hasDownloadFunction");
        setExtAttr(extAttrs, dto.getDownloadFunctionDesc(), "downloadFunctionDesc");
        setExtAttr(extAttrs, dto.getDownloadFileTypes(), "downloadFileTypes");
        setExtAttr(extAttrs, dto.getIsAppApplication(), "isAppApplication");
        setExtAttr(extAttrs, dto.getOpenSourceInfo(), "openSourceInfo");
        setExtAttr(extAttrs, dto.getAccessUrl(), "accessUrl");
        setExtAttr(extAttrs, dto.getDomainName(), "domainName");
        setExtAttr(extAttrs, dto.getNetworkMode(), "networkMode");
        setExtAttr(extAttrs, dto.getAccessType(), "accessType");
        setExtAttr(extAttrs, dto.getVendorInfo(), "vendorInfo");
        setExtAttr(extAttrs, dto.getAuthMethod(), "authMethod");
        setExtAttr(extAttrs, dto.getHasThirdPartyIntegration(), "hasThirdPartyIntegration");
        setExtAttr(extAttrs, dto.getThirdPartyIntegrationDesc(), "thirdPartyIntegrationDesc");
        setExtAttr(extAttrs, dto.getIsInternetLine(), "isInternetLine");
        setExtAttr(extAttrs, dto.getXcCloudStatus(), "xcCloudStatus");
        setExtAttr(extAttrs, dto.getServerXcStatus(), "serverXcStatus");
        setExtAttr(extAttrs, dto.getIsFullXc(), "isFullXc");
        setExtAttr(extAttrs, dto.getHasThirdPartyProduct(), "hasThirdPartyProduct");
        setExtAttr(extAttrs, dto.getThirdPartyIsXc(), "thirdPartyIsXc");
        setExtAttr(extAttrs, dto.getXcClassification(), "xcClassification");
        setExtAttr(extAttrs, dto.getDbServerXcStatus(), "dbServerXcStatus");
        setExtAttr(extAttrs, dto.getDbServerOs(), "dbServerOs");
        setExtAttr(extAttrs, dto.getDatabaseXcStatus(), "databaseXcStatus");
        setExtAttr(extAttrs, dto.getServerXcDesc(), "serverXcDesc");
        setExtAttr(extAttrs, dto.getDatabaseXcDesc(), "databaseXcDesc");
        setExtAttr(extAttrs, dto.getPartialXcDesc(), "partialXcDesc");
        setExtAttr(extAttrs, dto.getThirdPartyXcDesc(), "thirdPartyXcDesc");
        setExtAttr(extAttrs, dto.getXcOverallDesc(), "xcOverallDesc");
        setExtAttr(extAttrs, dto.getCustomerType(), "customerType");
        setExtAttr(extAttrs, dto.getServiceTimeType(), "serviceTimeType");
        setExtAttr(extAttrs, dto.getInternalUserScope(), "internalUserScope");
        setExtAttr(extAttrs, dto.getServiceWindowDesc(), "serviceWindowDesc");
        setExtAttr(extAttrs, dto.getUsageScopeDesc(), "usageScopeDesc");
        setExtAttr(extAttrs, dto.getCityRpo(), "cityRpo");
        setExtAttr(extAttrs, dto.getCityRto(), "cityRto");
        setExtAttr(extAttrs, dto.getCityActiveType(), "cityActiveType");
        setExtAttr(extAttrs, dto.getHasCityEnvironment(), "hasCityEnvironment");
        setExtAttr(extAttrs, dto.getRemoteRpo(), "remoteRpo");
        setExtAttr(extAttrs, dto.getRemoteRto(), "remoteRto");
        setExtAttr(extAttrs, dto.getRemoteActiveType(), "remoteActiveType");
        setExtAttr(extAttrs, dto.getHasDrEnvironment(), "hasDrEnvironment");
        setExtAttr(extAttrs, dto.getOpsLevel(), "opsLevel");
        setExtAttr(extAttrs, dto.getOldOpsLevel(), "oldOpsLevel");
        setExtAttr(extAttrs, dto.getOpsUnit(), "opsUnit");
        setExtAttr(extAttrs, dto.getRemoteAccessClass(), "remoteAccessClass");
        setExtAttr(extAttrs, dto.getIsChangeAutomation(), "isChangeAutomation");
        setExtAttr(extAttrs, dto.getChangeDeployTime(), "changeDeployTime");
        setExtAttr(extAttrs, dto.getChangeDeployTimeDesc(), "changeDeployTimeDesc");
        setExtAttr(extAttrs, dto.getMainBusinessHours(), "mainBusinessHours");
        setExtAttr(extAttrs, dto.getDataAssetApprovalDept(), "dataAssetApprovalDept");
        setExtAttr(extAttrs, dto.getIsContainerized(), "isContainerized");
        setExtAttr(extAttrs, dto.getDeploymentEnvironment(), "deploymentEnvironment");
        setExtAttr(extAttrs, dto.getDeploymentLocation(), "deploymentLocation");
        setExtAttr(extAttrs, dto.getDeploymentLocationDesc(), "deploymentLocationDesc");
        setExtAttr(extAttrs, dto.getDrLevel(), "drLevel");
        setExtAttr(extAttrs, dto.getDrRecoveryLevel(), "drRecoveryLevel");

        try {
            entity.setExtAttrs(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(extAttrs));
        } catch (Exception e) {
            // ignore
        }
    }

    private void setExtAttr(Map<String, Object> extAttrs, Object value, String key) {
        if (value != null) {
            extAttrs.put(key, value);
        }
    }

    /**
     * 获取应用分层树结构（用于侧边栏）
     * 层级：总行应用系统 → 应用分层（一级） → 应用群组（二级） → 具体应用
     */
    public List<Map<String, Object>> getLayeredTree() {
        // 定义9个固定的应用分层顺序
        List<String> fixedLayers = Arrays.asList(
            "未分类用层", "数据中台层", "基础资源层", "业务中台层",
            "管理决策层", "用户交互层", "理财子公司", "产品服务层", "技术支撑层"
        );

        LambdaQueryWrapper<ArchApplication> wrapper = new LambdaQueryWrapper<>();
        // 移除systemLayer过滤条件，查询所有应用以显示在对应的9个分层中
        List<ArchApplication> apps = list(wrapper);

        // 按systemLayer和parentAppId分组
        Map<String, Map<String, List<ArchApplication>>> grouped = apps.stream()
            .collect(Collectors.groupingBy(
                app -> app.getSystemLayer() != null ? app.getSystemLayer() : "未分类用层",
                Collectors.groupingBy(app -> {
                    if (app.getParentAppId() != null) {
                        ArchApplication parent = getById(app.getParentAppId());
                        return parent != null ? parent.getAppName() : "未分类用系统";
                    }
                    return "未分类用系统";
                })
            ));

        // 构建树结构
        List<Map<String, Object>> tree = new ArrayList<>();

        // 总行应用系统根节点
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("id", "head-office-root");
        root.put("label", "总行应用系统");
        root.put("type", "root");
        root.put("count", apps.size());

        List<Map<String, Object>> layers = new ArrayList<>();
        for (String layerName : fixedLayers) {
            Map<String, Object> layer = new LinkedHashMap<>();
            layer.put("id", "layer-" + layerName);
            layer.put("label", layerName);
            layer.put("type", "layer");

            Map<String, List<ArchApplication>> layerGroups = grouped.getOrDefault(layerName, new HashMap<>());
            int layerCount = layerGroups.values().stream().mapToInt(List::size).sum();
            layer.put("count", layerCount);

            List<Map<String, Object>> groups = new ArrayList<>();
            // 按固定顺序排列群组
            layerGroups.forEach((groupName, groupApps) -> {
                Map<String, Object> group = new LinkedHashMap<>();
                group.put("id", "group-" + groupName);
                group.put("label", groupName);
                group.put("type", "group");
                group.put("count", groupApps.size());

                // 子应用列表
                List<Map<String, Object>> appsList = new ArrayList<>();
                for (ArchApplication app : groupApps) {
                    Map<String, Object> appNode = new LinkedHashMap<>();
                    appNode.put("id", app.getId());
                    appNode.put("label", app.getAppName());
                    appNode.put("type", "app");
                    appNode.put("appCode", app.getAppCode());
                    appNode.put("status", app.getStatus());
                    appsList.add(appNode);
                }
                group.put("children", appsList);
                groups.add(group);
            });

            layer.put("children", groups);
            layers.add(layer);
        }

        root.put("children", layers);
        tree.add(root);

        return tree;
    }

    /**
     * 搜索应用（跨分层、群组、应用名称搜索）
     */
    public List<Map<String, Object>> searchApps(String keyword) {
        LambdaQueryWrapper<ArchApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), w -> w
            .like(ArchApplication::getAppName, keyword)
            .or().like(ArchApplication::getAppCode, keyword)
            .or().like(ArchApplication::getAppNameEn, keyword));
        List<ArchApplication> apps = list(wrapper);

        List<Map<String, Object>> results = new ArrayList<>();
        for (ArchApplication app : apps) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", app.getId());
            item.put("label", app.getAppName());
            item.put("appCode", app.getAppCode());
            item.put("status", app.getStatus());
            item.put("systemLayer", app.getSystemLayer() != null ? app.getSystemLayer() : "未分类用层");

            // 获取父应用名称作为群组名
            if (app.getParentAppId() != null) {
                ArchApplication parent = getById(app.getParentAppId());
                item.put("parentAppName", parent != null ? parent.getAppName() : "未分类用系统");
            } else {
                item.put("parentAppName", "未分类用系统");
            }
            results.add(item);
        }
        return results;
    }
}
