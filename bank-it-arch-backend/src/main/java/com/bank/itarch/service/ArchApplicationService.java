package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.ArchApplicationMapper;
import com.bank.itarch.model.dto.ApplicationDetailDTO;
import com.bank.itarch.model.entity.ArchApplication;
import com.bank.itarch.model.entity.ArchApplicationModule;
import com.bank.itarch.model.entity.ArchDependency;
import com.bank.itarch.model.entity.ArchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArchApplicationService extends ServiceImpl<ArchApplicationMapper, ArchApplication> {

    private final ArchApplicationModuleService moduleService;
    private final ArchServiceService serviceService;
    private final ArchDependencyService dependencyService;

    public PageResult<ArchApplication> pageQuery(PageQuery query, String keyword, String status, String lifecycle, Long departmentId) {
        Page<ArchApplication> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<ArchApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), w -> w.like(ArchApplication::getAppName, keyword).or().like(ArchApplication::getAppCode, keyword))
               .eq(StringUtils.hasText(status), ArchApplication::getStatus, status)
               .eq(StringUtils.hasText(lifecycle), ArchApplication::getLifecycle, lifecycle)
               .eq(departmentId != null, ArchApplication::getDepartmentId, departmentId)
               .orderByDesc(ArchApplication::getCreateTime);
        Page<ArchApplication> result = page(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    public ArchApplication detail(Long id) {
        ArchApplication entity = getById(id);
        if (entity == null) throw new BusinessException(3001, "应用不存在");
        return entity;
    }

    public ApplicationDetailDTO getDetail(Long id) {
        ArchApplication entity = getById(id);
        if (entity == null) throw new BusinessException(3001, "应用不存在");

        List<ArchApplicationModule> modules = moduleService.listByAppId(id);
        List<ArchService> services = serviceService.listByAppId(id);
        List<ArchDependency> dependencies = dependencyService.listByAppId(id);

        ApplicationDetailDTO dto = new ApplicationDetailDTO();
        dto.setApplication(entity);
        dto.setModules(modules);
        dto.setServices(services);
        dto.setDependencies(dependencies);
        return dto;
    }

    public ArchApplication create(ArchApplication entity) {
        long count = count() + 1;
        entity.setAppCode(String.format("APP-%05d", count));
        save(entity);
        return entity;
    }

    public ArchApplication update(Long id, ArchApplication entity) {
        if (getById(id) == null) throw new BusinessException(3001, "应用不存在");
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(3001, "应用不存在");
        removeById(id);
    }
}
