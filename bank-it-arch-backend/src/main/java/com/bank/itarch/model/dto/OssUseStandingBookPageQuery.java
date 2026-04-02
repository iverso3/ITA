package com.bank.itarch.model.dto;

import com.bank.itarch.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class OssUseStandingBookPageQuery extends PageQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    private String swName;
    private String swVersion;
    private String swCategory;
    private String appName;
    private String environment;
    private String swFullName;
}