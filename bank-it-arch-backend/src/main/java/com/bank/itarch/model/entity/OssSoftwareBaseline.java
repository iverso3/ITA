package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("oss_software_baseline")
public class OssSoftwareBaseline implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 软件id(关联oss_software_info) */
    private String swId;

    /** 软件名称 */
    private String swName;

    /** 软件版本 */
    private String swVersion;

    /** 软件分类: BASE-开源基础软件, TOOL-开源工具软件, CMPNT-开源组件 */
    private String swCategory;

    /** 软件类型: MAIN-主推软件, LIMIT-非主推软件, QUIT-已退出软件 */
    private String swType;

    /** 版本类型: WHITELIST-白名单, GREYLIST-灰名单, BLACKLIST-黑名单 */
    private String verType;

    /** 引入团队id */
    private String implTeamId;

    /** 引入团队名称 */
    private String implTeamName;

    /** 引入人id */
    private String implUserId;

    /** 引入人姓名 */
    private String implUserName;

    /** 引入申请编号 */
    private String implApplyNo;

    /** 许可证id */
    private String licId;

    /** 许可证简称 */
    private String licAbbr;

    /** 最后评估时间 */
    private LocalDateTime lastEvalDatetime;

    /** 最后评估人id */
    private String lastEvalId;

    /** 生效时间 */
    private LocalDateTime effectDatetime;

    /** 失效时间 */
    private LocalDateTime expireDatetime;

    /** 同步时间 */
    private LocalDateTime syncDatetime;

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

    /** 使用机构id */
    private String useBranchId;

    /** 使用机构名称 */
    private String useBranchName;

    /** 备注 */
    private String remark;

    /** 介质地址(JSON) */
    private String mediaAddr;

    /** 是否主推荐使用版本: 0-否, 1-是 */
    private String isMainUse;

    /** 适用场景: ALL_SCENE-全场景适用, INNER_SCENE-限定内部使用 */
    private String applicableScene;

    /** 适用职能范围: ALL_BANK_USE-全行使用, ONLY_BRANCH_USE-仅分行使用 */
    private String applicableFunctionRange;

    /** 开源软件全称 */
    private String swFullName;

    /** 组件依赖类型 */
    private String dependType;

    /** 数据来源标识: 0-人工引入, 1-组件自动同步 */
    private String dataSrcFlag;

    /** 批次日期 */
    private String batchDate;
}
