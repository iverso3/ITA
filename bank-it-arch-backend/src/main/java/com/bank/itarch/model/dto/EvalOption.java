package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class EvalOption implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 选项标签 */
    private String label;

    /** 选项值 */
    private String value;

    /** 选项分数 */
    private Integer score;

    public EvalOption() {}

    public EvalOption(String label, String value, Integer score) {
        this.label = label;
        this.value = value;
        this.score = score;
    }
}
