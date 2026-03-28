package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("arch_tech_stack")
public class ArchTechStack extends BaseEntity {
    private String stackCode;
    private String stackName;
    private String stackType;
    private Long parentId;
    private String treePath;
    private String version;
    private String vendor;
    private String licenseType;
    private Integer isStandard;
    private Integer isActive;
    private String description;
    private String usageGuide;
    private String remark;
}
