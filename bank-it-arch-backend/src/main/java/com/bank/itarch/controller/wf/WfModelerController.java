package com.bank.itarch.controller.wf;

import com.bank.itarch.common.Result;
import com.bank.itarch.engine.WfProcessEngine;
import com.bank.itarch.mapper.*;
import com.bank.itarch.model.dto.*;
import com.bank.itarch.model.entity.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流程建模器控制器
 */
@RestController
@RequestMapping("/v1/wf/modeler")
@RequiredArgsConstructor
@Tag(name = "流程建模器")
public class WfModelerController {

    private final WfDefinitionMapper definitionMapper;
    private final WfDefinitionNodeMapper nodeMapper;
    private final WfDefinitionLineMapper lineMapper;
    private final WfDefinitionVersionMapper versionMapper;
    private final WfProcessEngine processEngine;

    @GetMapping("/{definitionId}")
    @Operation(summary = "获取流程模型")
    public Result<WfProcessModelDTO> getProcessModel(@PathVariable Long definitionId) {
        WfDefinition definition = definitionMapper.selectById(definitionId);
        if (definition == null) {
            return Result.error("流程定义不存在");
        }

        List<WfDefinitionNode> nodes = nodeMapper.selectList(
            new LambdaQueryWrapper<WfDefinitionNode>()
                .eq(WfDefinitionNode::getDefinitionId, definitionId)
                .orderByAsc(WfDefinitionNode::getNodeOrder));

        List<WfDefinitionLine> lines = lineMapper.selectList(
            new LambdaQueryWrapper<WfDefinitionLine>()
                .eq(WfDefinitionLine::getDefinitionId, definitionId));

        // 转换为DTO
        WfProcessModelDTO model = new WfProcessModelDTO();
        model.setDefinitionId(definition.getId());
        model.setDefinitionCode(definition.getDefinitionCode());
        model.setDefinitionName(definition.getDefinitionName());
        model.setProcessType(definition.getProcessType());
        model.setVersion(definition.getVersion() != null ? (int) Double.parseDouble(definition.getVersion()) : 1);
        model.setStatus("DRAFT");

        List<WfNodeDTO> nodeDTOs = nodes.stream().map(n -> {
            WfNodeDTO dto = new WfNodeDTO();
            dto.setId(n.getId());
            dto.setDefinitionId(n.getDefinitionId());
            dto.setNodeCode(n.getNodeCode());
            dto.setNodeName(n.getNodeName());
            dto.setNodeOrder(n.getNodeOrder());
            dto.setNodeType(n.getNodeType());
            dto.setNodeCategory(n.getNodeCategory());
            dto.setApprovalType(n.getApprovalType());
            dto.setAssignedRoleId(n.getAssignedRoleId());
            dto.setFlowRoleId(n.getFlowRoleId());
            dto.setTimeoutDuration(n.getTimeoutDuration());
            dto.setTimeoutAction(n.getTimeoutAction());
            dto.setTimeoutTransferUser(n.getTimeoutTransferUser());
            dto.setFormSchema(n.getFormSchema());
            dto.setConditionRule(n.getConditionRule());
            dto.setPositionX(n.getPositionX());
            dto.setPositionY(n.getPositionY());
            return dto;
        }).toList();

        List<WfLineDTO> lineDTOs = lines.stream().map(l -> {
            WfLineDTO dto = new WfLineDTO();
            dto.setId(l.getId());
            dto.setDefinitionId(l.getDefinitionId());
            dto.setSourceNodeId(l.getSourceNodeId());
            dto.setTargetNodeId(l.getTargetNodeId());
            dto.setLineCode(l.getLineCode());
            dto.setLineName(l.getLineName());
            dto.setConditionExpr(l.getConditionExpr());
            dto.setLineOrder(l.getLineOrder());
            return dto;
        }).toList();

        model.setNodes(nodeDTOs);
        model.setLines(lineDTOs);

        return Result.success(model);
    }

    @PutMapping("/{definitionId}/nodes")
    @Operation(summary = "保存节点配置")
    public Result<List<WfNodeDTO>> saveNodes(@PathVariable Long definitionId, @RequestBody List<WfNodeDTO> nodes) {
        // 先删除旧的节点
        nodeMapper.delete(new LambdaQueryWrapper<WfDefinitionNode>()
            .eq(WfDefinitionNode::getDefinitionId, definitionId));

        // 保存新的节点
        List<WfNodeDTO> result = new java.util.ArrayList<>();
        for (WfNodeDTO nodeDto : nodes) {
            WfDefinitionNode node = new WfDefinitionNode();
            node.setDefinitionId(definitionId);
            node.setNodeCode(nodeDto.getNodeCode());
            node.setNodeName(nodeDto.getNodeName());
            node.setNodeOrder(nodeDto.getNodeOrder());
            node.setNodeType(nodeDto.getNodeType());
            node.setNodeCategory(nodeDto.getNodeCategory());
            node.setApprovalType(nodeDto.getApprovalType());
            node.setAssignedRoleId(nodeDto.getAssignedRoleId());
            node.setFlowRoleId(nodeDto.getFlowRoleId());
            node.setTimeoutDuration(nodeDto.getTimeoutDuration());
            node.setTimeoutAction(nodeDto.getTimeoutAction());
            node.setTimeoutTransferUser(nodeDto.getTimeoutTransferUser());
            node.setFormSchema(nodeDto.getFormSchema());
            node.setConditionRule(nodeDto.getConditionRule());
            node.setExtraAttrs(nodeDto.getExtraAttrs());
            node.setPositionX(nodeDto.getPositionX());
            node.setPositionY(nodeDto.getPositionY());

            nodeMapper.insert(node);

            // 返回保存后的节点信息
            WfNodeDTO savedDto = new WfNodeDTO();
            savedDto.setId(node.getId());
            savedDto.setNodeCode(node.getNodeCode());
            savedDto.setNodeName(node.getNodeName());
            savedDto.setNodeCategory(node.getNodeCategory());
            savedDto.setPositionX(node.getPositionX());
            savedDto.setPositionY(node.getPositionY());
            result.add(savedDto);
        }
        return Result.success(result);
    }

    @PutMapping("/{definitionId}/nodes/{nodeId}/conditionRule")
    @Operation(summary = "更新节点条件规则")
    public Result<Void> updateNodeConditionRule(
            @PathVariable Long definitionId,
            @PathVariable Long nodeId,
            @RequestBody Map<String, String> body) {
        String conditionRule = body.get("conditionRule");
        WfDefinitionNode node = nodeMapper.selectOne(
            new LambdaQueryWrapper<WfDefinitionNode>()
                .eq(WfDefinitionNode::getId, nodeId)
                .eq(WfDefinitionNode::getDefinitionId, definitionId));
        if (node == null) {
            return Result.error("节点不存在");
        }
        node.setConditionRule(conditionRule);
        nodeMapper.updateById(node);
        return Result.success("更新成功", null);
    }

    @PutMapping("/{definitionId}/lines")
    @Operation(summary = "保存连线配置")
    public Result<Void> saveLines(@PathVariable Long definitionId, @RequestBody List<WfLineDTO> lines) {
        // 先删除旧的连线
        lineMapper.delete(new LambdaQueryWrapper<WfDefinitionLine>()
            .eq(WfDefinitionLine::getDefinitionId, definitionId));

        // 保存新的连线
        for (WfLineDTO lineDto : lines) {
            WfDefinitionLine line = new WfDefinitionLine();
            line.setDefinitionId(definitionId);
            // 转换Object为Long
            line.setSourceNodeId(convertToLong(lineDto.getSourceNodeId()));
            line.setTargetNodeId(convertToLong(lineDto.getTargetNodeId()));
            line.setLineCode(lineDto.getLineCode());
            line.setLineName(lineDto.getLineName());
            line.setConditionExpr(lineDto.getConditionExpr());
            line.setLineOrder(lineDto.getLineOrder());
            lineMapper.insert(line);
        }
        return Result.success("保存成功", null);
    }

    // 辅助方法：将Object转换为Long
    private Long convertToLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        return Long.parseLong(String.valueOf(obj));
    }

    @PostMapping("/validate")
    @Operation(summary = "校验流程模型")
    public Result<String> validateModel(@RequestBody WfProcessModelDTO model) {
        // 校验流程模型连通性
        if (model.getNodes() == null || model.getNodes().isEmpty()) {
            return Result.error("流程必须至少包含一个节点");
        }

        boolean hasStart = model.getNodes().stream()
            .anyMatch(n -> "START".equals(n.getNodeCategory()));
        boolean hasEnd = model.getNodes().stream()
            .anyMatch(n -> "END".equals(n.getNodeCategory()));

        if (!hasStart) {
            return Result.error("流程必须包含开始节点");
        }
        if (!hasEnd) {
            return Result.error("流程必须包含结束节点");
        }

        if (model.getLines() == null || model.getLines().isEmpty()) {
            return Result.error("流程必须至少包含一条连线");
        }

        return Result.success("校验通过", null);
    }

    @PostMapping("/{definitionId}/publish")
    @Operation(summary = "发布流程")
    public Result<Void> publish(@PathVariable Long definitionId, @RequestBody(required = false) String changeLog) {
        WfDefinition definition = definitionMapper.selectById(definitionId);
        if (definition == null) {
            return Result.error("流程定义不存在");
        }

        // 获取最大版本号
        Integer maxVersion = versionMapper.selectList(
            new LambdaQueryWrapper<WfDefinitionVersion>()
                .eq(WfDefinitionVersion::getDefinitionId, definitionId)
                .eq(WfDefinitionVersion::getStatus, "PUBLISHED"))
            .stream()
            .map(WfDefinitionVersion::getVersion)
            .max(Integer::compareTo)
            .orElse(0);

        int newVersion = maxVersion + 1;

        // 创建新版本记录
        WfDefinitionVersion version = new WfDefinitionVersion();
        version.setDefinitionId(definitionId);
        version.setVersion(newVersion);
        version.setStatus("PUBLISHED");
        version.setPublishTime(java.time.LocalDateTime.now());
        version.setChangeLog(changeLog);
        versionMapper.insert(version);

        // 更新流程定义的当前版本号
        definition.setVersion(String.valueOf(newVersion));
        definitionMapper.updateById(definition);

        return Result.success("发布成功", null);
    }
}
