package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 流程版本记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_definition_version")
public class WfDefinitionVersion extends BaseEntity {
    private Long definitionId;
    private Integer version;
    private String status;
    private LocalDateTime publishTime;
    private String publishUser;
    private String changeLog;
}
