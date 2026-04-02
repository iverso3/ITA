package com.bank.itarch.controller;

import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.model.dto.ApplicationDetailDTO;
import com.bank.itarch.model.entity.*;
import com.bank.itarch.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/arch")
@RequiredArgsConstructor
@Tag(name = "应用架构管理")
public class ArchApplicationController {

    private final ArchApplicationService applicationService;
    private final ArchApplicationModuleService moduleService;
    private final ArchServiceService serviceService;
    private final ArchDependencyService dependencyService;

    @GetMapping("/applications")
    @Operation(summary = "应用列表")
    public Result<PageResult<ArchApplication>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String lifecycle,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String systemLayer,
            @RequestParam(required = false) String parentAppName) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        return Result.success(applicationService.pageQuery(query, keyword, status, lifecycle, departmentId, systemLayer, parentAppName));
    }

    @GetMapping("/applications/layered-tree")
    @Operation(summary = "获取应用分层树结构")
    public Result<List<Map<String, Object>>> getLayeredTree() {
        return Result.success(applicationService.getLayeredTree());
    }

    @GetMapping("/applications/search")
    @Operation(summary = "搜索应用（跨层级）")
    public Result<List<Map<String, Object>>> searchApps(@RequestParam(required = false) String keyword) {
        return Result.success(applicationService.searchApps(keyword));
    }

    @GetMapping("/applications/{id}")
    @Operation(summary = "应用详情")
    public Result<ApplicationDetailDTO> getById(@PathVariable Long id) {
        return Result.success(applicationService.getDetail(id));
    }

    @PostMapping("/applications")
    @Operation(summary = "新增应用")
    public Result<ArchApplication> create(@RequestBody ArchApplication entity) {
        return Result.success("创建成功", applicationService.create(entity));
    }

    @PutMapping("/applications/{id}")
    @Operation(summary = "编辑应用")
    public Result<ArchApplication> update(@PathVariable Long id, @RequestBody ArchApplication entity) {
        return Result.success("更新成功", applicationService.update(id, entity));
    }

    @DeleteMapping("/applications/{id}")
    @Operation(summary = "删除应用")
    public Result<Void> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/applications/export")
    @Operation(summary = "导出应用列表")
    public Result<List<ArchApplication>> export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String lifecycle,
            @RequestParam(required = false) Long departmentId) {
        List<ArchApplication> list = applicationService.exportList(keyword, status, lifecycle, departmentId);
        return Result.success(list);
    }

    @PostMapping("/applications/import")
    @Operation(summary = "导入应用")
    public Result<com.bank.itarch.model.dto.ImportResult> importApplications(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            com.bank.itarch.model.dto.ImportResult result = applicationService.importList(file);
            return Result.success(result);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/applications/{id}/modules")
    @Operation(summary = "应用模块列表")
    public Result<List<ArchApplicationModule>> listModules(@PathVariable Long id) {
        return Result.success(moduleService.listByAppId(id));
    }

    @PostMapping("/modules")
    @Operation(summary = "新增模块")
    public Result<ArchApplicationModule> createModule(@RequestBody ArchApplicationModule entity) {
        return Result.success("创建成功", moduleService.create(entity));
    }

    @PutMapping("/modules/{id}")
    @Operation(summary = "编辑模块")
    public Result<ArchApplicationModule> updateModule(@PathVariable Long id, @RequestBody ArchApplicationModule entity) {
        return Result.success("更新成功", moduleService.update(id, entity));
    }

    @DeleteMapping("/modules/{id}")
    @Operation(summary = "删除模块")
    public Result<Void> deleteModule(@PathVariable Long id) {
        moduleService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/applications/{id}/services")
    @Operation(summary = "应用服务列表")
    public Result<List<ArchService>> listServices(@PathVariable Long id) {
        return Result.success(serviceService.listByAppId(id));
    }

    @PostMapping("/services")
    @Operation(summary = "新增服务")
    public Result<ArchService> createService(@RequestBody ArchService entity) {
        return Result.success("创建成功", serviceService.create(entity));
    }

    @PutMapping("/services/{id}")
    @Operation(summary = "编辑服务")
    public Result<ArchService> updateService(@PathVariable Long id, @RequestBody ArchService entity) {
        return Result.success("更新成功", serviceService.update(id, entity));
    }

    @DeleteMapping("/services/{id}")
    @Operation(summary = "删除服务")
    public Result<Void> deleteService(@PathVariable Long id) {
        serviceService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/applications/{id}/dependencies")
    @Operation(summary = "应用依赖列表")
    public Result<List<ArchDependency>> listDependencies(@PathVariable Long id) {
        return Result.success(dependencyService.listByAppId(id));
    }

    @PostMapping("/dependencies")
    @Operation(summary = "添加依赖")
    public Result<ArchDependency> createDependency(@RequestBody ArchDependency entity) {
        return Result.success("创建成功", dependencyService.create(entity));
    }

    @DeleteMapping("/dependencies/{id}")
    @Operation(summary = "删除依赖")
    public Result<Void> deleteDependency(@PathVariable Long id) {
        dependencyService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/dependencies/graph")
    @Operation(summary = "依赖拓扑图")
    public Result<Map<String, Object>> dependencyGraph() {
        return Result.success(dependencyService.getDependencyGraph());
    }
}
