package com.bank.itarch.controller;

import com.bank.itarch.common.Result;
import com.bank.itarch.model.entity.ArchDeploymentArchitecture;
import com.bank.itarch.service.ArchDeploymentArchitectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/deployment")
@RequiredArgsConstructor
@Tag(name = "部署架构管理")
public class DeploymentArchController {

    private final ArchDeploymentArchitectureService deploymentService;

    @GetMapping("/applications/{appId}")
    @Operation(summary = "按应用ID获取部署架构")
    public Result<List<ArchDeploymentArchitecture>> listByApp(@PathVariable Long appId) {
        return Result.success(deploymentService.listByAppId(appId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "部署节点详情")
    public Result<ArchDeploymentArchitecture> getById(@PathVariable Long id) {
        return Result.success(deploymentService.detail(id));
    }

    @PostMapping
    @Operation(summary = "新增部署节点")
    public Result<ArchDeploymentArchitecture> create(@RequestBody ArchDeploymentArchitecture entity) {
        return Result.success("创建成功", deploymentService.create(entity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑部署节点")
    public Result<ArchDeploymentArchitecture> update(@PathVariable Long id, @RequestBody ArchDeploymentArchitecture entity) {
        return Result.success("更新成功", deploymentService.update(id, entity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部署节点")
    public Result<Void> delete(@PathVariable Long id) {
        deploymentService.delete(id);
        return Result.success("删除成功", null);
    }
}
