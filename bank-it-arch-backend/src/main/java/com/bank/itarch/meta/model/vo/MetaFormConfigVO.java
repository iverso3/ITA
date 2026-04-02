package com.bank.itarch.meta.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 表单配置VO
 */
@Data
public class MetaFormConfigVO {

    private Long modelId;
    private String modelCode;
    private String modelName;
    private List<MetaFormGroupVO> groups;
}
