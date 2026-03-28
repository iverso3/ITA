package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_definition")
public class WfDefinition extends BaseEntity {
    private String definitionCode;
    private String definitionName;
    private String processType;
    private String description;
    private String version;
    private Integer isActive;
    private String config;
    private String remark;
}
