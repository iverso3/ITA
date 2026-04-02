package com.bank.itarch.meta.model.vo;

import lombok.Data;

/**
 * 表格分页配置VO
 */
@Data
public class MetaTablePaginationVO {

    private Boolean show;
    private Integer pageSize;
    private Integer pageSizes;
    private String layout;
}
