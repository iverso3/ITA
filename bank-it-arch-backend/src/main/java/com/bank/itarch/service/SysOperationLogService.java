package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.SysOperationLogMapper;
import com.bank.itarch.model.entity.SysOperationLog;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysOperationLogService extends ServiceImpl<SysOperationLogMapper, SysOperationLog> {

    public PageResult<SysOperationLog> pageQuery(PageQuery query, String module, String operatorName) {
        Page<SysOperationLog> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(module), SysOperationLog::getModule, module)
               .like(StringUtils.hasText(operatorName), SysOperationLog::getOperatorName, operatorName)
               .orderByDesc(SysOperationLog::getCreateTime);
        Page<SysOperationLog> result = page(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }
}
