package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("arch_dependency")
public class ArchDependency extends BaseEntity {
    private Long sourceAppId;
    private String sourceAppCode;
    private String sourceAppName;
    private Long sourceServiceId;
    private String sourceServiceName;
    private Long targetAppId;
    private String targetAppCode;
    private String targetAppName;
    private Long targetServiceId;
    private String targetServiceName;
    private String dependencyType;
    private String description;
    private String interfaceType;
    private String interfacePath;
    private String remark;
}
