package com.bank.itarch.meta.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 校验规则VO
 */
@Data
@AllArgsConstructor
public class MetaValidationVO {

    private String type;
    private String message;
    private String rule; // 正则表达式或其他规则
}
