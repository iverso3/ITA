package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.OssUseStandingBookMainMapper;
import com.bank.itarch.mapper.OssUseStandingBookDetailsMapper;
import com.bank.itarch.model.dto.OssUseStandingBookMainDTO;
import com.bank.itarch.model.dto.OssUseStandingBookPageQuery;
import com.bank.itarch.model.entity.OssUseStandingBookMain;
import com.bank.itarch.model.entity.OssUseStandingBookDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OssUseStandingBookMainService extends ServiceImpl<OssUseStandingBookMainMapper, OssUseStandingBookMain> {

    private final OssUseStandingBookDetailsMapper detailsMapper;

    public PageResult<OssUseStandingBookMainDTO> pageQuery(OssUseStandingBookPageQuery query) {
        Page<OssUseStandingBookMain> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<OssUseStandingBookMain> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getSwName()), OssUseStandingBookMain::getSwName, query.getSwName())
               .like(StringUtils.hasText(query.getSwVersion()), OssUseStandingBookMain::getSwVersion, query.getSwVersion())
               .eq(StringUtils.hasText(query.getSwCategory()), OssUseStandingBookMain::getSwCategory, query.getSwCategory())
               .like(StringUtils.hasText(query.getAppName()), OssUseStandingBookMain::getAppName, query.getAppName())
               .eq(StringUtils.hasText(query.getEnvironment()), OssUseStandingBookMain::getEnvironment, query.getEnvironment())
               .like(StringUtils.hasText(query.getSwFullName()), OssUseStandingBookMain::getSwFullName, query.getSwFullName())
               .orderByDesc(OssUseStandingBookMain::getCreateDatetime);

        IPage<OssUseStandingBookMain> result = page(page, wrapper);

        List<OssUseStandingBookMainDTO> dtoList = result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, result.getTotal(), query.getPage(), query.getPageSize());
    }

    public OssUseStandingBookMainDTO getById(String id) {
        OssUseStandingBookMain main = super.getById(id);
        if (main == null) {
            throw new BusinessException(2001, "Main record not found");
        }
        return toDTO(main);
    }

    @Transactional
    public OssUseStandingBookMainDTO create(OssUseStandingBookMainDTO dto) {
        OssUseStandingBookMain main = toEntity(dto);
        main.setCreateMode(0);
        save(main);
        return toDTO(main);
    }

    @Transactional
    public OssUseStandingBookMainDTO update(String id, OssUseStandingBookMainDTO dto) {
        OssUseStandingBookMain existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Main record not found");
        }
        updateEntity(existing, dto);
        updateById(existing);

        // 同步更新关联的Detail冗余字段
        LambdaQueryWrapper<OssUseStandingBookDetails> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssUseStandingBookDetails::getParentId, id);
        List<OssUseStandingBookDetails> details = detailsMapper.selectList(wrapper);
        for (OssUseStandingBookDetails detail : details) {
            syncDetailFromMain(detail, existing);
            detailsMapper.updateById(detail);
        }

        return toDTO(existing);
    }

    @Transactional
    public void delete(String id) {
        OssUseStandingBookMain existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Main record not found");
        }
        // 逻辑删除关联的Detail
        LambdaQueryWrapper<OssUseStandingBookDetails> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssUseStandingBookDetails::getParentId, id);
        List<OssUseStandingBookDetails> details = detailsMapper.selectList(wrapper);
        for (OssUseStandingBookDetails detail : details) {
            detail.setLogicStatus(1);
            detailsMapper.updateById(detail);
        }
        removeById(id);
    }

    public List<OssUseStandingBookMainDTO> listAllForExport(OssUseStandingBookPageQuery query) {
        LambdaQueryWrapper<OssUseStandingBookMain> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getSwName()), OssUseStandingBookMain::getSwName, query.getSwName())
               .like(StringUtils.hasText(query.getSwVersion()), OssUseStandingBookMain::getSwVersion, query.getSwVersion())
               .eq(StringUtils.hasText(query.getSwCategory()), OssUseStandingBookMain::getSwCategory, query.getSwCategory())
               .like(StringUtils.hasText(query.getAppName()), OssUseStandingBookMain::getAppName, query.getAppName())
               .eq(StringUtils.hasText(query.getEnvironment()), OssUseStandingBookMain::getEnvironment, query.getEnvironment())
               .orderByDesc(OssUseStandingBookMain::getCreateDatetime);

        List<OssUseStandingBookMain> list = list(wrapper);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private void syncDetailFromMain(OssUseStandingBookDetails detail, OssUseStandingBookMain main) {
        detail.setSwName(main.getSwName());
        detail.setSwVersion(main.getSwVersion());
        detail.setSwFullName(main.getSwFullName());
        detail.setLicAbbr(main.getLicAbbr());
        detail.setEnvironment(main.getEnvironment());
        detail.setSwCategory(main.getSwCategory());
        detail.setAppNo(main.getAppNo());
        detail.setAppName(main.getAppName());
        detail.setIsCommerc(main.getIsCommerc());
    }

    private OssUseStandingBookMainDTO toDTO(OssUseStandingBookMain main) {
        OssUseStandingBookMainDTO dto = new OssUseStandingBookMainDTO();
        dto.setId(main.getId());
        dto.setSwName(main.getSwName());
        dto.setSwVersion(main.getSwVersion());
        dto.setSwFullName(main.getSwFullName());
        dto.setLicAbbr(main.getLicAbbr());
        dto.setEnvironment(main.getEnvironment());
        dto.setScanDate(main.getScanDate());
        dto.setSwCategory(main.getSwCategory());
        dto.setAppNo(main.getAppNo());
        dto.setAppName(main.getAppName());
        dto.setSyncDatetime(main.getSyncDatetime());
        dto.setCreateMode(main.getCreateMode());
        dto.setCreateUserId(main.getCreateUserId());
        dto.setCreateDatetime(main.getCreateDatetime());
        dto.setUpdateUserId(main.getUpdateUserId());
        dto.setUpdateDatetime(main.getUpdateDatetime());
        dto.setIsCommerc(main.getIsCommerc());
        return dto;
    }

    private OssUseStandingBookMain toEntity(OssUseStandingBookMainDTO dto) {
        OssUseStandingBookMain main = new OssUseStandingBookMain();
        main.setSwName(dto.getSwName());
        main.setSwVersion(dto.getSwVersion());
        main.setSwFullName(dto.getSwFullName());
        main.setLicAbbr(dto.getLicAbbr());
        main.setEnvironment(dto.getEnvironment());
        main.setScanDate(dto.getScanDate());
        main.setSwCategory(dto.getSwCategory());
        main.setAppNo(dto.getAppNo());
        main.setAppName(dto.getAppName());
        main.setSyncDatetime(dto.getSyncDatetime());
        main.setCreateMode(dto.getCreateMode());
        main.setCreateUserId(dto.getCreateUserId());
        main.setUpdateUserId(dto.getUpdateUserId());
        main.setIsCommerc(dto.getIsCommerc());
        return main;
    }

    private void updateEntity(OssUseStandingBookMain main, OssUseStandingBookMainDTO dto) {
        if (dto.getSwName() != null) main.setSwName(dto.getSwName());
        if (dto.getSwVersion() != null) main.setSwVersion(dto.getSwVersion());
        if (dto.getSwFullName() != null) main.setSwFullName(dto.getSwFullName());
        if (dto.getLicAbbr() != null) main.setLicAbbr(dto.getLicAbbr());
        if (dto.getEnvironment() != null) main.setEnvironment(dto.getEnvironment());
        if (dto.getScanDate() != null) main.setScanDate(dto.getScanDate());
        if (dto.getSwCategory() != null) main.setSwCategory(dto.getSwCategory());
        if (dto.getAppNo() != null) main.setAppNo(dto.getAppNo());
        if (dto.getAppName() != null) main.setAppName(dto.getAppName());
        if (dto.getSyncDatetime() != null) main.setSyncDatetime(dto.getSyncDatetime());
        if (dto.getUpdateUserId() != null) main.setUpdateUserId(dto.getUpdateUserId());
        if (dto.getIsCommerc() != null) main.setIsCommerc(dto.getIsCommerc());
    }
}