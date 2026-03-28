package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.WfTaskMapper;
import com.bank.itarch.model.entity.WfTask;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WfTaskService extends ServiceImpl<WfTaskMapper, WfTask> {

    public PageResult<WfTask> todoList(PageQuery query) {
        Page<WfTask> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<WfTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTask::getStatus, "PENDING")
               .orderByDesc(WfTask::getCreateTime);
        Page<WfTask> result = page(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    public PageResult<WfTask> doneList(PageQuery query) {
        Page<WfTask> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<WfTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(WfTask::getStatus, "PENDING")
               .orderByDesc(WfTask::getCompleteTime);
        Page<WfTask> result = page(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    public void approve(Long id, String opinion) {
        WfTask task = getById(id);
        if (task == null) throw new BusinessException(6001, "任务不存在");
        task.setStatus("APPROVED");
        task.setOpinion(opinion);
        task.setCompleteTime(LocalDateTime.now());
        updateById(task);
    }

    public void reject(Long id, String opinion) {
        WfTask task = getById(id);
        if (task == null) throw new BusinessException(6001, "任务不存在");
        task.setStatus("REJECTED");
        task.setOpinion(opinion);
        task.setCompleteTime(LocalDateTime.now());
        updateById(task);
    }
}
