package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("oss_software_info")
public class OssSoftware implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 软件名称 */
    private String swName;

    /** 软件分类: BASE-开源基础软件, TOOL-开源工具软件, CMPNT-开源组件 */
    private String swCategory;

    /** 软件类型: MAIN-主推软件, LIMIT-非主推软件, QUIT-已退出软件 */
    private String swType;

    /** 责任团队ID */
    private String rspTeamId;

    /** 责任团队名称 */
    private String rspTeamName;

    /** 责任人ID */
    private String rspUserId;

    /** 责任人姓名 */
    private String rspUserName;

    /** 产品类型 */
    private String productType;

    /** 主推荐使用版本 */
    private String recommendedVersion;

    /** 应用场景 */
    private String applicationScene;

    /** 数据来源: 0-人工引入, 1-异地同步 */
    private String dataSrcFlag;

    /** 同步时间 */
    private LocalDateTime syncDatetime;

    /** 批次日期 */
    private String batchDate;

    /** 创建方式: 0-本系统创建, 1-异地同步 */
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

    /** 逻辑状态: 0-正常, 1-已删除 */
    @TableLogic
    @TableField(select = false)
    private Integer logicStatus;
}
