package com.bank.itarch.controller;

import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.model.dto.OssSoftwareBaselineDTO;
import com.bank.itarch.model.dto.OssSoftwareBaselineQueryDTO;
import com.bank.itarch.model.dto.OssSoftwareMediaDTO;
import com.bank.itarch.model.dto.OssSoftwareMediaQueryDTO;
import com.bank.itarch.service.OssSoftwareBaselineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/v1/oss/software/baseline")
@RequiredArgsConstructor
@Tag(name = "开源软件版本管理")
public class OssSoftwareBaselineController {

    private final OssSoftwareBaselineService baselineService;

    @GetMapping("/list")
    @Operation(summary = "版本清单列表")
    public Result<PageResult<OssSoftwareBaselineDTO>> list(OssSoftwareBaselineQueryDTO query) {
        return Result.success(baselineService.pageQuery(query));
    }

    @GetMapping("/media/list")
    @Operation(summary = "介质文档列表")
    public Result<PageResult<OssSoftwareMediaDTO>> mediaList(OssSoftwareMediaQueryDTO query) {
        return Result.success(baselineService.pageQueryMedia(query));
    }

    @GetMapping("/media/download")
    @Operation(summary = "下载介质文件")
    public ResponseEntity<Resource> download(@RequestParam String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "版本详情")
    public Result<OssSoftwareBaselineDTO> getById(@PathVariable String id) {
        return Result.success(baselineService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建版本")
    public Result<OssSoftwareBaselineDTO> create(@RequestBody OssSoftwareBaselineDTO dto) {
        return Result.success("创建成功", baselineService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新版本")
    public Result<OssSoftwareBaselineDTO> update(@PathVariable String id, @RequestBody OssSoftwareBaselineDTO dto) {
        return Result.success("更新成功", baselineService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除版本")
    public Result<Void> delete(@PathVariable String id) {
        baselineService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/export")
    @Operation(summary = "导出版本清单")
    public Result<List<OssSoftwareBaselineDTO>> export(OssSoftwareBaselineQueryDTO query) {
        return Result.success(baselineService.listAllForExport(query));
    }
}
