package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.WfDefinitionMapper;
import com.bank.itarch.model.entity.WfDefinition;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class WfDefinitionService extends ServiceImpl<WfDefinitionMapper, WfDefinition> {

    public PageResult<WfDefinition> pageQuery(PageQuery query, String keyword) {
        Page<WfDefinition> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<WfDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), WfDefinition::getDefinitionName, keyword)
               .orderByDesc(WfDefinition::getCreateTime);
        Page<WfDefinition> result = page(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    public WfDefinition create(WfDefinition entity) {
        save(entity);
        return entity;
    }

    public WfDefinition update(Long id, WfDefinition entity) {
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void toggleStatus(Long id) {
        WfDefinition def = getById(id);
        if (def != null) {
            def.setIsActive(def.getIsActive() == 1 ? 0 : 1);
            updateById(def);
        }
    }
}
