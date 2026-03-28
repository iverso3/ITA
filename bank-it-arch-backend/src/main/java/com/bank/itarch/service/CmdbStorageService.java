package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.CmdbStorageMapper;
import com.bank.itarch.model.entity.CmdbStorage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CmdbStorageService extends ServiceImpl<CmdbStorageMapper, CmdbStorage> {

    public PageResult<CmdbStorage> pageQuery(PageQuery query, String keyword, String status, Long departmentId) {
        Page<CmdbStorage> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<CmdbStorage> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), CmdbStorage::getDeviceName, keyword)
               .eq(StringUtils.hasText(status), CmdbStorage::getStatus, status)
               .eq(departmentId != null, CmdbStorage::getDepartmentId, departmentId)
               .orderByDesc(CmdbStorage::getCreateTime);
        Page<CmdbStorage> result = page(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    public CmdbStorage detail(Long id) {
        CmdbStorage entity = getById(id);
        if (entity == null) throw new BusinessException(2001, "存储设备不存在");
        return entity;
    }

    public CmdbStorage create(CmdbStorage entity) {
        long count = count() + 1;
        entity.setAssetCode(String.format("ASSET-STOR-%05d", count));
        save(entity);
        return entity;
    }

    public CmdbStorage update(Long id, CmdbStorage entity) {
        CmdbStorage existing = getById(id);
        if (existing == null) throw new BusinessException(2001, "存储设备不存在");
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(2001, "存储设备不存在");
        removeById(id);
    }
}
