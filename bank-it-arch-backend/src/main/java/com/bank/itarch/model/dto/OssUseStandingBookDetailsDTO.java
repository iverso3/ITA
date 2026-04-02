package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OssUseStandingBookDetailsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String swName;
    private String swVersion;
    private String swFullName;
    private String licAbbr;
    private String environment;
    private LocalDateTime scanTime;
    private String swCategory;
    private String appNo;
    private String appName;
    private String parentId;
    private String installPath;
    private Integer source;
    private String ipOrHostName;
    private String command;
    private String detailedInfo;
    private String fileType;
    private String dependType;
    private LocalDateTime syncDatetime;
    private Integer createMode;
    private String createUserId;
    private LocalDateTime createDatetime;
    private String updateUserId;
    private LocalDateTime updateDatetime;
    private String isCommerc;
    private String commercProductName;
    private String commercProductVersion;
    private String projectName;
}