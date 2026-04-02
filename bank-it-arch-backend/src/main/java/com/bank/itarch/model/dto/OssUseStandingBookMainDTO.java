package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OssUseStandingBookMainDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String swName;
    private String swVersion;
    private String swFullName;
    private String licAbbr;
    private String environment;
    private LocalDate scanDate;
    private String swCategory;
    private String appNo;
    private String appName;
    private LocalDateTime syncDatetime;
    private Integer createMode;
    private String createUserId;
    private LocalDateTime createDatetime;
    private String updateUserId;
    private LocalDateTime updateDatetime;
    private String isCommerc;
}