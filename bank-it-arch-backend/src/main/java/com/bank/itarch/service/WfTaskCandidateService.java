package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.mapper.WfTaskCandidateMapper;
import com.bank.itarch.model.entity.WfTaskCandidate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WfTaskCandidateService extends ServiceImpl<WfTaskCandidateMapper, WfTaskCandidate> {

    public List<WfTaskCandidate> listByTaskId(Long taskId) {
        return list(new LambdaQueryWrapper<WfTaskCandidate>()
                .eq(taskId != null, WfTaskCandidate::getTaskId, taskId));
    }

    public void saveCandidates(Long taskId, List<WfTaskCandidate> candidates) {
        // Delete existing
        remove(new LambdaQueryWrapper<WfTaskCandidate>().eq(WfTaskCandidate::getTaskId, taskId));
        // Save new
        for (WfTaskCandidate candidate : candidates) {
            candidate.setTaskId(taskId);
            save(candidate);
        }
    }
}
