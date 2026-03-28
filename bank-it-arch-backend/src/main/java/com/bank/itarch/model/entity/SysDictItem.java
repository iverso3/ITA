package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_item")
public class SysDictItem extends BaseEntity {
    private Long dictId;
    private String itemCode;
    private String itemName;
    private String itemValue;
    private Integer sortOrder;
    private Integer isDefault;
    private Integer isActive;
    private String description;
}
