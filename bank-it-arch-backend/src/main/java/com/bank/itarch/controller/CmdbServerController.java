package com.bank.itarch.controller;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.model.dto.CmdbServerDTO;
import com.bank.itarch.service.CmdbServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/cmdb/servers")
@RequiredArgsConstructor
@Tag(name = "CMDB-服务器管理")
public class CmdbServerController {

    private final CmdbServerService cmdbServerService;

    @GetMapping
    @Operation(summary = "服务器列表")
    public Result<PageResult<CmdbServerDTO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        return Result.success(cmdbServerService.pageQuery(query, keyword, status, departmentId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "服务器详情")
    public Result<CmdbServerDTO> getById(@PathVariable Long id) {
        return Result.success(cmdbServerService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建服务器")
    public Result<CmdbServerDTO> create(@RequestBody CmdbServerDTO dto) {
        return Result.success("创建成功", cmdbServerService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新服务器")
    public Result<CmdbServerDTO> update(@PathVariable Long id, @RequestBody CmdbServerDTO dto) {
        return Result.success("更新成功", cmdbServerService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除服务器")
    public Result<Void> delete(@PathVariable Long id) {
        cmdbServerService.delete(id);
        return Result.success("删除成功", null);
    }
}
