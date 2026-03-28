package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.mapper.SysDictMapper;
import com.bank.itarch.model.entity.SysDict;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDictService extends ServiceImpl<SysDictMapper, SysDict> {

    public List<SysDict> listAll() {
        return list(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getIsActive, 1)
                .orderByAsc(SysDict::getSortOrder));
    }

    public SysDict detail(Long id) {
        SysDict dict = getById(id);
        if (dict == null) throw new BusinessException(1001, "字典不存在");
        return dict;
    }

    public SysDict create(SysDict entity) {
        save(entity);
        return entity;
    }

    public SysDict update(Long id, SysDict entity) {
        if (getById(id) == null) throw new BusinessException(1001, "字典不存在");
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(1001, "字典不存在");
        removeById(id);
    }
}
