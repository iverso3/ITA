package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.mapper.ArchServiceMapper;
import com.bank.itarch.model.entity.ArchService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArchServiceService extends ServiceImpl<ArchServiceMapper, ArchService> {

    public List<ArchService> listByAppId(Long appId) {
        return list(new LambdaQueryWrapper<ArchService>()
                .eq(ArchService::getAppId, appId)
                .orderByAsc(ArchService::getServiceCode));
    }

    public ArchService create(ArchService entity) {
        save(entity);
        return entity;
    }

    public ArchService update(Long id, ArchService entity) {
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        removeById(id);
    }
}
