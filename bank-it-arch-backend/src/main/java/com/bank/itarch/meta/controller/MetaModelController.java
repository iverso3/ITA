package com.bank.itarch.meta.controller;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.meta.model.dto.MetaModelDTO;
import com.bank.itarch.meta.model.entity.MetaModel;
import com.bank.itarch.meta.service.MetaModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 元模型管理控制器
 */
@Tag(name = "元模型管理")
@RestController
@RequestMapping("/v1/meta/models")
@RequiredArgsConstructor
public class MetaModelController {

    private final MetaModelService metaModelService;

    @Operation(summary = "创建元模型")
    @PostMapping
    public Result<MetaModel> createModel(@RequestBody MetaModelDTO dto) {
        MetaModel model = metaModelService.createModel(dto);
        return Result.success("创建成功", model);
    }

    @Operation(summary = "更新元模型")
    @PutMapping("/{id}")
    public Result<MetaModel> updateModel(
            @Parameter(description = "模型ID") @PathVariable Long id,
            @RequestBody MetaModelDTO dto) {
        MetaModel model = metaModelService.updateModel(id, dto);
        return Result.success("更新成功", model);
    }

    @Operation(summary = "删除元模型")
    @DeleteMapping("/{id}")
    public Result<Void> deleteModel(
            @Parameter(description = "模型ID") @PathVariable Long id) {
        metaModelService.deleteModel(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "分页查询元模型")
    @GetMapping("/page")
    public Result<PageResult<MetaModel>> pageQuery(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "模型类型") @RequestParam(required = false) String modelType,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        PageResult<MetaModel> result = metaModelService.pageQuery(query, keyword, modelType, status);
        return Result.success(result);
    }

    @Operation(summary = "获取元模型详情")
    @GetMapping("/{id}")
    public Result<MetaModelDTO> getModelDetail(
            @Parameter(description = "模型ID") @PathVariable Long id) {
        MetaModelDTO detail = metaModelService.getModelDetail(id);
        return Result.success(detail);
    }

    @Operation(summary = "通过编码获取元模型")
    @GetMapping("/code/{modelCode}")
    public Result<MetaModel> getByCode(
            @Parameter(description = "模型编码") @PathVariable String modelCode) {
        MetaModel model = metaModelService.getByCode(modelCode);
        return Result.success(model);
    }

    @Operation(summary = "获取所有启用的元模型")
    @GetMapping("/list")
    public Result<List<MetaModel>> listAllActive() {
        List<MetaModel> models = metaModelService.listAllActive();
        return Result.success(models);
    }

    @Operation(summary = "根据类型查询元模型")
    @GetMapping("/type/{modelType}")
    public Result<List<MetaModel>> listByType(
            @Parameter(description = "模型类型") @PathVariable String modelType) {
        List<MetaModel> models = metaModelService.listByType(modelType);
        return Result.success(models);
    }
}
