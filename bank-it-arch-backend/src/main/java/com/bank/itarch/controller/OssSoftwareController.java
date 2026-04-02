package com.bank.itarch.controller;

import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.model.dto.OssSoftwareDTO;
import com.bank.itarch.model.dto.OssSoftwareQueryDTO;
import com.bank.itarch.service.OssSoftwareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/oss/software")
@RequiredArgsConstructor
@Tag(name = "开源软件管理")
public class OssSoftwareController {

    private final OssSoftwareService ossSoftwareService;

    @GetMapping("/list")
    @Operation(summary = "软件清单列表")
    public Result<PageResult<OssSoftwareDTO>> list(OssSoftwareQueryDTO query) {
        return Result.success(ossSoftwareService.pageQuery(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "软件详情")
    public Result<OssSoftwareDTO> getById(@PathVariable String id) {
        return Result.success(ossSoftwareService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建软件")
    public Result<OssSoftwareDTO> create(@RequestBody OssSoftwareDTO dto) {
        return Result.success("创建成功", ossSoftwareService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新软件")
    public Result<OssSoftwareDTO> update(@PathVariable String id, @RequestBody OssSoftwareDTO dto) {
        return Result.success("更新成功", ossSoftwareService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除软件")
    public Result<Void> delete(@PathVariable String id) {
        ossSoftwareService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/export")
    @Operation(summary = "导出软件清单")
    public Result<List<OssSoftwareDTO>> export(OssSoftwareQueryDTO query) {
        return Result.success(ossSoftwareService.listAllForExport(query));
    }
}
