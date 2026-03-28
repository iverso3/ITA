package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.mapper.SysDictItemMapper;
import com.bank.itarch.model.entity.SysDictItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDictItemService extends ServiceImpl<SysDictItemMapper, SysDictItem> {

    public List<SysDictItem> listByDictId(Long dictId) {
        return list(new LambdaQueryWrapper<SysDictItem>()
                .eq(dictId != null, SysDictItem::getDictId, dictId)
                .eq(SysDictItem::getIsActive, 1)
                .orderByAsc(SysDictItem::getSortOrder));
    }

    public SysDictItem detail(Long id) {
        SysDictItem item = getById(id);
        if (item == null) throw new BusinessException(1001, "字典项不存在");
        return item;
    }

    public SysDictItem create(SysDictItem entity) {
        save(entity);
        return entity;
    }

    public SysDictItem update(Long id, SysDictItem entity) {
        if (getById(id) == null) throw new BusinessException(1001, "字典项不存在");
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(1001, "字典项不存在");
        removeById(id);
    }
}
