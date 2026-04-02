package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.WfInstanceMapper;
import com.bank.itarch.model.entity.WfInstance;
import org.springframework.stereotype.Service;

@Service
public class WfInstanceService extends ServiceImpl<WfInstanceMapper, WfInstance> {

    public PageResult<WfInstance> pageQuery(PageQuery query, String status) {
        Page<WfInstance> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<WfInstance> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(WfInstance::getStatus, status);
        }
        wrapper.orderByDesc(WfInstance::getStartTime);
        Page<WfInstance> result = page(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    public WfInstance detail(Long id) {
        return getById(id);
    }
}
