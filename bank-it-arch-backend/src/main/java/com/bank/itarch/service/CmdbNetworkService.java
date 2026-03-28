package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.CmdbNetworkMapper;
import com.bank.itarch.model.entity.CmdbNetwork;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CmdbNetworkService extends ServiceImpl<CmdbNetworkMapper, CmdbNetwork> {

    public PageResult<CmdbNetwork> pageQuery(PageQuery query, String keyword, String status, Long departmentId) {
        Page<CmdbNetwork> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<CmdbNetwork> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), w -> w.like(CmdbNetwork::getDeviceName, keyword).or().like(CmdbNetwork::getIpAddress, keyword))
               .eq(StringUtils.hasText(status), CmdbNetwork::getStatus, status)
               .eq(departmentId != null, CmdbNetwork::getDepartmentId, departmentId)
               .orderByDesc(CmdbNetwork::getCreateTime);
        Page<CmdbNetwork> result = page(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    public CmdbNetwork detail(Long id) {
        CmdbNetwork entity = getById(id);
        if (entity == null) throw new BusinessException(2001, "网络设备不存在");
        return entity;
    }

    public CmdbNetwork create(CmdbNetwork entity) {
        long count = count() + 1;
        entity.setAssetCode(String.format("ASSET-NET-%05d", count));
        save(entity);
        return entity;
    }

    public CmdbNetwork update(Long id, CmdbNetwork entity) {
        CmdbNetwork existing = getById(id);
        if (existing == null) throw new BusinessException(2001, "网络设备不存在");
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(2001, "网络设备不存在");
        removeById(id);
    }
}
