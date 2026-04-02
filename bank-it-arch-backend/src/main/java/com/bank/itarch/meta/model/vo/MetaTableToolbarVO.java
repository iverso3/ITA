package com.bank.itarch.meta.model.vo;

import lombok.Data;

/**
 * 表格工具栏配置VO
 */
@Data
public class MetaTableToolbarVO {

    private Boolean showSearch;
    private Boolean showRefresh;
    private Boolean showColumns;
    private Boolean showExport;
    private Boolean showAdd;
    private Boolean showBatchDelete;
}
