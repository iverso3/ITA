package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.mapper.WfHistoryMapper;
import com.bank.itarch.model.entity.WfHistory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WfHistoryService extends ServiceImpl<WfHistoryMapper, WfHistory> {

    public List<WfHistory> listByInstanceId(Long instanceId) {
        return list(new LambdaQueryWrapper<WfHistory>()
                .eq(WfHistory::getInstanceId, instanceId)
                .orderByAsc(WfHistory::getCreateTime));
    }
}
