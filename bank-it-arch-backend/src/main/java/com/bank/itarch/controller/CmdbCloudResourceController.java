package com.bank.itarch.controller;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.model.entity.CmdbCloudResource;
import com.bank.itarch.service.CmdbCloudResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/cmdb/cloud-resources")
@RequiredArgsConstructor
@Tag(name = "CMDB-云资源管理")
public class CmdbCloudResourceController {

    private final CmdbCloudResourceService service;

    @GetMapping
    @Operation(summary = "云资源列表")
    public Result<PageResult<CmdbCloudResource>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        return Result.success(service.pageQuery(query, keyword, status, departmentId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "云资源详情")
    public Result<CmdbCloudResource> getById(@PathVariable Long id) {
        return Result.success(service.detail(id));
    }

    @PostMapping
    @Operation(summary = "创建云资源")
    public Result<CmdbCloudResource> create(@RequestBody CmdbCloudResource entity) {
        return Result.success("创建成功", service.create(entity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新云资源")
    public Result<CmdbCloudResource> update(@PathVariable Long id, @RequestBody CmdbCloudResource entity) {
        return Result.success("更新成功", service.update(id, entity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除云资源")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success("删除成功", null);
    }
}
