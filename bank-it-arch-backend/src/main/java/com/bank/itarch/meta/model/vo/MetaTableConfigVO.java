package com.bank.itarch.meta.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 表格配置VO
 */
@Data
public class MetaTableConfigVO {

    private Long modelId;
    private String modelCode;
    private String modelName;
    private List<MetaTableColumnVO> columns;
    private MetaTableToolbarVO toolbar;
    private MetaTablePaginationVO pagination;
}
