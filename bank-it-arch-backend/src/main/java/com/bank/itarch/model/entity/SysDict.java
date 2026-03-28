package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict")
public class SysDict extends BaseEntity {
    private String dictCode;
    private String dictName;
    private String dictType;
    private String description;
    private Integer isActive;
    private Integer sortOrder;
}
