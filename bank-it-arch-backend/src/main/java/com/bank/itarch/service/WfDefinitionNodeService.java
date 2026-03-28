package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.mapper.WfDefinitionNodeMapper;
import com.bank.itarch.model.entity.WfDefinitionNode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WfDefinitionNodeService extends ServiceImpl<WfDefinitionNodeMapper, WfDefinitionNode> {

    public List<WfDefinitionNode> listByDefinitionId(Long definitionId) {
        return list(new LambdaQueryWrapper<WfDefinitionNode>()
                .eq(definitionId != null, WfDefinitionNode::getDefinitionId, definitionId)
                .orderByAsc(WfDefinitionNode::getNodeOrder));
    }

    public WfDefinitionNode getNextNode(Long definitionId, Integer currentOrder) {
        return getOne(new LambdaQueryWrapper<WfDefinitionNode>()
                .eq(WfDefinitionNode::getDefinitionId, definitionId)
                .gt(WfDefinitionNode::getNodeOrder, currentOrder)
                .orderByAsc(WfDefinitionNode::getNodeOrder)
                .last("LIMIT 1"));
    }
}
