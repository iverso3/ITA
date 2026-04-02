package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.OssImplApplyInfoMapper;
import com.bank.itarch.mapper.OssSoftwareBaselineMapper;
import com.bank.itarch.mapper.OssSoftwareMapper;
import com.bank.itarch.model.dto.OssImplApplyDTO;
import com.bank.itarch.model.dto.OssImplApplyQueryDTO;
import com.bank.itarch.model.dto.OssImplApplySuplDTO;
import com.bank.itarch.model.entity.OssImplApplyInfo;
import com.bank.itarch.model.entity.OssSoftware;
import com.bank.itarch.model.entity.OssSoftwareBaseline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssImplApplyService extends ServiceImpl<OssImplApplyInfoMapper, OssImplApplyInfo> {

    private final OssSoftwareMapper softwareMapper;
    private final OssSoftwareBaselineMapper baselineMapper;
    private final OssImplApplySuplService suplService;

    public PageResult<OssImplApplyDTO> pageQuery(OssImplApplyQueryDTO query) {
        Page<OssImplApplyInfo> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<OssImplApplyInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getImplApplyNo()), OssImplApplyInfo::getImplApplyNo, query.getImplApplyNo())
               .eq(StringUtils.hasText(query.getImplApplyType()), OssImplApplyInfo::getImplApplyType, query.getImplApplyType())
               .eq(StringUtils.hasText(query.getSwCategory()), OssImplApplyInfo::getSwCategory, query.getSwCategory())
               .like(StringUtils.hasText(query.getSwName()), OssImplApplyInfo::getSwName, query.getSwName())
               .eq(StringUtils.hasText(query.getSwId()), OssImplApplyInfo::getSwId, query.getSwId())
               .eq(query.getProcInstId() != null, OssImplApplyInfo::getProcInstId, query.getProcInstId())
               .eq(StringUtils.hasText(query.getImplUserId()), OssImplApplyInfo::getImplUserId, query.getImplUserId())
               .eq(StringUtils.hasText(query.getImplTeamId()), OssImplApplyInfo::getImplTeamId, query.getImplTeamId())
               .orderByDesc(OssImplApplyInfo::getCreateDatetime);

        IPage<OssImplApplyInfo> result = page(page, wrapper);

        List<OssImplApplyDTO> dtoList = result.getRecords().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());

        return PageResult.of(dtoList, result.getTotal(), query.getPage(), query.getPageSize());
    }

    public OssImplApplyDTO getById(String id) {
        OssImplApplyInfo info = super.getById(id);
        if (info == null) {
            throw new BusinessException(2001, "Application not found");
        }
        OssImplApplyDTO dto = toDTO(info);
        // 填充评测数据
        fillEvalData(dto, info.getImplApplyNo());
        return dto;
    }

    public OssImplApplyDTO getByImplApplyNo(String implApplyNo) {
        LambdaQueryWrapper<OssImplApplyInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssImplApplyInfo::getImplApplyNo, implApplyNo);
        OssImplApplyInfo info = getOne(wrapper);
        if (info == null) {
            throw new BusinessException(2001, "Application not found");
        }
        OssImplApplyDTO dto = toDTO(info);
        // 填充评测数据
        fillEvalData(dto, implApplyNo);
        return dto;
    }

    private void fillEvalData(OssImplApplyDTO dto, String implApplyNo) {
        if (implApplyNo == null) return;
        try {
            OssImplApplySuplDTO supl = suplService.getOrCreateByImplApplyNo(implApplyNo);
            if (supl.getEvalResultListJson() != null) {
                dto.setEvalResultListJson(supl.getEvalResultListJson());
            }
            if (supl.getEvalScoreListJson() != null) {
                dto.setEvalIndicatorsJson(supl.getEvalScoreListJson());
            }
        } catch (Exception e) {
            // Ignore if supplementary data not found
        }
    }

    @Transactional
    public OssImplApplyDTO create(OssImplApplyDTO dto) {
        OssImplApplyInfo info = toEntity(dto);
        info.setCreateMode(0);
        // 生成申请单号
        String applyNo = generateApplyNo();
        info.setImplApplyNo(applyNo);
        info.setFlowTitle(dto.getSwName() + " - " + dto.getSwVersion() + " - 引入申请");
        save(info);
        return toDTO(info);
    }

    @Transactional
    public OssImplApplyDTO update(String id, OssImplApplyDTO dto) {
        OssImplApplyInfo existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Application not found");
        }
        updateEntity(existing, dto);
        updateById(existing);
        return toDTO(existing);
    }

    @Transactional
    public void delete(String id) {
        OssImplApplyInfo existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Application not found");
        }
        removeById(id);
    }

    public List<OssSoftware> listSoftwareForSelect(String swCategory) {
        LambdaQueryWrapper<OssSoftware> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(swCategory), OssSoftware::getSwCategory, swCategory);
        wrapper.orderByDesc(OssSoftware::getCreateDatetime);
        return softwareMapper.selectList(wrapper);
    }

    /**
     * 检查软件名称和版本是否已存在（用于首次引入时的重复校验）
     * 检查范围：1）版本清单表(oss_software_baseline) 2）软件信息表(oss_software_info)
     * @param swName 软件名称
     * @param swVersion 软件版本
     * @return true 表示已存在，false 表示不存在
     */
    public boolean checkDuplicate(String swName, String swVersion) {
        // 1. 检查版本清单表 - 如果同名软件的该版本已存在，则重复
        LambdaQueryWrapper<OssSoftwareBaseline> baselineWrapper = new LambdaQueryWrapper<>();
        baselineWrapper.eq(OssSoftwareBaseline::getSwName, swName)
               .eq(OssSoftwareBaseline::getSwVersion, swVersion)
               .eq(OssSoftwareBaseline::getLogicStatus, 0);
        if (baselineMapper.selectCount(baselineWrapper) > 0) {
            return true;
        }
        // 2. 检查软件信息表 - 如果软件名称已存在，则该软件不是首次引入，而是新版本引入
        LambdaQueryWrapper<OssSoftware> softwareWrapper = new LambdaQueryWrapper<>();
        softwareWrapper.eq(OssSoftware::getSwName, swName)
               .eq(OssSoftware::getLogicStatus, 0);
        return softwareMapper.selectCount(softwareWrapper) > 0;
    }

    /**
     * 检查软件特定版本是否已存在于版本清单表中（用于新版本引入时的重复校验）
     * @param swId 软件ID
     * @param swVersion 软件版本
     * @return true 表示该版本已存在，false 表示不存在
     */
    public boolean checkVersionExistsInBaseline(String swId, String swVersion) {
        log.info("checkVersionExistsInBaseline: swId={}, swVersion={}", swId, swVersion);
        LambdaQueryWrapper<OssSoftwareBaseline> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssSoftwareBaseline::getSwId, swId)
               .eq(OssSoftwareBaseline::getSwVersion, swVersion)
               .eq(OssSoftwareBaseline::getLogicStatus, 0);
        long count = baselineMapper.selectCount(wrapper);
        log.info("checkVersionExistsInBaseline: count={}", count);
        return count > 0;
    }

    /**
     * 审批通过后同步数据到软件表和版本清单表
     * @param implApplyNo 申请单号
     */
    @Transactional
    public void syncToSoftware(String implApplyNo) {
        log.info("syncToSoftware start: implApplyNo={}", implApplyNo);
        if (implApplyNo == null || implApplyNo.trim().isEmpty()) {
            log.error("syncToSoftware失败: implApplyNo为空");
            throw new BusinessException(2001, "implApplyNo不能为空");
        }
        OssImplApplyDTO applyDTO = getByImplApplyNo(implApplyNo);
        if (applyDTO == null) {
            log.error("syncToSoftware失败: 未找到申请记录 implApplyNo={}", implApplyNo);
            throw new BusinessException(2001, "Application not found: " + implApplyNo);
        }
        log.info("syncToSoftware: implApplyType={}, swId={}, swName={}, swVersion={}",
                applyDTO.getImplApplyType(), applyDTO.getSwId(), applyDTO.getSwName(), applyDTO.getSwVersion());

        // 首次引入：同步到软件表和版本清单表
        if ("0".equals(applyDTO.getImplApplyType())) {
            syncToSoftwareInfo(applyDTO);
            log.info("syncToSoftware: first-time introduction, software info synced, swId={}", applyDTO.getSwId());
        }

        // 同步到版本清单表（首次引入和新版本引入都需要）
        syncToBaseline(applyDTO);
        log.info("syncToSoftware end: implApplyNo={}", implApplyNo);
    }

    /**
     * 同步数据到软件信息表（仅首次引入）
     */
    private void syncToSoftwareInfo(OssImplApplyDTO applyDTO) {
        LambdaQueryWrapper<OssSoftware> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssSoftware::getSwName, applyDTO.getSwName())
               .eq(OssSoftware::getLogicStatus, 0);
        OssSoftware existingSoftware = softwareMapper.selectOne(wrapper);

        if (existingSoftware == null) {
            // 创建新的软件记录
            OssSoftware software = new OssSoftware();
            software.setSwName(applyDTO.getSwName());
            software.setSwCategory(applyDTO.getSwCategory());
            // 如果是主推荐使用版本，则软件类型设为主推荐版本
            software.setSwType("1".equals(applyDTO.getIsMainUse()) ? "MAIN" : applyDTO.getSwType());
            // 责任团队信息（数据库NOT NULL字段，使用空字符串作为默认值）
            software.setRspTeamId(applyDTO.getRspTeamId() != null ? applyDTO.getRspTeamId() : "");
            software.setRspTeamName(applyDTO.getRspTeamName() != null ? applyDTO.getRspTeamName() : "");
            software.setRspUserId(applyDTO.getRspUserId() != null ? applyDTO.getRspUserId() : "");
            software.setRspUserName(applyDTO.getRspUserName() != null ? applyDTO.getRspUserName() : "");
            software.setProductType(applyDTO.getProductType());
            software.setApplicationScene(applyDTO.getApplicationScene());
            software.setDataSrcFlag("0"); // 人工引入
            software.setCreateMode(0);
            softwareMapper.insert(software);

            // 更新申请记录的软件ID
            applyDTO.setSwId(software.getId());
            OssImplApplyInfo applyInfo = baseMapper.selectById(applyDTO.getId());
            if (applyInfo != null) {
                applyInfo.setSwId(software.getId());
                baseMapper.updateById(applyInfo);
            }
        } else {
            // 更新现有软件记录的相关字段
            existingSoftware.setSwType(applyDTO.getSwType());
            existingSoftware.setRspTeamId(applyDTO.getRspTeamId() != null ? applyDTO.getRspTeamId() : "");
            existingSoftware.setRspTeamName(applyDTO.getRspTeamName() != null ? applyDTO.getRspTeamName() : "");
            existingSoftware.setRspUserId(applyDTO.getRspUserId() != null ? applyDTO.getRspUserId() : "");
            existingSoftware.setRspUserName(applyDTO.getRspUserName() != null ? applyDTO.getRspUserName() : "");
            existingSoftware.setProductType(applyDTO.getProductType());
            existingSoftware.setApplicationScene(applyDTO.getApplicationScene());
            softwareMapper.updateById(existingSoftware);

            // 更新申请记录的软件ID
            applyDTO.setSwId(existingSoftware.getId());
            OssImplApplyInfo applyInfo = baseMapper.selectById(applyDTO.getId());
            if (applyInfo != null) {
                applyInfo.setSwId(existingSoftware.getId());
                baseMapper.updateById(applyInfo);
            }
        }
    }

    /**
     * 同步数据到版本清单表（首次引入和新版本引入都需要）
     */
    private void syncToBaseline(OssImplApplyDTO applyDTO) {
        log.info("syncToBaseline start: swId={}, swName={}, swVersion={}, implTeamId={}, implUserId={}",
                applyDTO.getSwId(), applyDTO.getSwName(), applyDTO.getSwVersion(), applyDTO.getImplTeamId(), applyDTO.getImplUserId());

        // 检查该版本是否已存在于版本清单表，避免重复插入
        if (applyDTO.getSwId() != null && applyDTO.getSwVersion() != null) {
            LambdaQueryWrapper<OssSoftwareBaseline> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(OssSoftwareBaseline::getSwId, applyDTO.getSwId())
                   .eq(OssSoftwareBaseline::getSwVersion, applyDTO.getSwVersion())
                   .eq(OssSoftwareBaseline::getLogicStatus, 0);
            long existingCount = baselineMapper.selectCount(checkWrapper);
            if (existingCount > 0) {
                log.warn("syncToBaseline: version already exists, skip insert. swId={}, swVersion={}", applyDTO.getSwId(), applyDTO.getSwVersion());
                return;
            }
        }

        OssSoftwareBaseline baseline = new OssSoftwareBaseline();
        baseline.setSwId(applyDTO.getSwId() != null ? applyDTO.getSwId() : "");
        baseline.setSwName(applyDTO.getSwName());
        baseline.setSwVersion(applyDTO.getSwVersion());
        baseline.setSwCategory(applyDTO.getSwCategory());
        // 如果是主推荐使用版本，则软件类型设为主推荐版本
        baseline.setSwType("1".equals(applyDTO.getIsMainUse()) ? "MAIN" : applyDTO.getSwType());
        baseline.setVerType(applyDTO.getVerType());
        // 实施团队信息（数据库NOT NULL字段，使用空字符串作为默认值）
        baseline.setImplTeamId(applyDTO.getImplTeamId() != null ? applyDTO.getImplTeamId() : "");
        baseline.setImplTeamName(applyDTO.getImplTeamName() != null ? applyDTO.getImplTeamName() : "");
        baseline.setImplUserId(applyDTO.getImplUserId() != null ? applyDTO.getImplUserId() : "");
        baseline.setImplUserName(applyDTO.getImplUserName() != null ? applyDTO.getImplUserName() : "");
        baseline.setImplApplyNo(applyDTO.getImplApplyNo());
        baseline.setLicId(applyDTO.getLicId());
        baseline.setLicAbbr(applyDTO.getLicAbbr());
        baseline.setIsMainUse(applyDTO.getIsMainUse());
        baseline.setApplicableScene(applyDTO.getApplicableScene());
        baseline.setApplicableFunctionRange(applyDTO.getApplicableFunctionRange());
        baseline.setUseBranchId(applyDTO.getUseBranchId() != null ? applyDTO.getUseBranchId() : "");
        baseline.setUseBranchName(applyDTO.getUseBranchName() != null ? applyDTO.getUseBranchName() : "");
        baseline.setDataSrcFlag("0"); // 人工引入
        baseline.setCreateMode(0);
        baselineMapper.insert(baseline);
        log.info("syncToBaseline end: inserted baseline id={}", baseline.getId());
    }

    private String generateApplyNo() {
        String date = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = System.currentTimeMillis() % 100000;
        return "OSS" + date + String.format("%05d", seq);
    }

    private OssImplApplyDTO toDTO(OssImplApplyInfo info) {
        OssImplApplyDTO dto = new OssImplApplyDTO();
        dto.setId(info.getId());
        dto.setImplApplyNo(info.getImplApplyNo());
        dto.setFlowTitle(info.getFlowTitle());
        dto.setImplApplyType(info.getImplApplyType());
        dto.setSwId(info.getSwId());
        dto.setSwName(info.getSwName());
        dto.setSwVersion(info.getSwVersion());
        dto.setSwType(info.getSwType());
        dto.setRspUserId(info.getRspUserId());
        dto.setRspUserName(info.getRspUserName());
        dto.setRspTeamId(info.getRspTeamId());
        dto.setRspTeamName(info.getRspTeamName());
        dto.setSwCategory(info.getSwCategory());
        dto.setImplTeamId(info.getImplTeamId());
        dto.setImplTeamName(info.getImplTeamName());
        dto.setImplUserId(info.getImplUserId());
        dto.setImplUserName(info.getImplUserName());
        dto.setLicId(info.getLicId());
        dto.setLicAbbr(info.getLicAbbr());
        dto.setUseBranchId(info.getUseBranchId());
        dto.setUseBranchName(info.getUseBranchName());
        dto.setSecInstrt(info.getSecInstrt());
        dto.setOsType(info.getOsType());
        dto.setOsVersion(info.getOsVersion());
        dto.setOsDigit(info.getOsDigit());
        dto.setApplyTeamId(info.getApplyTeamId());
        dto.setApplyTeamName(info.getApplyTeamName());
        dto.setUseAppNo(info.getUseAppNo());
        dto.setLaunchVersion(info.getLaunchVersion());
        dto.setLaunchTaskInfo(info.getLaunchTaskInfo());
        dto.setImplCmnt(info.getImplCmnt());
        dto.setContactUserId(info.getContactUserId());
        dto.setContactUserName(info.getContactUserName());
        dto.setContactTelNo(info.getContactTelNo());
        dto.setEvalBackground(info.getEvalBackground());
        dto.setSystemEnv(info.getSystemEnv());
        dto.setFunctionIntro(info.getFunctionIntro());
        dto.setEvalConclusion(info.getEvalConclusion());
        dto.setApproveDatetime(info.getApproveDatetime());
        dto.setProcInstId(info.getProcInstId());
        dto.setRemark(info.getRemark());
        dto.setSyncDatetime(info.getSyncDatetime());
        dto.setCreateMode(info.getCreateMode());
        dto.setCreateUserId(info.getCreateUserId());
        dto.setCreateDatetime(info.getCreateDatetime());
        dto.setUpdateUserId(info.getUpdateUserId());
        dto.setUpdateDatetime(info.getUpdateDatetime());
        // 审批补充字段
        dto.setApplicableScene(info.getApplicableScene());
        dto.setApplicableFunctionRange(info.getApplicableFunctionRange());
        dto.setIsMainUse(info.getIsMainUse());
        dto.setVerType(info.getVerType());
        dto.setProductType(info.getProductType());
        dto.setApplicationScene(info.getApplicationScene());
        return dto;
    }

    private OssImplApplyInfo toEntity(OssImplApplyDTO dto) {
        OssImplApplyInfo info = new OssImplApplyInfo();
        info.setImplApplyType(dto.getImplApplyType());
        info.setSwId(dto.getSwId());
        info.setSwName(dto.getSwName());
        info.setSwVersion(dto.getSwVersion());
        info.setSwType(dto.getSwType());
        info.setRspUserId(dto.getRspUserId());
        info.setRspUserName(dto.getRspUserName());
        info.setRspTeamId(dto.getRspTeamId());
        info.setRspTeamName(dto.getRspTeamName());
        info.setSwCategory(dto.getSwCategory());
        info.setImplTeamId(dto.getImplTeamId());
        info.setImplTeamName(dto.getImplTeamName());
        info.setImplUserId(dto.getImplUserId());
        info.setImplUserName(dto.getImplUserName());
        info.setLicId(dto.getLicId());
        info.setLicAbbr(dto.getLicAbbr());
        info.setUseBranchId(dto.getUseBranchId());
        info.setUseBranchName(dto.getUseBranchName());
        info.setSecInstrt(dto.getSecInstrt());
        info.setOsType(dto.getOsType());
        info.setOsVersion(dto.getOsVersion());
        info.setOsDigit(dto.getOsDigit());
        info.setApplyTeamId(dto.getApplyTeamId());
        info.setApplyTeamName(dto.getApplyTeamName());
        info.setUseAppNo(dto.getUseAppNo());
        info.setLaunchVersion(dto.getLaunchVersion());
        info.setLaunchTaskInfo(dto.getLaunchTaskInfo());
        info.setImplCmnt(dto.getImplCmnt());
        info.setContactUserId(dto.getContactUserId());
        info.setContactUserName(dto.getContactUserName());
        info.setContactTelNo(dto.getContactTelNo());
        info.setEvalBackground(dto.getEvalBackground());
        info.setSystemEnv(dto.getSystemEnv());
        info.setFunctionIntro(dto.getFunctionIntro());
        info.setEvalConclusion(dto.getEvalConclusion());
        info.setRemark(dto.getRemark());
        return info;
    }

    private void updateEntity(OssImplApplyInfo info, OssImplApplyDTO dto) {
        if (dto.getSwId() != null) info.setSwId(dto.getSwId());
        if (dto.getSwName() != null) info.setSwName(dto.getSwName());
        if (dto.getSwVersion() != null) info.setSwVersion(dto.getSwVersion());
        if (dto.getSwType() != null) info.setSwType(dto.getSwType());
        if (dto.getSwCategory() != null) info.setSwCategory(dto.getSwCategory());
        if (dto.getImplTeamId() != null) info.setImplTeamId(dto.getImplTeamId());
        if (dto.getImplTeamName() != null) info.setImplTeamName(dto.getImplTeamName());
        if (dto.getImplUserId() != null) info.setImplUserId(dto.getImplUserId());
        if (dto.getImplUserName() != null) info.setImplUserName(dto.getImplUserName());
        if (dto.getLicId() != null) info.setLicId(dto.getLicId());
        if (dto.getLicAbbr() != null) info.setLicAbbr(dto.getLicAbbr());
        if (dto.getEvalBackground() != null) info.setEvalBackground(dto.getEvalBackground());
        if (dto.getSystemEnv() != null) info.setSystemEnv(dto.getSystemEnv());
        if (dto.getFunctionIntro() != null) info.setFunctionIntro(dto.getFunctionIntro());
        if (dto.getEvalConclusion() != null) info.setEvalConclusion(dto.getEvalConclusion());
        if (dto.getApproveDatetime() != null) info.setApproveDatetime(dto.getApproveDatetime());
        if (dto.getProcInstId() != null) info.setProcInstId(dto.getProcInstId());
        if (dto.getRemark() != null) info.setRemark(dto.getRemark());
        // 审批补充字段
        if (dto.getApplicableScene() != null) info.setApplicableScene(dto.getApplicableScene());
        if (dto.getApplicableFunctionRange() != null) info.setApplicableFunctionRange(dto.getApplicableFunctionRange());
        if (dto.getIsMainUse() != null) info.setIsMainUse(dto.getIsMainUse());
        if (dto.getVerType() != null) info.setVerType(dto.getVerType());
        if (dto.getProductType() != null) info.setProductType(dto.getProductType());
        if (dto.getApplicationScene() != null) info.setApplicationScene(dto.getApplicationScene());
    }
}
