package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.ArchDataFlowMapper;
import com.bank.itarch.model.entity.ArchDataFlow;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class ArchDataFlowService extends ServiceImpl<ArchDataFlowMapper, ArchDataFlow> {

    public PageResult<ArchDataFlow> pageQuery(PageQuery query, String keyword) {
        Page<ArchDataFlow> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<ArchDataFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), ArchDataFlow::getFlowName, keyword)
               .orderByDesc(ArchDataFlow::getCreateTime);
        Page<ArchDataFlow> result = page(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    public ArchDataFlow create(ArchDataFlow entity) {
        long count = count() + 1;
        entity.setFlowCode(String.format("FLOW-%05d", count));
        save(entity);
        return entity;
    }

    public ArchDataFlow update(Long id, ArchDataFlow entity) {
        if (getById(id) == null) throw new BusinessException(5001, "数据流向不存在");
        entity.setId(id);
        updateById(entity);
        return getById(id);
    }

    public void delete(Long id) {
        if (getById(id) == null) throw new BusinessException(5001, "数据流向不存在");
        removeById(id);
    }

    public Map<String, Object> getFlowGraph() {
        List<ArchDataFlow> all = list();
        Set<Map<String, Object>> nodes = new LinkedHashSet<>();
        List<Map<String, Object>> links = new ArrayList<>();
        for (ArchDataFlow flow : all) {
            Map<String, Object> src = new HashMap<>();
            src.put("id", flow.getSourceAppId());
            src.put("name", flow.getSourceAppName());
            nodes.add(src);
            Map<String, Object> tgt = new HashMap<>();
            tgt.put("id", flow.getTargetAppId());
            tgt.put("name", flow.getTargetAppName());
            nodes.add(tgt);
            Map<String, Object> link = new HashMap<>();
            link.put("source", flow.getSourceAppId());
            link.put("target", flow.getTargetAppId());
            link.put("label", flow.getFlowName());
            link.put("type", flow.getFlowType());
            links.add(link);
        }
        Map<String, Object> graph = new HashMap<>();
        graph.put("nodes", new ArrayList<>(nodes));
        graph.put("links", links);
        return graph;
    }
}
