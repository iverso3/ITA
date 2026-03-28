package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.ArchTechStackMapper;
import com.bank.itarch.model.entity.ArchTechStack;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArchTechStackService extends ServiceImpl<ArchTechStackMapper, ArchTechStack> {

    public PageResult<ArchTechStack> pageQuery(PageQuery query, String keyword, String stackType) {
        Page<ArchTechStack> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<ArchTechStack> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), ArchTechStack::getStackName, keyword)
               .eq(StringUtils.hasText(stackType), ArchTechStack::getStackType, stackType)
               .orderByAsc(ArchTechStack::getStackType, ArchTechStack::getStackName);
        Page<ArchTechStack> result = page(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    public List<Map<String, Object>> getTree() {
        List<ArchTechStack> all = list(new LambdaQueryWrapper<ArchTechStack>().eq(ArchTechStack::getIsActive, 1));
        Map<Long, List<ArchTechStack>> grouped = all.stream()
                .collect(Collectors.groupingBy(s -> s.getParentId() == null ? 0L : s.getParentId()));
        return buildTree(grouped, 0L);
    }

    private List<Map<String, Object>> buildTree(Map<Long, List<ArchTechStack>> grouped, Long parentId) {
        List<ArchTechStack> children = grouped.getOrDefault(parentId, Collections.emptyList());
        List<Map<String, Object>> result = new ArrayList<>();
        for (ArchTechStack stack : children) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", stack.getId());
            node.put("stackCode", stack.getStackCode());
            node.put("stackName", stack.getStackName());
            node.put("stackType", stack.getStackType());
            node.put("version", stack.getVersion());
            node.put("isStandard", stack.getIsStandard());
            node.put("children", buildTree(grouped, stack.getId()));
            result.add(node);
        }
        return result;
    }

    public ArchTechStack create(ArchTechStack entity) {
        save(entity);
        return entity;
    }

    public ArchTechStack update(Long id, ArchTechStack entity) {
        if (getById(id) == null) throw new BusinessException(4001, "技术栈不存在");
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(4001, "技术栈不存在");
        removeById(id);
    }
}
