package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department")
public class SysDepartment extends BaseEntity {
    private Long parentId;
    private String treePath;
    private String departmentCode;
    private String departmentName;
    private Long leaderId;
    private String leaderName;
    private Integer sortOrder;
    private Integer isActive;
}
