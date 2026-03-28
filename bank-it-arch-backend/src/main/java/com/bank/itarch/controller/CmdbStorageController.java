package com.bank.itarch.controller;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.model.entity.CmdbStorage;
import com.bank.itarch.service.CmdbStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/cmdb/storages")
@RequiredArgsConstructor
@Tag(name = "CMDB-存储设备管理")
public class CmdbStorageController {

    private final CmdbStorageService service;

    @GetMapping
    @Operation(summary = "存储设备列表")
    public Result<PageResult<CmdbStorage>> list(
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
    @Operation(summary = "存储设备详情")
    public Result<CmdbStorage> getById(@PathVariable Long id) {
        return Result.success(service.detail(id));
    }

    @PostMapping
    @Operation(summary = "创建存储设备")
    public Result<CmdbStorage> create(@RequestBody CmdbStorage entity) {
        return Result.success("创建成功", service.create(entity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新存储设备")
    public Result<CmdbStorage> update(@PathVariable Long id, @RequestBody CmdbStorage entity) {
        return Result.success("更新成功", service.update(id, entity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除存储设备")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success("删除成功", null);
    }
}
