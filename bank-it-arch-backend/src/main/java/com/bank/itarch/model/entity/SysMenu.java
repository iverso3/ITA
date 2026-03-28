package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {
    private Long parentId;
    private String treePath;
    private String menuCode;
    private String menuName;
    private String menuType;
    private String icon;
    private String path;
    private String component;
    private String redirect;
    private Integer isVisible;
    private Integer isCache;
    private Integer sortOrder;
    private Integer isActive;
    private String externalLink;
    private String permission;
}
