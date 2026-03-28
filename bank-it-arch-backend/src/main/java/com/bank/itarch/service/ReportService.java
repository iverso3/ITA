package com.bank.itarch.service;

import com.bank.itarch.model.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ArchApplicationService applicationService;
    private final CmdbServerService serverService;
    private final CmdbNetworkService networkService;
    private final CmdbStorageService storageService;
    private final CmdbCloudResourceService cloudResourceService;
    private final ArchDependencyService dependencyService;
    private final ArchTechStackService techStackService;
    private final ArchDataEntityService dataEntityService;
    private final ArchDataFlowService dataFlowService;

    public Map<String, Object> systemStatistics() {
        List<ArchApplication> apps = applicationService.list();
        Map<String, Object> data = new HashMap<>();
        data.put("total", apps.size());
        data.put("appCount", (long) apps.size());
        data.put("serverCount", serverService.count());
        data.put("techStackCount", techStackService.count());
        data.put("entityCount", dataEntityService.count());

        Map<String, Long> byStatus = apps.stream().collect(Collectors.groupingBy(
                a -> a.getStatus() != null ? a.getStatus() : "UNKNOWN", Collectors.counting()));
        data.put("byStatus", byStatus);

        Map<String, Long> byImportance = apps.stream().collect(Collectors.groupingBy(
                a -> a.getImportanceLevel() != null ? a.getImportanceLevel() : "未设置", Collectors.counting()));
        data.put("byImportance", byImportance);

        Map<String, Long> byLifecycle = apps.stream().collect(Collectors.groupingBy(
                a -> a.getLifecycle() != null ? a.getLifecycle() : "UNKNOWN", Collectors.counting()));
        data.put("byLifecycle", byLifecycle);

        Map<String, Long> byDept = apps.stream().collect(Collectors.groupingBy(
                a -> a.getDepartmentName() != null ? a.getDepartmentName() : "未分配", Collectors.counting()));
        List<Map<String, Object>> byDepartment = byDept.entrySet().stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("departmentName", e.getKey());
            m.put("count", e.getValue());
            return m;
        }).collect(Collectors.toList());
        data.put("byDepartment", byDepartment);

        // App type distribution for charts
        Map<String, Long> byType = apps.stream().collect(Collectors.groupingBy(
                a -> a.getAppType() != null ? a.getAppType() : "其他", Collectors.counting()));
        List<Map<String, Object>> appTypeDistribution = byType.entrySet().stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("name", e.getKey());
            m.put("value", e.getValue());
            return m;
        }).collect(Collectors.toList());
        data.put("appTypeDistribution", appTypeDistribution);

        // Lifecycle distribution for charts
        List<Map<String, Object>> lifecycleDistribution = byLifecycle.entrySet().stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("name", e.getKey());
            m.put("value", e.getValue());
            return m;
        }).collect(Collectors.toList());
        data.put("lifecycleDistribution", lifecycleDistribution);

        return data;
    }

    public Map<String, Object> resourceStatistics() {
        Map<String, Object> data = new HashMap<>();
        data.put("serverCount", serverService.count());
        data.put("networkCount", networkService.count());
        data.put("storageCount", storageService.count());
        data.put("cloudResourceCount", cloudResourceService.count());

        // Detail breakdown
        List<Map<String, Object>> serverStatus = new ArrayList<>();
        serverStatus.add(Map.of("name", "运行中", "value", serverService.count()));
        data.put("serverStatus", serverStatus);

        return data;
    }

    public Map<String, Object> systemPanorama() {
        List<ArchApplication> apps = applicationService.list();
        List<ArchDependency> deps = dependencyService.list();

        List<Map<String, Object>> nodes = apps.stream().map(a -> {
            Map<String, Object> n = new HashMap<>();
            n.put("id", a.getId());
            n.put("name", a.getAppName());
            n.put("group", a.getAppType());
            n.put("status", a.getStatus());
            n.put("importance", a.getImportanceLevel());
            return n;
        }).collect(Collectors.toList());

        List<Map<String, Object>> links = deps.stream().map(d -> {
            Map<String, Object> l = new HashMap<>();
            l.put("source", d.getSourceAppId());
            l.put("target", d.getTargetAppId());
            l.put("type", "dependency");
            l.put("label", d.getDependencyType());
            return l;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("nodes", nodes);
        data.put("links", links);
        return data;
    }

    public Map<String, Object> techStackDistribution() {
        List<ArchTechStack> stacks = techStackService.list();
        Map<String, Long> byType = stacks.stream().collect(Collectors.groupingBy(
                s -> s.getStackType() != null ? s.getStackType() : "其他", Collectors.counting()));
        List<Map<String, Object>> distribution = byType.entrySet().stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("name", e.getKey());
            m.put("value", e.getValue());
            return m;
        }).collect(Collectors.toList());
        return Map.of("distribution", distribution);
    }

    public Map<String, Object> departmentStatistics() {
        List<ArchApplication> apps = applicationService.list();
        Map<String, Long> byDept = apps.stream().collect(Collectors.groupingBy(
                a -> a.getDepartmentName() != null ? a.getDepartmentName() : "未分配", Collectors.counting()));
        List<Map<String, Object>> stats = byDept.entrySet().stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("departmentName", e.getKey());
            m.put("count", e.getValue());
            return m;
        }).collect(Collectors.toList());
        return Map.of("departments", stats);
    }

    public Map<String, Object> dataDistribution() {
        List<ArchDataEntity> entities = dataEntityService.list();
        Map<String, Long> byDbType = entities.stream().collect(Collectors.groupingBy(
                e -> e.getDbType() != null ? e.getDbType() : "其他", Collectors.counting()));
        List<Map<String, Object>> distribution = byDbType.entrySet().stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("name", e.getKey());
            m.put("value", e.getValue());
            return m;
        }).collect(Collectors.toList());
        return Map.of("distribution", distribution);
    }

    public Map<String, Object> techStackPanorama() {
        List<ArchTechStack> stacks = techStackService.list();
        List<Map<String, Object>> data = stacks.stream().map(s -> {
            Map<String, Object> n = new HashMap<>();
            n.put("name", s.getStackName());
            n.put("value", 1);
            n.put("stackType", s.getStackType());
            n.put("version", s.getVersion());
            return n;
        }).collect(Collectors.toList());
        return Map.of("nodes", data);
    }

    public Map<String, Object> dataFlowPanorama() {
        List<ArchDataFlow> flows = dataFlowService.list();
        List<ArchApplication> apps = applicationService.list();

        List<Map<String, Object>> nodes = apps.stream().map(a -> {
            Map<String, Object> n = new HashMap<>();
            n.put("id", a.getId());
            n.put("name", a.getAppName());
            return n;
        }).collect(Collectors.toList());

        List<Map<String, Object>> links = flows.stream().map(f -> {
            Map<String, Object> l = new HashMap<>();
            l.put("source", f.getSourceAppId());
            l.put("target", f.getTargetAppId());
            l.put("flowType", f.getFlowType());
            return l;
        }).collect(Collectors.toList());

        return Map.of("nodes", nodes, "links", links);
    }
}
