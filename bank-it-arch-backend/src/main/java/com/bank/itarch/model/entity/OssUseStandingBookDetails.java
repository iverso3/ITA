package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("oss_use_standing_book_details_info")
public class OssUseStandingBookDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 开源软件名称 */
    private String swName;

    /** 开源软件版本 */
    private String swVersion;

    /** 开源软件全称 */
    private String swFullName;

    /** 开源许可证集合 */
    private String licAbbr;

    /** 所属环境: PROD/DEVTEST */
    private String environment;

    /** 扫描时间 */
    private LocalDateTime scanTime;

    /** 软件分类 */
    private String swCategory;

    /** 应用编号 */
    private String appNo;

    /** 应用全称 */
    private String appName;

    /** 父编号(Main表ID) */
    private String parentId;

    /** 项目路径/安装路径 */
    private String installPath;

    /** 台账来源: 0-系统同步; 1-手工上传 */
    private Integer source;

    /** IP/主机名称 */
    private String ipOrHostName;

    /** 软件启动命令 */
    private String command;

    /** 版本详细信息 */
    private String detailedInfo;

    /** 组件文件类型: tgz/jar */
    private String fileType;

    /** 组件依赖类型: maven/npm/go/python */
    private String dependType;

    /** 同步时间 */
    private LocalDateTime syncDatetime;

    /** 创建方式: 0-本系统创建; 1-异地同步 */
    private Integer createMode;

    /** 创建者 */
    private String createUserId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDatetime;

    /** 更新者 */
    private String updateUserId;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateDatetime;

    /** 逻辑状态: 0-正常; 1-逻辑删除 */
    @TableLogic
    @TableField(select = false)
    private Integer logicStatus;

    /** 是否普遍 */
    private String isCommerc;

    /** 产品名称 */
    private String commercProductName;

    /** 产品版本 */
    private String commercProductVersion;

    /** 项目名 */
    private String projectName;
}