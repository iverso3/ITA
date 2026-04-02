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
import org.springframework.util.StringUtils;

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

    public int importList(org.springframework.web.multipart.MultipartFile file) throws Exception {
        // Excel导入实现
        throw new UnsupportedOperationException("Excel导入功能开发中");
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
