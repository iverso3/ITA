package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.mapper.ArchDeploymentArchitectureMapper;
import com.bank.itarch.model.entity.ArchDeploymentArchitecture;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArchDeploymentArchitectureService extends ServiceImpl<ArchDeploymentArchitectureMapper, ArchDeploymentArchitecture> {

    public List<ArchDeploymentArchitecture> listByAppId(Long appId) {
        return list(new LambdaQueryWrapper<ArchDeploymentArchitecture>()
                .eq(appId != null, ArchDeploymentArchitecture::getAppId, appId)
                .orderByAsc(ArchDeploymentArchitecture::getSortOrder));
    }

    public ArchDeploymentArchitecture detail(Long id) {
        ArchDeploymentArchitecture arch = getById(id);
        if (arch == null) throw new BusinessException(4001, "部署架构不存在");
        return arch;
    }

    public ArchDeploymentArchitecture create(ArchDeploymentArchitecture entity) {
        save(entity);
        return entity;
    }

    public ArchDeploymentArchitecture update(Long id, ArchDeploymentArchitecture entity) {
        if (getById(id) == null) throw new BusinessException(4001, "部署架构不存在");
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(4001, "部署架构不存在");
        removeById(id);
    }
}
