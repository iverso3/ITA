package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.OssUseStandingBookDetailsMapper;
import com.bank.itarch.model.dto.OssUseStandingBookDetailsDTO;
import com.bank.itarch.model.entity.OssUseStandingBookDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OssUseStandingBookDetailsService extends ServiceImpl<OssUseStandingBookDetailsMapper, OssUseStandingBookDetails> {

    public PageResult<OssUseStandingBookDetailsDTO> pageQueryByParentId(String parentId, Integer page, Integer pageSize) {
        Page<OssUseStandingBookDetails> pageParam = new Page<>(page, pageSize);

        LambdaQueryWrapper<OssUseStandingBookDetails> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssUseStandingBookDetails::getParentId, parentId)
               .orderByDesc(OssUseStandingBookDetails::getCreateDatetime);

        IPage<OssUseStandingBookDetails> result = page(pageParam, wrapper);

        List<OssUseStandingBookDetailsDTO> dtoList = result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, result.getTotal(), page, pageSize);
    }

    public OssUseStandingBookDetailsDTO getById(String id) {
        OssUseStandingBookDetails detail = super.getById(id);
        if (detail == null) {
            throw new BusinessException(2001, "Detail record not found");
        }
        return toDTO(detail);
    }

    @Transactional
    public OssUseStandingBookDetailsDTO create(OssUseStandingBookDetailsDTO dto) {
        OssUseStandingBookDetails detail = toEntity(dto);
        detail.setCreateMode(0);
        save(detail);
        return toDTO(detail);
    }

    @Transactional
    public OssUseStandingBookDetailsDTO update(String id, OssUseStandingBookDetailsDTO dto) {
        OssUseStandingBookDetails existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Detail record not found");
        }
        updateEntity(existing, dto);
        updateById(existing);
        return toDTO(existing);
    }

    @Transactional
    public void delete(String id) {
        OssUseStandingBookDetails existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Detail record not found");
        }
        removeById(id);
    }

    private OssUseStandingBookDetailsDTO toDTO(OssUseStandingBookDetails detail) {
        OssUseStandingBookDetailsDTO dto = new OssUseStandingBookDetailsDTO();
        dto.setId(detail.getId());
        dto.setSwName(detail.getSwName());
        dto.setSwVersion(detail.getSwVersion());
        dto.setSwFullName(detail.getSwFullName());
        dto.setLicAbbr(detail.getLicAbbr());
        dto.setEnvironment(detail.getEnvironment());
        dto.setScanTime(detail.getScanTime());
        dto.setSwCategory(detail.getSwCategory());
        dto.setAppNo(detail.getAppNo());
        dto.setAppName(detail.getAppName());
        dto.setParentId(detail.getParentId());
        dto.setInstallPath(detail.getInstallPath());
        dto.setSource(detail.getSource());
        dto.setIpOrHostName(detail.getIpOrHostName());
        dto.setCommand(detail.getCommand());
        dto.setDetailedInfo(detail.getDetailedInfo());
        dto.setFileType(detail.getFileType());
        dto.setDependType(detail.getDependType());
        dto.setSyncDatetime(detail.getSyncDatetime());
        dto.setCreateMode(detail.getCreateMode());
        dto.setCreateUserId(detail.getCreateUserId());
        dto.setCreateDatetime(detail.getCreateDatetime());
        dto.setUpdateUserId(detail.getUpdateUserId());
        dto.setUpdateDatetime(detail.getUpdateDatetime());
        dto.setIsCommerc(detail.getIsCommerc());
        dto.setCommercProductName(detail.getCommercProductName());
        dto.setCommercProductVersion(detail.getCommercProductVersion());
        dto.setProjectName(detail.getProjectName());
        return dto;
    }

    private OssUseStandingBookDetails toEntity(OssUseStandingBookDetailsDTO dto) {
        OssUseStandingBookDetails detail = new OssUseStandingBookDetails();
        detail.setSwName(dto.getSwName());
        detail.setSwVersion(dto.getSwVersion());
        detail.setSwFullName(dto.getSwFullName());
        detail.setLicAbbr(dto.getLicAbbr());
        detail.setEnvironment(dto.getEnvironment());
        detail.setScanTime(dto.getScanTime());
        detail.setSwCategory(dto.getSwCategory());
        detail.setAppNo(dto.getAppNo());
        detail.setAppName(dto.getAppName());
        detail.setParentId(dto.getParentId());
        detail.setInstallPath(dto.getInstallPath());
        detail.setSource(dto.getSource());
        detail.setIpOrHostName(dto.getIpOrHostName());
        detail.setCommand(dto.getCommand());
        detail.setDetailedInfo(dto.getDetailedInfo());
        detail.setFileType(dto.getFileType());
        detail.setDependType(dto.getDependType());
        detail.setSyncDatetime(dto.getSyncDatetime());
        detail.setCreateUserId(dto.getCreateUserId());
        detail.setUpdateUserId(dto.getUpdateUserId());
        detail.setIsCommerc(dto.getIsCommerc());
        detail.setCommercProductName(dto.getCommercProductName());
        detail.setCommercProductVersion(dto.getCommercProductVersion());
        detail.setProjectName(dto.getProjectName());
        return detail;
    }

    private void updateEntity(OssUseStandingBookDetails detail, OssUseStandingBookDetailsDTO dto) {
        if (dto.getSwName() != null) detail.setSwName(dto.getSwName());
        if (dto.getSwVersion() != null) detail.setSwVersion(dto.getSwVersion());
        if (dto.getSwFullName() != null) detail.setSwFullName(dto.getSwFullName());
        if (dto.getLicAbbr() != null) detail.setLicAbbr(dto.getLicAbbr());
        if (dto.getEnvironment() != null) detail.setEnvironment(dto.getEnvironment());
        if (dto.getScanTime() != null) detail.setScanTime(dto.getScanTime());
        if (dto.getSwCategory() != null) detail.setSwCategory(dto.getSwCategory());
        if (dto.getAppNo() != null) detail.setAppNo(dto.getAppNo());
        if (dto.getAppName() != null) detail.setAppName(dto.getAppName());
        if (dto.getInstallPath() != null) detail.setInstallPath(dto.getInstallPath());
        if (dto.getSource() != null) detail.setSource(dto.getSource());
        if (dto.getIpOrHostName() != null) detail.setIpOrHostName(dto.getIpOrHostName());
        if (dto.getCommand() != null) detail.setCommand(dto.getCommand());
        if (dto.getDetailedInfo() != null) detail.setDetailedInfo(dto.getDetailedInfo());
        if (dto.getFileType() != null) detail.setFileType(dto.getFileType());
        if (dto.getDependType() != null) detail.setDependType(dto.getDependType());
        if (dto.getSyncDatetime() != null) detail.setSyncDatetime(dto.getSyncDatetime());
        if (dto.getUpdateUserId() != null) detail.setUpdateUserId(dto.getUpdateUserId());
        if (dto.getIsCommerc() != null) detail.setIsCommerc(dto.getIsCommerc());
        if (dto.getCommercProductName() != null) detail.setCommercProductName(dto.getCommercProductName());
        if (dto.getCommercProductVersion() != null) detail.setCommercProductVersion(dto.getCommercProductVersion());
        if (dto.getProjectName() != null) detail.setProjectName(dto.getProjectName());
    }
}