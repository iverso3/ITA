package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("arch_application_module")
public class ArchApplicationModule extends BaseEntity {
    private Long appId;
    private String moduleCode;
    private String moduleName;
    private String moduleType;
    private String description;
    private String techStack;
    private Long parentModuleId;
    private Integer sortOrder;
    private String status;
    private Long teamId;
    private String teamName;
}
