package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.OssSoftwareMapper;
import com.bank.itarch.model.dto.OssSoftwareDTO;
import com.bank.itarch.model.dto.OssSoftwareQueryDTO;
import com.bank.itarch.model.entity.OssSoftware;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OssSoftwareService extends ServiceImpl<OssSoftwareMapper, OssSoftware> {

    public PageResult<OssSoftwareDTO> pageQuery(OssSoftwareQueryDTO query) {
        Page<OssSoftware> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<OssSoftware> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getSwName()), OssSoftware::getSwName, query.getSwName())
               .eq(StringUtils.hasText(query.getSwCategory()), OssSoftware::getSwCategory, query.getSwCategory())
               .eq(StringUtils.hasText(query.getSwType()), OssSoftware::getSwType, query.getSwType())
               .eq(StringUtils.hasText(query.getRspTeamId()), OssSoftware::getRspTeamId, query.getRspTeamId())
               .like(StringUtils.hasText(query.getRspUserName()), OssSoftware::getRspUserName, query.getRspUserName())
               .orderByDesc(OssSoftware::getCreateDatetime);

        IPage<OssSoftware> result = page(page, wrapper);

        List<OssSoftwareDTO> dtoList = result.getRecords().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());

        return PageResult.of(dtoList, result.getTotal(), query.getPage(), query.getPageSize());
    }

    public OssSoftwareDTO getById(String id) {
        OssSoftware software = super.getById(id);
        if (software == null) {
            throw new BusinessException(2001, "Software not found");
        }
        return toDTO(software);
    }

    @Transactional
    public OssSoftwareDTO create(OssSoftwareDTO dto) {
        OssSoftware software = toEntity(dto);
        software.setCreateMode(0);
        save(software);
        return toDTO(software);
    }

    @Transactional
    public OssSoftwareDTO update(String id, OssSoftwareDTO dto) {
        OssSoftware existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Software not found");
        }
        updateEntity(existing, dto);
        updateById(existing);
        return toDTO(existing);
    }

    @Transactional
    public void delete(String id) {
        OssSoftware existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Software not found");
        }
        removeById(id);
    }

    public List<OssSoftwareDTO> listAllForExport(OssSoftwareQueryDTO query) {
        LambdaQueryWrapper<OssSoftware> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getSwName()), OssSoftware::getSwName, query.getSwName())
               .eq(StringUtils.hasText(query.getSwCategory()), OssSoftware::getSwCategory, query.getSwCategory())
               .eq(StringUtils.hasText(query.getSwType()), OssSoftware::getSwType, query.getSwType())
               .eq(StringUtils.hasText(query.getRspTeamId()), OssSoftware::getRspTeamId, query.getRspTeamId())
               .like(StringUtils.hasText(query.getRspUserName()), OssSoftware::getRspUserName, query.getRspUserName())
               .orderByDesc(OssSoftware::getCreateDatetime);

        List<OssSoftware> list = list(wrapper);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<OssSoftware> listSoftwareForSelect(String swCategory) {
        LambdaQueryWrapper<OssSoftware> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(swCategory), OssSoftware::getSwCategory, swCategory);
        wrapper.orderByDesc(OssSoftware::getCreateDatetime);
        return list(wrapper);
    }

    private OssSoftwareDTO toDTO(OssSoftware software) {
        OssSoftwareDTO dto = new OssSoftwareDTO();
        dto.setId(software.getId());
        dto.setSwName(software.getSwName());
        dto.setSwCategory(software.getSwCategory());
        dto.setSwType(software.getSwType());
        dto.setRspTeamId(software.getRspTeamId());
        dto.setRspTeamName(software.getRspTeamName());
        dto.setRspUserId(software.getRspUserId());
        dto.setRspUserName(software.getRspUserName());
        dto.setProductType(software.getProductType());
        dto.setRecommendedVersion(software.getRecommendedVersion());
        dto.setApplicationScene(software.getApplicationScene());
        dto.setDataSrcFlag(software.getDataSrcFlag());
        dto.setSyncDatetime(software.getSyncDatetime());
        dto.setBatchDate(software.getBatchDate());
        dto.setCreateMode(software.getCreateMode());
        dto.setCreateUserId(software.getCreateUserId());
        dto.setCreateDatetime(software.getCreateDatetime());
        dto.setUpdateUserId(software.getUpdateUserId());
        dto.setUpdateDatetime(software.getUpdateDatetime());
        return dto;
    }

    private OssSoftware toEntity(OssSoftwareDTO dto) {
        OssSoftware software = new OssSoftware();
        software.setSwName(dto.getSwName());
        software.setSwCategory(dto.getSwCategory());
        software.setSwType(dto.getSwType());
        software.setRspTeamId(dto.getRspTeamId());
        software.setRspTeamName(dto.getRspTeamName());
        software.setRspUserId(dto.getRspUserId());
        software.setRspUserName(dto.getRspUserName());
        software.setProductType(dto.getProductType());
        software.setRecommendedVersion(dto.getRecommendedVersion());
        software.setApplicationScene(dto.getApplicationScene());
        software.setDataSrcFlag(dto.getDataSrcFlag());
        software.setSyncDatetime(dto.getSyncDatetime());
        software.setBatchDate(dto.getBatchDate());
        return software;
    }

    private void updateEntity(OssSoftware software, OssSoftwareDTO dto) {
        if (dto.getSwName() != null) software.setSwName(dto.getSwName());
        if (dto.getSwCategory() != null) software.setSwCategory(dto.getSwCategory());
        if (dto.getSwType() != null) software.setSwType(dto.getSwType());
        if (dto.getRspTeamId() != null) software.setRspTeamId(dto.getRspTeamId());
        if (dto.getRspTeamName() != null) software.setRspTeamName(dto.getRspTeamName());
        if (dto.getRspUserId() != null) software.setRspUserId(dto.getRspUserId());
        if (dto.getRspUserName() != null) software.setRspUserName(dto.getRspUserName());
        if (dto.getProductType() != null) software.setProductType(dto.getProductType());
        if (dto.getRecommendedVersion() != null) software.setRecommendedVersion(dto.getRecommendedVersion());
        if (dto.getApplicationScene() != null) software.setApplicationScene(dto.getApplicationScene());
        if (dto.getDataSrcFlag() != null) software.setDataSrcFlag(dto.getDataSrcFlag());
        if (dto.getSyncDatetime() != null) software.setSyncDatetime(dto.getSyncDatetime());
        if (dto.getBatchDate() != null) software.setBatchDate(dto.getBatchDate());
    }
}
