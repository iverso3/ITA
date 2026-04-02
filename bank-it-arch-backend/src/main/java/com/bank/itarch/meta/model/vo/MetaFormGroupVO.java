package com.bank.itarch.meta.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 表单分组VO
 */
@Data
public class MetaFormGroupVO {

    private Long groupId;
    private String groupCode;
    private String groupName;
    private String groupType;
    private Integer sortOrder;
    private List<MetaFormFieldVO> fields;
}
