package com.bank.itarch.meta.controller;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.meta.engine.MetaFormEngine;
import com.bank.itarch.meta.engine.MetaQueryEngine;
import com.bank.itarch.meta.model.dto.MetaEntityDTO;
import com.bank.itarch.meta.model.entity.MetaEntity;
import com.bank.itarch.meta.model.entity.MetaModel;
import com.bank.itarch.meta.model.vo.*;
import com.bank.itarch.meta.service.MetaAuditService;
import com.bank.itarch.meta.service.MetaEntityService;
import com.bank.itarch.meta.service.MetaModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 动态CRUD控制器
 * 提供统一的动态数据访问API
 */
@Tag(name = "动态数据管理")
@RestController
@RequestMapping("/v1/meta/dynamic")
@RequiredArgsConstructor
public class MetaDynamicController {

    private final MetaQueryEngine queryEngine;
    private final MetaFormEngine formEngine;
    private final MetaEntityService entityService;
    private final MetaAuditService auditService;
    private final MetaModelService metaModelService;

    @Operation(summary = "动态分页查询")
    @PostMapping("/query/{modelCode}")
    public Result<PageResult<Map<String, Object>>> query(
            @Parameter(description = "模型编码") @PathVariable String modelCode,
            @RequestBody QueryRequest request) {
        Long modelId = getModelIdByCode(modelCode);
        PageResult<Map<String, Object>> result = queryEngine.executeQuery(
                modelId,
                request.getQuery(),
                request.getFilters(),
                request.getSelectFields(),
                request.getSortField(),
                request.getSortOrder()
        );
        return Result.success(result);
    }

    @Operation(summary = "动态单条查询")
    @GetMapping("/get/{modelCode}/{id}")
    public Result<Map<String, Object>> get(
            @Parameter(description = "模型编码") @PathVariable String modelCode,
            @Parameter(description = "实体ID") @PathVariable Long id,
            @RequestParam(required = false) List<String> selectFields) {
        Long modelId = getModelIdByCode(modelCode);
        Map<String, Object> result = queryEngine.executeGet(modelId, id, selectFields);

        // 脱敏处理
        if (result != null) {
            result = auditService.maskSensitiveData(modelId, result);
        }

        return Result.success(result);
    }

    @Operation(summary = "动态创建")
    @PostMapping("/create/{modelCode}")
    public Result<MetaEntity> create(
            @Parameter(description = "模型编码") @PathVariable String modelCode,
            @RequestBody CreateRequest request) {
        Long modelId = getModelIdByCode(modelCode);
        request.getEntity().setModelId(modelId);
        request.getEntity().setModelCode(modelCode);
        MetaEntity entity = entityService.createEntity(request.getEntity());
        return Result.success("创建成功", entity);
    }

    @Operation(summary = "动态更新")
    @PutMapping("/update/{id}")
    public Result<MetaEntity> update(
            @Parameter(description = "实体ID") @PathVariable Long id,
            @RequestBody CreateRequest request) {
        MetaEntity entity = entityService.updateEntity(id, request.getEntity());
        return Result.success("更新成功", entity);
    }

    @Operation(summary = "动态删除")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(
            @Parameter(description = "实体ID") @PathVariable Long id) {
        entityService.deleteEntity(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取表单配置")
    @GetMapping("/form-config/{modelCode}")
    public Result<MetaFormConfigVO> getFormConfig(
            @Parameter(description = "模型编码") @PathVariable String modelCode,
            @Parameter(description = "表单类型") @RequestParam(required = false, defaultValue = "default") String formType) {
        Long modelId = getModelIdByCode(modelCode);
        MetaFormConfigVO config = formEngine.buildFormConfig(modelId, formType);
        return Result.success(config);
    }

    @Operation(summary = "获取表格配置")
    @GetMapping("/table-config/{modelCode}")
    public Result<MetaTableConfigVO> getTableConfig(
            @Parameter(description = "模型编码") @PathVariable String modelCode) {
        Long modelId = getModelIdByCode(modelCode);
        MetaFormConfigVO formConfig = formEngine.buildFormConfig(modelId, "default");

        // 转换为表格配置
        MetaTableConfigVO tableConfig = new MetaTableConfigVO();
        tableConfig.setModelId(modelId);
        tableConfig.setModelCode(modelCode);
        tableConfig.setModelName(formConfig.getModelName());

        // 构建列配置
        List<MetaTableColumnVO> columns = formConfig.getGroups().stream()
                .flatMap(g -> g.getFields().stream())
                .map(f -> {
                    MetaTableColumnVO col = new MetaTableColumnVO();
                    col.setFieldCode(f.getFieldCode());
                    col.setFieldName(f.getFieldName());
                    col.setFieldType(f.getFieldType());
                    col.setDisplayType(f.getDisplayType());
                    col.setWidth(150);
                    col.setSortable(true);
                    col.setSearchable(f.getIsRequired() != null && f.getIsRequired());
                    return col;
                })
                .collect(java.util.stream.Collectors.toList());

        // 添加系统列
        columns.add(0, createColumn("entityCode", "编码", "STRING", true));
        columns.add(1, createColumn("entityName", "名称", "STRING", true));
        columns.add(createColumn("status", "状态", "STRING", true));
        columns.add(createColumn("createTime", "创建时间", "DATETIME", true));

        tableConfig.setColumns(columns);

        // 工具栏配置
        MetaTableToolbarVO toolbar = new MetaTableToolbarVO();
        toolbar.setShowSearch(true);
        toolbar.setShowRefresh(true);
        toolbar.setShowColumns(true);
        toolbar.setShowExport(true);
        toolbar.setShowAdd(true);
        toolbar.setShowBatchDelete(true);
        tableConfig.setToolbar(toolbar);

        // 分页配置
        MetaTablePaginationVO pagination = new MetaTablePaginationVO();
        pagination.setShow(true);
        pagination.setPageSize(20);
        pagination.setPageSizes(20);
        pagination.setLayout("total, sizes, prev, pager, next, jumper");
        tableConfig.setPagination(pagination);

        return Result.success(tableConfig);
    }

    @Operation(summary = "获取字段配置")
    @GetMapping("/field-config/{modelCode}/{fieldCode}")
    public Result<MetaFormFieldVO> getFieldConfig(
            @Parameter(description = "模型编码") @PathVariable String modelCode,
            @Parameter(description = "字段编码") @PathVariable String fieldCode) {
        Long modelId = getModelIdByCode(modelCode);
        MetaFormFieldVO fieldConfig = formEngine.buildFieldConfig(modelId, fieldCode);
        return Result.success(fieldConfig);
    }

    @Operation(summary = "获取下拉选项")
    @GetMapping("/options/{dictCode}")
    public Result<List<MetaOptionVO>> getOptions(
            @Parameter(description = "字典编码") @PathVariable String dictCode) {
        List<MetaOptionVO> options = formEngine.getDictItems(dictCode);
        return Result.success(options);
    }

    // ========== 辅助方法 ==========

    private Long getModelIdByCode(String modelCode) {
        MetaModel model = metaModelService.getByCode(modelCode);
        if (model == null) {
            throw new RuntimeException("模型不存在: " + modelCode);
        }
        return model.getId();
    }

    private MetaTableColumnVO createColumn(String fieldCode, String fieldName,
                                           String fieldType, Boolean sortable) {
        MetaTableColumnVO col = new MetaTableColumnVO();
        col.setFieldCode(fieldCode);
        col.setFieldName(fieldName);
        col.setFieldType(fieldType);
        col.setDisplayType("TEXT");
        col.setWidth(150);
        col.setSortable(sortable);
        col.setSearchable(false);
        return col;
    }

    // ========== 请求DTO ==========

    @lombok.Data
    public static class QueryRequest {
        private PageQuery query;
        private Map<String, Object> filters;
        private List<String> selectFields;
        private String sortField;
        private String sortOrder;
    }

    @lombok.Data
    public static class CreateRequest {
        private MetaEntityDTO entity;
    }
}
