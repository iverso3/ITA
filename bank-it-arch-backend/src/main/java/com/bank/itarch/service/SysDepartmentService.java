package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.mapper.SysDepartmentMapper;
import com.bank.itarch.model.entity.SysDepartment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysDepartmentService extends ServiceImpl<SysDepartmentMapper, SysDepartment> {

    public List<SysDepartment> listAll() {
        return list(new LambdaQueryWrapper<SysDepartment>().orderByAsc(SysDepartment::getSortOrder));
    }

    public List<Map<String, Object>> getTree() {
        List<SysDepartment> all = listAll();
        Map<Long, List<SysDepartment>> grouped = all.stream()
                .collect(Collectors.groupingBy(d -> d.getParentId() == null ? 0L : d.getParentId()));
        return buildTree(grouped, 0L);
    }

    private List<Map<String, Object>> buildTree(Map<Long, List<SysDepartment>> grouped, Long parentId) {
        List<SysDepartment> children = grouped.getOrDefault(parentId, Collections.emptyList());
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysDepartment dept : children) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", dept.getId());
            node.put("departmentCode", dept.getDepartmentCode());
            node.put("departmentName", dept.getDepartmentName());
            node.put("leaderName", dept.getLeaderName());
            node.put("children", buildTree(grouped, dept.getId()));
            result.add(node);
        }
        return result;
    }
}
