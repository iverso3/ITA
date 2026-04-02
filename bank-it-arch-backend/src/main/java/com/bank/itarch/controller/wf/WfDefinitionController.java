package com.bank.itarch.controller.wf;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.mapper.WfDefinitionMapper;
import com.bank.itarch.model.entity.WfDefinition;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/wf/definitions")
@RequiredArgsConstructor
@Tag(name = "流程定义")
public class WfDefinitionController {

    private final WfDefinitionMapper definitionMapper;

    @GetMapping
    @Operation(summary = "流程定义列表")
    public Result<PageResult<WfDefinition>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<WfDefinition> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<WfDefinition> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(WfDefinition::getDefinitionName, keyword)
                    .or()
                    .like(WfDefinition::getDefinitionCode, keyword);
        }
        wrapper.orderByDesc(WfDefinition::getCreateTime);
        IPage<WfDefinition> result = definitionMapper.selectPage(p, wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "流程定义详情")
    public Result<WfDefinition> getById(@PathVariable Long id) {
        WfDefinition definition = definitionMapper.selectById(id);
        return Result.success(definition);
    }

    @PostMapping
    @Operation(summary = "创建流程定义")
    public Result<WfDefinition> create(@RequestBody WfDefinition definition) {
        definitionMapper.insert(definition);
        return Result.success("创建成功", definition);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新流程定义")
    public Result<WfDefinition> update(@PathVariable Long id, @RequestBody WfDefinition definition) {
        definition.setId(id);
        definitionMapper.updateById(definition);
        return Result.success("更新成功", definition);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除流程定义")
    public Result<Void> delete(@PathVariable Long id) {
        definitionMapper.deleteById(id);
        return Result.success("删除成功", null);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "启用/停用流程定义")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        WfDefinition definition = definitionMapper.selectById(id);
        if (definition != null) {
            definition.setIsActive(definition.getIsActive() == 1 ? 0 : 1);
            definitionMapper.updateById(definition);
        }
        return Result.success("操作成功", null);
    }
}
