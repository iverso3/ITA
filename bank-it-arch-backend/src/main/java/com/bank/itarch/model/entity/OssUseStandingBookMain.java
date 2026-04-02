package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("oss_use_standing_book_main_info")
public class OssUseStandingBookMain implements Serializable {
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

    /** 所属环境: PROD-生产环境; DEVTEST-开发测试环境 */
    private String environment;

    /** 扫描日期 */
    private LocalDate scanDate;

    /** 软件分类: 开源组件框架/开源基础软件 */
    private String swCategory;

    /** 应用编号 */
    private String appNo;

    /** 应用全称 */
    private String appName;

    /** 同步时间（两地三中心） */
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

    /** 是否普遍（是否商用） */
    private String isCommerc;
}