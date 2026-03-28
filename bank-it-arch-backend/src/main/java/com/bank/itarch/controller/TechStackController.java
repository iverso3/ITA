package com.bank.itarch.controller;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.model.entity.ArchTechStack;
import com.bank.itarch.service.ArchTechStackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/tech")
@RequiredArgsConstructor
@Tag(name = "技术架构管理")
public class TechStackController {

    private final ArchTechStackService techStackService;

    @GetMapping("/stacks")
    @Operation(summary = "技术栈列表")
    public Result<PageResult<ArchTechStack>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String stackType) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setPageSize(pageSize);
        return Result.success(techStackService.pageQuery(query, keyword, stackType));
    }

    @GetMapping("/stacks/tree")
    @Operation(summary = "技术栈树")
    public Result<List<Map<String, Object>>> tree() {
        return Result.success(techStackService.getTree());
    }

    @GetMapping("/stacks/{id}")
    @Operation(summary = "技术栈详情")
    public Result<ArchTechStack> getById(@PathVariable Long id) {
        return Result.success(techStackService.getById(id));
    }

    @PostMapping("/stacks")
    @Operation(summary = "新增技术栈")
    public Result<ArchTechStack> create(@RequestBody ArchTechStack entity) {
        return Result.success("创建成功", techStackService.create(entity));
    }

    @PutMapping("/stacks/{id}")
    @Operation(summary = "编辑技术栈")
    public Result<ArchTechStack> update(@PathVariable Long id, @RequestBody ArchTechStack entity) {
        return Result.success("更新成功", techStackService.update(id, entity));
    }

    @DeleteMapping("/stacks/{id}")
    @Operation(summary = "删除技术栈")
    public Result<Void> delete(@PathVariable Long id) {
        techStackService.delete(id);
        return Result.success("删除成功", null);
    }
}
