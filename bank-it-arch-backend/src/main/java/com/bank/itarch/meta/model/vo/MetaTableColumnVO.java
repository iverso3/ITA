package com.bank.itarch.meta.model.vo;

import lombok.Data;

/**
 * 表格列配置VO
 */
@Data
public class MetaTableColumnVO {

    private String fieldCode;
    private String fieldName;
    private String fieldType;
    private String displayType;
    private Integer width;
    private Boolean sortable;
    private Boolean searchable;
    private String align;
    private String slotName;
}
