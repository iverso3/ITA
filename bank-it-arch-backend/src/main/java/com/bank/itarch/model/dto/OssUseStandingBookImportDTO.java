package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OssUseStandingBookImportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String swName;                     // 开源软件名称
    private String swVersion;                  // 开源软件版本
    private String swFullName;                 // 开源软件全称
    private String licAbbr;                    // 开源许可证集合
    private String environment;                // 所属环境
    private String swCategory;                 // 软件分类
    private String appNo;                      // 应用编号
    private String appName;                    // 应用全称
    private String installPath;                // 项目路径/安装路径
    private Integer source;                     // 台账来源
    private LocalDateTime scanTime;            // 扫描时间
    private String ipOrHostName;               // IP/主机名称
    private String command;                    // 软件启动命令
    private String detailedInfo;               // 版本详细信息
    private String fileType;                   // 组件文件类型
    private String dependType;                 // 组件依赖类型
    private String isCommerc;                  // 是否商用
    private String commercProductName;         // 产品名称
    private String commercProductVersion;       // 产品版本
    private String projectName;                // 项目名
}