package com.bank.itarch.controller;

import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.model.dto.OssUseStandingBookDetailsDTO;
import com.bank.itarch.model.dto.OssUseStandingBookMainDTO;
import com.bank.itarch.model.dto.OssUseStandingBookPageQuery;
import com.bank.itarch.service.OssUseStandingBookDetailsService;
import com.bank.itarch.service.OssUseStandingBookMainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/oss/standing-book")
@RequiredArgsConstructor
@Tag(name = "使用台账管理")
public class OssUseStandingBookController {

    private final OssUseStandingBookMainService mainService;
    private final OssUseStandingBookDetailsService detailsService;

    // ==================== Main表接口 ====================

    @GetMapping("/main/list")
    @Operation(summary = "台账主列表")
    public Result<PageResult<OssUseStandingBookMainDTO>> listMain(OssUseStandingBookPageQuery query) {
        return Result.success(mainService.pageQuery(query));
    }

    @GetMapping("/main/{id}")
    @Operation(summary = "台账主详情")
    public Result<OssUseStandingBookMainDTO> getMainById(@PathVariable String id) {
        return Result.success(mainService.getById(id));
    }

    @PostMapping("/main")
    @Operation(summary = "创建台账主记录")
    public Result<OssUseStandingBookMainDTO> createMain(@RequestBody OssUseStandingBookMainDTO dto) {
        return Result.success("创建成功", mainService.create(dto));
    }

    @PutMapping("/main/{id}")
    @Operation(summary = "更新台账主记录")
    public Result<OssUseStandingBookMainDTO> updateMain(@PathVariable String id, @RequestBody OssUseStandingBookMainDTO dto) {
        return Result.success("更新成功", mainService.update(id, dto));
    }

    @DeleteMapping("/main/{id}")
    @Operation(summary = "删除台账主记录")
    public Result<Void> deleteMain(@PathVariable String id) {
        mainService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/main/export")
    @Operation(summary = "导出台账主列表")
    public Result<List<OssUseStandingBookMainDTO>> exportMain(OssUseStandingBookPageQuery query) {
        return Result.success(mainService.listAllForExport(query));
    }

    // ==================== Detail表接口 ====================

    @GetMapping("/details")
    @Operation(summary = "台账明细列表（按parentId）")
    public Result<PageResult<OssUseStandingBookDetailsDTO>> listDetails(
            @RequestParam String parentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(detailsService.pageQueryByParentId(parentId, page, pageSize));
    }

    @GetMapping("/details/{id}")
    @Operation(summary = "台账明细详情")
    public Result<OssUseStandingBookDetailsDTO> getDetailById(@PathVariable String id) {
        return Result.success(detailsService.getById(id));
    }

    @PostMapping("/details")
    @Operation(summary = "创建台账明细记录")
    public Result<OssUseStandingBookDetailsDTO> createDetail(@RequestBody OssUseStandingBookDetailsDTO dto) {
        return Result.success("创建成功", detailsService.create(dto));
    }

    @PutMapping("/details/{id}")
    @Operation(summary = "更新台账明细记录")
    public Result<OssUseStandingBookDetailsDTO> updateDetail(@PathVariable String id, @RequestBody OssUseStandingBookDetailsDTO dto) {
        return Result.success("更新成功", detailsService.update(id, dto));
    }

    @DeleteMapping("/details/{id}")
    @Operation(summary = "删除台账明细记录")
    public Result<Void> deleteDetail(@PathVariable String id) {
        detailsService.delete(id);
        return Result.success("删除成功", null);
    }
}