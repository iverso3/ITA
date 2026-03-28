package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.mapper.ArchDependencyMapper;
import com.bank.itarch.model.entity.ArchDependency;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ArchDependencyService extends ServiceImpl<ArchDependencyMapper, ArchDependency> {

    public List<ArchDependency> listByAppId(Long appId) {
        return list(new LambdaQueryWrapper<ArchDependency>()
                .eq(ArchDependency::getSourceAppId, appId)
                .or()
                .eq(ArchDependency::getTargetAppId, appId));
    }

    public ArchDependency create(ArchDependency entity) {
        save(entity);
        return entity;
    }

    public void delete(Long id) {
        removeById(id);
    }

    public Map<String, Object> getDependencyGraph() {
        List<ArchDependency> all = list();
        Set<Map<String, Object>> nodes = new LinkedHashSet<>();
        List<Map<String, Object>> links = new ArrayList<>();

        for (ArchDependency dep : all) {
            Map<String, Object> sourceNode = new HashMap<>();
            sourceNode.put("id", dep.getSourceAppId());
            sourceNode.put("name", dep.getSourceAppName());
            nodes.add(sourceNode);

            Map<String, Object> targetNode = new HashMap<>();
            targetNode.put("id", dep.getTargetAppId());
            targetNode.put("name", dep.getTargetAppName());
            nodes.add(targetNode);

            Map<String, Object> link = new HashMap<>();
            link.put("source", dep.getSourceAppId());
            link.put("target", dep.getTargetAppId());
            link.put("type", dep.getDependencyType());
            link.put("label", dep.getInterfaceType());
            links.add(link);
        }

        Map<String, Object> graph = new HashMap<>();
        graph.put("nodes", new ArrayList<>(nodes));
        graph.put("links", links);
        return graph;
    }
}
