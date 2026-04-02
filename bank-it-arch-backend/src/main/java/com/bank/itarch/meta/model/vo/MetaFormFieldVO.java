package com.bank.itarch.meta.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 表单字段VO
 */
@Data
public class MetaFormFieldVO {

    private Long fieldId;
    private String fieldCode;
    private String fieldName;
    private String fieldType;
    private String displayType;
    private Boolean isRequired;
    private String defaultValue;
    private String placeholder;
    private String helpText;
    private String description;

    // 选项配置
    private List<MetaOptionVO> options;

    // 联动配置
    private List<MetaFieldLinkageVO> linkages;

    // 校验规则
    private List<MetaValidationVO> validations;

    // 权限配置
    private Boolean readonly;
    private Boolean hidden;
}
