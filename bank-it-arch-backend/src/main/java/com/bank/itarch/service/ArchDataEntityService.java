package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.ArchDataEntityMapper;
import com.bank.itarch.model.entity.ArchDataEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ArchDataEntityService extends ServiceImpl<ArchDataEntityMapper, ArchDataEntity> {

    public PageResult<ArchDataEntity> pageQuery(PageQuery query, String keyword, Long appId) {
        Page<ArchDataEntity> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<ArchDataEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), ArchDataEntity::getEntityName, keyword)
               .eq(appId != null, ArchDataEntity::getAppId, appId)
               .orderByDesc(ArchDataEntity::getCreateTime);
        Page<ArchDataEntity> result = page(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    public ArchDataEntity detail(Long id) {
        ArchDataEntity entity = getById(id);
        if (entity == null) throw new BusinessException(5001, "数据实体不存在");
        return entity;
    }

    public ArchDataEntity create(ArchDataEntity entity) {
        save(entity);
        return entity;
    }

    public ArchDataEntity update(Long id, ArchDataEntity entity) {
        if (getById(id) == null) throw new BusinessException(5001, "数据实体不存在");
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(5001, "数据实体不存在");
        removeById(id);
    }
}
