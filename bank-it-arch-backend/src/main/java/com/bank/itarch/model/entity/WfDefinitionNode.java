package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wf_definition_node")
public class WfDefinitionNode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long definitionId;
    private String nodeCode;
    private String nodeName;
    private Integer nodeOrder;
    private String nodeType;
    private String approverType;
    private Long approverId;
    private String approverName;
    private Integer isRequired;
    private String approveType;
    private Integer timeoutHours;
    private Integer autoPass;
    private String conditionRule;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String creator;
    private String modifier;
}
