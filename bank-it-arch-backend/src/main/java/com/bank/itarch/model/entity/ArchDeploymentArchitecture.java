package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("arch_deployment_architecture")
public class ArchDeploymentArchitecture {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appId;
    private String layer;
    private String nodeName;
    private String nodeType;
    private Integer count;
    private String specification;
    private String deploymentLocation;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String creator;
    private String modifier;
}
