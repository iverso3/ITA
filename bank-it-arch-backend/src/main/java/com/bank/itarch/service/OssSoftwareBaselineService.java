package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.OssSoftwareBaselineMapper;
import com.bank.itarch.model.dto.OssSoftwareBaselineDTO;
import com.bank.itarch.model.dto.OssSoftwareBaselineQueryDTO;
import com.bank.itarch.model.dto.OssSoftwareMediaDTO;
import com.bank.itarch.model.dto.OssSoftwareMediaQueryDTO;
import com.bank.itarch.model.entity.OssSoftwareBaseline;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OssSoftwareBaselineService extends ServiceImpl<OssSoftwareBaselineMapper, OssSoftwareBaseline> {

    public PageResult<OssSoftwareBaselineDTO> pageQuery(OssSoftwareBaselineQueryDTO query) {
        Page<OssSoftwareBaseline> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<OssSoftwareBaseline> wrapper = new LambdaQueryWrapper<>();
        // 根据exactMatch决定精确还是模糊查询
        if (Boolean.TRUE.equals(query.getExactMatch())) {
            wrapper.eq(StringUtils.hasText(query.getSwName()), OssSoftwareBaseline::getSwName, query.getSwName());
        } else {
            wrapper.like(StringUtils.hasText(query.getSwName()), OssSoftwareBaseline::getSwName, query.getSwName());
        }
        wrapper.like(StringUtils.hasText(query.getSwVersion()), OssSoftwareBaseline::getSwVersion, query.getSwVersion())
               .eq(StringUtils.hasText(query.getSwCategory()), OssSoftwareBaseline::getSwCategory, query.getSwCategory())
               .eq(StringUtils.hasText(query.getSwType()), OssSoftwareBaseline::getSwType, query.getSwType())
               .eq(StringUtils.hasText(query.getVerType()), OssSoftwareBaseline::getVerType, query.getVerType())
               .eq(StringUtils.hasText(query.getIsMainUse()), OssSoftwareBaseline::getIsMainUse, query.getIsMainUse())
               .like(StringUtils.hasText(query.getLicAbbr()), OssSoftwareBaseline::getLicAbbr, query.getLicAbbr())
               .eq(StringUtils.hasText(query.getImplTeamId()), OssSoftwareBaseline::getImplTeamId, query.getImplTeamId())
               .orderByDesc(OssSoftwareBaseline::getCreateDatetime);

        IPage<OssSoftwareBaseline> result = page(page, wrapper);

        List<OssSoftwareBaselineDTO> dtoList = result.getRecords().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());

        return PageResult.of(dtoList, result.getTotal(), query.getPage(), query.getPageSize());
    }

    public OssSoftwareBaselineDTO getById(String id) {
        OssSoftwareBaseline baseline = super.getById(id);
        if (baseline == null) {
            throw new BusinessException(2001, "Version not found");
        }
        return toDTO(baseline);
    }

    @Transactional
    public OssSoftwareBaselineDTO create(OssSoftwareBaselineDTO dto) {
        OssSoftwareBaseline baseline = toEntity(dto);
        baseline.setCreateMode(0);
        save(baseline);
        return toDTO(baseline);
    }

    @Transactional
    public OssSoftwareBaselineDTO update(String id, OssSoftwareBaselineDTO dto) {
        OssSoftwareBaseline existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Version not found");
        }
        updateEntity(existing, dto);
        updateById(existing);
        return toDTO(existing);
    }

    @Transactional
    public void delete(String id) {
        OssSoftwareBaseline existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Version not found");
        }
        removeById(id);
    }

    public List<OssSoftwareBaselineDTO> listAllForExport(OssSoftwareBaselineQueryDTO query) {
        LambdaQueryWrapper<OssSoftwareBaseline> wrapper = new LambdaQueryWrapper<>();
        if (Boolean.TRUE.equals(query.getExactMatch())) {
            wrapper.eq(StringUtils.hasText(query.getSwName()), OssSoftwareBaseline::getSwName, query.getSwName());
        } else {
            wrapper.like(StringUtils.hasText(query.getSwName()), OssSoftwareBaseline::getSwName, query.getSwName());
        }
        wrapper.like(StringUtils.hasText(query.getSwVersion()), OssSoftwareBaseline::getSwVersion, query.getSwVersion())
               .eq(StringUtils.hasText(query.getSwCategory()), OssSoftwareBaseline::getSwCategory, query.getSwCategory())
               .eq(StringUtils.hasText(query.getSwType()), OssSoftwareBaseline::getSwType, query.getSwType())
               .eq(StringUtils.hasText(query.getVerType()), OssSoftwareBaseline::getVerType, query.getVerType())
               .eq(StringUtils.hasText(query.getIsMainUse()), OssSoftwareBaseline::getIsMainUse, query.getIsMainUse())
               .like(StringUtils.hasText(query.getLicAbbr()), OssSoftwareBaseline::getLicAbbr, query.getLicAbbr())
               .eq(StringUtils.hasText(query.getImplTeamId()), OssSoftwareBaseline::getImplTeamId, query.getImplTeamId())
               .orderByDesc(OssSoftwareBaseline::getCreateDatetime);

        List<OssSoftwareBaseline> list = list(wrapper);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private OssSoftwareBaselineDTO toDTO(OssSoftwareBaseline baseline) {
        OssSoftwareBaselineDTO dto = new OssSoftwareBaselineDTO();
        dto.setId(baseline.getId());
        dto.setSwId(baseline.getSwId());
        dto.setSwName(baseline.getSwName());
        dto.setSwVersion(baseline.getSwVersion());
        dto.setSwCategory(baseline.getSwCategory());
        dto.setSwType(baseline.getSwType());
        dto.setVerType(baseline.getVerType());
        dto.setImplTeamId(baseline.getImplTeamId());
        dto.setImplTeamName(baseline.getImplTeamName());
        dto.setImplUserId(baseline.getImplUserId());
        dto.setImplUserName(baseline.getImplUserName());
        dto.setImplApplyNo(baseline.getImplApplyNo());
        dto.setLicId(baseline.getLicId());
        dto.setLicAbbr(baseline.getLicAbbr());
        dto.setLastEvalDatetime(baseline.getLastEvalDatetime());
        dto.setLastEvalId(baseline.getLastEvalId());
        dto.setEffectDatetime(baseline.getEffectDatetime());
        dto.setExpireDatetime(baseline.getExpireDatetime());
        dto.setSyncDatetime(baseline.getSyncDatetime());
        dto.setCreateMode(baseline.getCreateMode());
        dto.setCreateUserId(baseline.getCreateUserId());
        dto.setCreateDatetime(baseline.getCreateDatetime());
        dto.setUpdateUserId(baseline.getUpdateUserId());
        dto.setUpdateDatetime(baseline.getUpdateDatetime());
        dto.setUseBranchId(baseline.getUseBranchId());
        dto.setUseBranchName(baseline.getUseBranchName());
        dto.setRemark(baseline.getRemark());
        dto.setMediaAddr(baseline.getMediaAddr());
        dto.setIsMainUse(baseline.getIsMainUse());
        dto.setApplicableScene(baseline.getApplicableScene());
        dto.setApplicableFunctionRange(baseline.getApplicableFunctionRange());
        dto.setSwFullName(baseline.getSwFullName());
        dto.setDependType(baseline.getDependType());
        dto.setDataSrcFlag(baseline.getDataSrcFlag());
        dto.setBatchDate(baseline.getBatchDate());
        return dto;
    }

    private OssSoftwareBaseline toEntity(OssSoftwareBaselineDTO dto) {
        OssSoftwareBaseline baseline = new OssSoftwareBaseline();
        baseline.setSwId(dto.getSwId());
        baseline.setSwName(dto.getSwName());
        baseline.setSwVersion(dto.getSwVersion());
        baseline.setSwCategory(dto.getSwCategory());
        baseline.setSwType(dto.getSwType());
        baseline.setVerType(dto.getVerType());
        baseline.setImplTeamId(dto.getImplTeamId());
        baseline.setImplTeamName(dto.getImplTeamName());
        baseline.setImplUserId(dto.getImplUserId());
        baseline.setImplUserName(dto.getImplUserName());
        baseline.setImplApplyNo(dto.getImplApplyNo());
        baseline.setLicId(dto.getLicId());
        baseline.setLicAbbr(dto.getLicAbbr());
        baseline.setLastEvalDatetime(dto.getLastEvalDatetime());
        baseline.setLastEvalId(dto.getLastEvalId());
        baseline.setEffectDatetime(dto.getEffectDatetime());
        baseline.setExpireDatetime(dto.getExpireDatetime());
        baseline.setSyncDatetime(dto.getSyncDatetime());
        baseline.setUseBranchId(dto.getUseBranchId());
        baseline.setUseBranchName(dto.getUseBranchName());
        baseline.setRemark(dto.getRemark());
        baseline.setMediaAddr(dto.getMediaAddr());
        baseline.setIsMainUse(dto.getIsMainUse());
        baseline.setApplicableScene(dto.getApplicableScene());
        baseline.setApplicableFunctionRange(dto.getApplicableFunctionRange());
        baseline.setSwFullName(dto.getSwFullName());
        baseline.setDependType(dto.getDependType());
        baseline.setDataSrcFlag(dto.getDataSrcFlag());
        baseline.setBatchDate(dto.getBatchDate());
        return baseline;
    }

    private void updateEntity(OssSoftwareBaseline baseline, OssSoftwareBaselineDTO dto) {
        if (dto.getSwId() != null) baseline.setSwId(dto.getSwId());
        if (dto.getSwName() != null) baseline.setSwName(dto.getSwName());
        if (dto.getSwVersion() != null) baseline.setSwVersion(dto.getSwVersion());
        if (dto.getSwCategory() != null) baseline.setSwCategory(dto.getSwCategory());
        if (dto.getSwType() != null) baseline.setSwType(dto.getSwType());
        if (dto.getVerType() != null) baseline.setVerType(dto.getVerType());
        if (dto.getImplTeamId() != null) baseline.setImplTeamId(dto.getImplTeamId());
        if (dto.getImplTeamName() != null) baseline.setImplTeamName(dto.getImplTeamName());
        if (dto.getImplUserId() != null) baseline.setImplUserId(dto.getImplUserId());
        if (dto.getImplUserName() != null) baseline.setImplUserName(dto.getImplUserName());
        if (dto.getImplApplyNo() != null) baseline.setImplApplyNo(dto.getImplApplyNo());
        if (dto.getLicId() != null) baseline.setLicId(dto.getLicId());
        if (dto.getLicAbbr() != null) baseline.setLicAbbr(dto.getLicAbbr());
        if (dto.getLastEvalDatetime() != null) baseline.setLastEvalDatetime(dto.getLastEvalDatetime());
        if (dto.getLastEvalId() != null) baseline.setLastEvalId(dto.getLastEvalId());
        if (dto.getEffectDatetime() != null) baseline.setEffectDatetime(dto.getEffectDatetime());
        if (dto.getExpireDatetime() != null) baseline.setExpireDatetime(dto.getExpireDatetime());
        if (dto.getSyncDatetime() != null) baseline.setSyncDatetime(dto.getSyncDatetime());
        if (dto.getUseBranchId() != null) baseline.setUseBranchId(dto.getUseBranchId());
        if (dto.getUseBranchName() != null) baseline.setUseBranchName(dto.getUseBranchName());
        if (dto.getRemark() != null) baseline.setRemark(dto.getRemark());
        if (dto.getMediaAddr() != null) baseline.setMediaAddr(dto.getMediaAddr());
        if (dto.getIsMainUse() != null) baseline.setIsMainUse(dto.getIsMainUse());
        if (dto.getApplicableScene() != null) baseline.setApplicableScene(dto.getApplicableScene());
        if (dto.getApplicableFunctionRange() != null) baseline.setApplicableFunctionRange(dto.getApplicableFunctionRange());
        if (dto.getSwFullName() != null) baseline.setSwFullName(dto.getSwFullName());
        if (dto.getDependType() != null) baseline.setDependType(dto.getDependType());
        if (dto.getDataSrcFlag() != null) baseline.setDataSrcFlag(dto.getDataSrcFlag());
        if (dto.getBatchDate() != null) baseline.setBatchDate(dto.getBatchDate());
    }

    public PageResult<OssSoftwareMediaDTO> pageQueryMedia(OssSoftwareMediaQueryDTO query) {
        List<Map<String, Object>> rawList = baseMapper.selectMediaList(
            query.getSwName(),
            query.getSwVersion(),
            query.getVerType(),
            query.getIsMainUse(),
            query.getSwCategory()
        );

        List<OssSoftwareMediaDTO> mediaList = new ArrayList<>();
        for (Map<String, Object> row : rawList) {
            String mediaAddr = row.get("media_addr") != null ? row.get("media_addr").toString() : null;

            if (StringUtils.hasText(mediaAddr)) {
                // media_addr is JSON array like [{"type":"linux","name":"file.tar.gz","url":"/path/to/file"}]
                try {
                    List<Object> mediaItems = JSONUtil.toList(mediaAddr, Object.class);
                    for (Object itemObj : mediaItems) {
                        if (itemObj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> item = (Map<String, Object>) itemObj;
                            OssSoftwareMediaDTO dto = new OssSoftwareMediaDTO();
                            dto.setBaselineId(row.get("baseline_id") != null ? row.get("baseline_id").toString() : null);
                            dto.setSwId(row.get("sw_id") != null ? row.get("sw_id").toString() : null);
                            dto.setSwName(row.get("sw_name") != null ? row.get("sw_name").toString() : null);
                            dto.setSwVersion(row.get("sw_version") != null ? row.get("sw_version").toString() : null);
                            dto.setSwCategory(row.get("sw_category") != null ? row.get("sw_category").toString() : null);
                            dto.setVerType(row.get("ver_type") != null ? row.get("ver_type").toString() : null);
                            dto.setIsMainUse(row.get("is_main_use") != null ? row.get("is_main_use").toString() : null);
                            dto.setRecommendedVersion(row.get("recommended_version") != null ? row.get("recommended_version").toString() : null);
                            dto.setMediaType(item.get("type") != null ? item.get("type").toString() : null);
                            dto.setMediaName(item.get("name") != null ? item.get("name").toString() : null);
                            dto.setMediaUrl(item.get("url") != null ? item.get("url").toString() : null);
                            mediaList.add(dto);
                        }
                    }
                } catch (Exception e) {
                    // If JSON parsing fails, skip this record
                }
            }
        }

        int total = mediaList.size();
        int start = (query.getPage() - 1) * query.getPageSize();
        int end = Math.min(start + query.getPageSize(), total);
        List<OssSoftwareMediaDTO> pageList = start < total ? mediaList.subList(start, end) : new ArrayList<>();

        return PageResult.of(pageList, total, query.getPage(), query.getPageSize());
    }
}
