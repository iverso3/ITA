package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.mapper.ArchApplicationModuleMapper;
import com.bank.itarch.model.entity.ArchApplicationModule;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArchApplicationModuleService extends ServiceImpl<ArchApplicationModuleMapper, ArchApplicationModule> {

    public List<ArchApplicationModule> listByAppId(Long appId) {
        return list(new LambdaQueryWrapper<ArchApplicationModule>()
                .eq(ArchApplicationModule::getAppId, appId)
                .orderByAsc(ArchApplicationModule::getSortOrder));
    }

    public ArchApplicationModule create(ArchApplicationModule entity) {
        save(entity);
        return entity;
    }

    public ArchApplicationModule update(Long id, ArchApplicationModule entity) {
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        removeById(id);
    }
}
