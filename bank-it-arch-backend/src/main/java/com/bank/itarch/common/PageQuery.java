package com.bank.itarch.common;

import lombok.Data;
import java.io.Serializable;

@Data
public class PageQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer page = 1;
    private Integer pageSize = 20;
    private String sortField;
    private String sortOrder = "desc";

    public Integer getOffset() {
        return (page - 1) * pageSize;
    }
}
