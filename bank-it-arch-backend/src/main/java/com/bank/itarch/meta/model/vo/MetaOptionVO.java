package com.bank.itarch.meta.model.vo;

import lombok.Data;

/**
 * 选项配置VO
 */
@Data
public class MetaOptionVO {

    private String value;
    private String label;
    private String color;
    private Boolean disabled;
}
