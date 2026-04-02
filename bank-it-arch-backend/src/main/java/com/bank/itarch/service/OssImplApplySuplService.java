package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.mapper.OssImplApplyInfoMapper;
import com.bank.itarch.mapper.OssImplApplySuplMapper;
import com.bank.itarch.model.dto.OssImplApplySuplDTO;
import com.bank.itarch.model.entity.OssImplApplyInfo;
import com.bank.itarch.model.entity.OssImplApplySupl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class OssImplApplySuplService extends ServiceImpl<OssImplApplySuplMapper, OssImplApplySupl> {

    private final OssImplApplyInfoMapper applyInfoMapper;

    public OssImplApplySuplDTO getByImplApplyNo(String implApplyNo) {
        LambdaQueryWrapper<OssImplApplySupl> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssImplApplySupl::getImplApplyNo, implApplyNo);
        OssImplApplySupl supl = getOne(wrapper);
        if (supl == null) {
            throw new BusinessException(2001, "Supplementary info not found");
        }
        return toDTO(supl);
    }

    @Transactional
    public OssImplApplySuplDTO create(OssImplApplySuplDTO dto) {
        OssImplApplySupl supl = toEntity(dto);
        supl.setCreateMode(0);
        save(supl);
        return toDTO(supl);
    }

    @Transactional
    public OssImplApplySuplDTO updateByImplApplyNo(String implApplyNo, OssImplApplySuplDTO dto) {
        LambdaQueryWrapper<OssImplApplySupl> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssImplApplySupl::getImplApplyNo, implApplyNo);
        OssImplApplySupl existing = getOne(wrapper);
        if (existing == null) {
            // 不存在则创建
            existing = new OssImplApplySupl();
            existing.setImplApplyNo(implApplyNo);
            existing.setCreateMode(0);
            updateEntity(existing, dto);
            save(existing);
        } else {
            updateEntity(existing, dto);
            updateById(existing);
        }

        // 同时更新主表 oss_impl_apply_info 的审批补充字段
        updateApplyInfoFields(implApplyNo, dto);

        return toDTO(existing);
    }

    /**
     * 更新 oss_impl_apply_info 表的审批补充字段
     */
    private void updateApplyInfoFields(String implApplyNo, OssImplApplySuplDTO dto) {
        LambdaQueryWrapper<OssImplApplyInfo> infoWrapper = new LambdaQueryWrapper<>();
        infoWrapper.eq(OssImplApplyInfo::getImplApplyNo, implApplyNo);
        OssImplApplyInfo applyInfo = applyInfoMapper.selectOne(infoWrapper);
        if (applyInfo != null) {
            if (dto.getApplicableScene() != null) applyInfo.setApplicableScene(dto.getApplicableScene());
            if (dto.getApplicableFunctionRange() != null) applyInfo.setApplicableFunctionRange(dto.getApplicableFunctionRange());
            if (dto.getIsMainUse() != null) applyInfo.setIsMainUse(dto.getIsMainUse());
            if (dto.getVerType() != null) applyInfo.setVerType(dto.getVerType());
            if (dto.getProductType() != null) applyInfo.setProductType(dto.getProductType());
            if (dto.getApplicationScene() != null) applyInfo.setApplicationScene(dto.getApplicationScene());
            applyInfoMapper.updateById(applyInfo);
        }
    }

    public OssImplApplySuplDTO getOrCreateByImplApplyNo(String implApplyNo) {
        LambdaQueryWrapper<OssImplApplySupl> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssImplApplySupl::getImplApplyNo, implApplyNo);
        OssImplApplySupl supl = getOne(wrapper);
        if (supl == null) {
            supl = new OssImplApplySupl();
            supl.setImplApplyNo(implApplyNo);
            supl.setCreateMode(0);
            save(supl);
        }
        return toDTO(supl);
    }

    private OssImplApplySuplDTO toDTO(OssImplApplySupl supl) {
        OssImplApplySuplDTO dto = new OssImplApplySuplDTO();
        dto.setId(supl.getId());
        dto.setImplApplyNo(supl.getImplApplyNo());
        dto.setEvalInfoListJson(supl.getEvalInfoListJson());
        dto.setEvalResultListJson(supl.getEvalResultListJson());
        dto.setEvalScoreListJson(supl.getEvalScoreListJson());
        dto.setEvalSummListJson(supl.getEvalSummListJson());
        dto.setEvalAtchListJson(supl.getEvalAtchListJson());
        dto.setMediaPreWhsUrl(supl.getMediaPreWhsUrl());
        dto.setProcInstId(supl.getProcInstId());
        dto.setSyncDatetime(supl.getSyncDatetime());
        dto.setCreateMode(supl.getCreateMode());
        dto.setCreateUserId(supl.getCreateUserId());
        dto.setCreateDatetime(supl.getCreateDatetime());
        dto.setUpdateUserId(supl.getUpdateUserId());
        dto.setUpdateDatetime(supl.getUpdateDatetime());

        // 从主表获取审批补充字段
        fillApprovalFieldsFromMainTable(dto, supl.getImplApplyNo());

        return dto;
    }

    /**
     * 从主表获取审批补充字段
     */
    private void fillApprovalFieldsFromMainTable(OssImplApplySuplDTO dto, String implApplyNo) {
        if (implApplyNo == null) return;
        try {
            LambdaQueryWrapper<OssImplApplyInfo> infoWrapper = new LambdaQueryWrapper<>();
            infoWrapper.eq(OssImplApplyInfo::getImplApplyNo, implApplyNo);
            OssImplApplyInfo applyInfo = applyInfoMapper.selectOne(infoWrapper);
            if (applyInfo != null) {
                dto.setApplicableScene(applyInfo.getApplicableScene());
                dto.setApplicableFunctionRange(applyInfo.getApplicableFunctionRange());
                dto.setIsMainUse(applyInfo.getIsMainUse());
                dto.setVerType(applyInfo.getVerType());
                dto.setProductType(applyInfo.getProductType());
                dto.setApplicationScene(applyInfo.getApplicationScene());
            }
        } catch (Exception e) {
            // Ignore if not found
        }
    }

    private OssImplApplySupl toEntity(OssImplApplySuplDTO dto) {
        OssImplApplySupl supl = new OssImplApplySupl();
        supl.setImplApplyNo(dto.getImplApplyNo());
        supl.setEvalInfoListJson(dto.getEvalInfoListJson());
        supl.setEvalResultListJson(dto.getEvalResultListJson());
        supl.setEvalScoreListJson(dto.getEvalScoreListJson());
        supl.setEvalSummListJson(dto.getEvalSummListJson());
        supl.setEvalAtchListJson(dto.getEvalAtchListJson());
        supl.setMediaPreWhsUrl(dto.getMediaPreWhsUrl());
        supl.setProcInstId(dto.getProcInstId());
        return supl;
    }

    private void updateEntity(OssImplApplySupl supl, OssImplApplySuplDTO dto) {
        if (dto.getEvalInfoListJson() != null) supl.setEvalInfoListJson(dto.getEvalInfoListJson());
        if (dto.getEvalResultListJson() != null) supl.setEvalResultListJson(dto.getEvalResultListJson());
        if (dto.getEvalScoreListJson() != null) supl.setEvalScoreListJson(dto.getEvalScoreListJson());
        if (dto.getEvalSummListJson() != null) supl.setEvalSummListJson(dto.getEvalSummListJson());
        if (dto.getEvalAtchListJson() != null) supl.setEvalAtchListJson(dto.getEvalAtchListJson());
        if (dto.getMediaPreWhsUrl() != null) supl.setMediaPreWhsUrl(dto.getMediaPreWhsUrl());
        if (dto.getProcInstId() != null) supl.setProcInstId(dto.getProcInstId());
    }
}
