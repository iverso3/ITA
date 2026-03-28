package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.CmdbCloudResourceMapper;
import com.bank.itarch.model.entity.CmdbCloudResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CmdbCloudResourceService extends ServiceImpl<CmdbCloudResourceMapper, CmdbCloudResource> {

    public PageResult<CmdbCloudResource> pageQuery(PageQuery query, String keyword, String status, Long departmentId) {
        Page<CmdbCloudResource> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<CmdbCloudResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), CmdbCloudResource::getResourceName, keyword)
               .eq(StringUtils.hasText(status), CmdbCloudResource::getStatus, status)
               .eq(departmentId != null, CmdbCloudResource::getDepartmentId, departmentId)
               .orderByDesc(CmdbCloudResource::getCreateTime);
        Page<CmdbCloudResource> result = page(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    public CmdbCloudResource detail(Long id) {
        CmdbCloudResource entity = getById(id);
        if (entity == null) throw new BusinessException(2001, "云资源不存在");
        return entity;
    }

    public CmdbCloudResource create(CmdbCloudResource entity) {
        long count = count() + 1;
        entity.setResourceCode(String.format("ASSET-CLOUD-%05d", count));
        save(entity);
        return entity;
    }

    public CmdbCloudResource update(Long id, CmdbCloudResource entity) {
        CmdbCloudResource existing = getById(id);
        if (existing == null) throw new BusinessException(2001, "云资源不存在");
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(2001, "云资源不存在");
        removeById(id);
    }
}
