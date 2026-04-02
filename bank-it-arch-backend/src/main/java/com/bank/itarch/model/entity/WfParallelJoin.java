package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 并行节点汇聚记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_parallel_join")
public class WfParallelJoin extends BaseEntity {
    private Long instanceId;
    private Long parallelNodeId;
    private Long branchNodeId;
    private String branchStatus;
    private LocalDateTime completeTime;
}
