package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class EvalIndicatorDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 一级指标 */
    private String level1;

    /** 二级指标 */
    private String level2;

    /** 三级指标 */
    private String level3;

    /** 评测标准 */
    private String criteria;

    /** 分值权重 */
    private Integer weight;

    /** 评测结果 */
    private String result;

    /** 评测得分 */
    private Integer score;

    /** 是否准入指标 */
    private Boolean isEntryIndicator;

    /** 指标类型: DROPDOWN-下拉选择, NUMBER-数值输入 */
    private String inputType;

    /** 下拉选项列表 (用于下拉选择类型的指标) */
    private List<EvalOption> options;
}
