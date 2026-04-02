package com.bank.itarch.controller;

import com.bank.itarch.common.PageResult;
import com.bank.itarch.common.Result;
import com.bank.itarch.engine.WfProcessEngine;
import com.bank.itarch.mapper.WfInstanceVariableMapper;
import com.bank.itarch.model.dto.OssImplApplyDTO;
import com.bank.itarch.model.dto.OssImplApplyQueryDTO;
import com.bank.itarch.model.dto.OssImplApplySuplDTO;
import com.bank.itarch.model.dto.WfStartProcessDTO;
import com.bank.itarch.model.entity.OssImplApplyInfo;
import com.bank.itarch.model.entity.OssSoftware;
import com.bank.itarch.model.entity.WfInstance;
import com.bank.itarch.service.OssImplApplyService;
import com.bank.itarch.service.OssImplApplySuplService;
import com.bank.itarch.service.OssSoftwareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/oss/impl/apply")
@RequiredArgsConstructor
@Tag(name = "开源软件引入申请")
public class OssImplApplyController {

    private final OssImplApplyService implApplyService;
    private final OssImplApplySuplService implApplySuplService;
    private final OssSoftwareService softwareService;
    private final WfProcessEngine processEngine;
    private final WfInstanceVariableMapper variableMapper;

    @GetMapping("/list")
    @Operation(summary = "申请列表")
    public Result<PageResult<OssImplApplyDTO>> list(OssImplApplyQueryDTO query) {
        return Result.success(implApplyService.pageQuery(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "申请详情")
    public Result<OssImplApplyDTO> getById(@PathVariable String id) {
        return Result.success(implApplyService.getById(id));
    }

    @GetMapping("/no/{implApplyNo}")
    @Operation(summary = "根据申请单号查询申请详情")
    public Result<OssImplApplyDTO> getByImplApplyNo(@PathVariable String implApplyNo) {
        return Result.success(implApplyService.getByImplApplyNo(implApplyNo));
    }

    @PostMapping
    @Operation(summary = "创建申请")
    public Result<OssImplApplyDTO> create(@RequestBody OssImplApplyDTO dto) {
        return Result.success("创建成功", implApplyService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新申请")
    public Result<OssImplApplyDTO> update(@PathVariable String id, @RequestBody OssImplApplyDTO dto) {
        return Result.success("更新成功", implApplyService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除申请")
    public Result<Void> delete(@PathVariable String id) {
        implApplyService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/software-list")
    @Operation(summary = "获取软件名称列表(用于新版本引入)")
    public Result<List<OssSoftware>> listSoftwareForSelect(String swCategory) {
        return Result.success(softwareService.listSoftwareForSelect(swCategory));
    }

    @GetMapping("/check-duplicate")
    @Operation(summary = "检查软件名称和版本是否已存在(用于首次引入校验)")
    public Result<Map<String, Boolean>> checkDuplicate(String swName, String swVersion) {
        boolean exists = implApplyService.checkDuplicate(swName, swVersion);
        return Result.success(Map.of("exists", exists));
    }

    @GetMapping("/check-version-exists")
    @Operation(summary = "检查软件特定版本是否已存在于版本清单表(用于新版本引入校验)")
    public Result<Map<String, Boolean>> checkVersionExists(String swId, String swVersion) {
        log.info("checkVersionExists API called: swId={}, swVersion={}", swId, swVersion);
        boolean exists = implApplyService.checkVersionExistsInBaseline(swId, swVersion);
        log.info("checkVersionExists result: exists={}", exists);
        return Result.success(Map.of("exists", exists));
    }

    @GetMapping("/supplementary/{implApplyNo}")
    @Operation(summary = "获取申请拓展信息")
    public Result<OssImplApplySuplDTO> getSupplementary(@PathVariable String implApplyNo) {
        return Result.success(implApplySuplService.getOrCreateByImplApplyNo(implApplyNo));
    }

    @PutMapping("/supplementary/{implApplyNo}")
    @Operation(summary = "更新申请拓展信息")
    public Result<OssImplApplySuplDTO> updateSupplementary(@PathVariable String implApplyNo, @RequestBody OssImplApplySuplDTO dto) {
        return Result.success("更新成功", implApplySuplService.updateByImplApplyNo(implApplyNo, dto));
    }

    @PostMapping("/start-process")
    @Operation(summary = "启动审批流程")
    public Result<WfInstance> startProcess(@RequestBody Map<String, Object> params) {
        log.info("start-process params: {}", params);
        String implApplyNo = (String) params.get("implApplyNo");
        Object defIdObj = params.get("definitionId");
        log.info("implApplyNo={}, definitionId={}", implApplyNo, defIdObj);
        Long definitionId = Long.valueOf(defIdObj.toString());

        // 获取申请信息
        OssImplApplyDTO applyDTO = implApplyService.getByImplApplyNo(implApplyNo);
        log.info("applyDTO: {}", applyDTO);

        // 创建工作流变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("swName", applyDTO.getSwName());
        variables.put("swVersion", applyDTO.getSwVersion());
        variables.put("swCategory", applyDTO.getSwCategory());
        variables.put("implApplyType", applyDTO.getImplApplyType());
        variables.put("implUserName", applyDTO.getImplUserName());

        // 启动工作流
        WfStartProcessDTO startDTO = new WfStartProcessDTO();
        startDTO.setDefinitionId(definitionId);
        startDTO.setBusinessType("OSS_IMPL_APPLY");
        startDTO.setBusinessId(0L); // 使用businessKey存储实体ID
        startDTO.setBusinessKey(applyDTO.getId()); // 存储实体UUID
        startDTO.setTitle(applyDTO.getFlowTitle());
        startDTO.setVariables(variables);

        WfInstance instance = processEngine.startProcess(startDTO, applyDTO.getImplUserId(), applyDTO.getImplUserName());

        // 更新申请的流程实例ID
        applyDTO.setProcInstId(instance.getId());
        implApplyService.update(applyDTO.getId(), applyDTO);

        return Result.success("流程启动成功", instance);
    }

    @GetMapping("/trace/{implApplyNo}")
    @Operation(summary = "获取审批轨迹")
    public Result<List<Map<String, Object>>> getTrace(@PathVariable String implApplyNo) {
        OssImplApplyDTO applyDTO = implApplyService.getByImplApplyNo(implApplyNo);
        if (applyDTO.getProcInstId() == null) {
            return Result.error("该申请未启动审批流程");
        }
        // TODO: 调用工作流引擎获取审批轨迹
        return Result.success(null);
    }

    @GetMapping("/by-uuid/{uuid}")
    @Operation(summary = "根据UUID获取申请详情(用于工作流查看)")
    public Result<OssImplApplyDTO> getByUuid(@PathVariable String uuid) {
        OssImplApplyDTO dto = implApplyService.getById(uuid);
        return Result.success(dto);
    }
}
