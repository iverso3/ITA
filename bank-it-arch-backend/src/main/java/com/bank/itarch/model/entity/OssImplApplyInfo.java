package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("oss_impl_apply_info")
public class OssImplApplyInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 申请单号 */
    private String implApplyNo;

    /** 流程标题 */
    private String flowTitle;

    /** 申请类型: 0-首次引入, 1-新版本引入 */
    private String implApplyType;

    /** 开源软件ID(关联oss_software_info) */
    private String swId;

    /** 开源软件名称 */
    private String swName;

    /** 开源软件版本 */
    private String swVersion;

    /** 软件类型: MAIN-主推, LIMIT-限制使用, QUIT-已退出 */
    private String swType;

    /** 责任人ID */
    private String rspUserId;

    /** 责任人名称 */
    private String rspUserName;

    /** 责任团队ID */
    private String rspTeamId;

    /** 责任团队名称 */
    private String rspTeamName;

    /** 软件分类: BASE-开源基础软件, TOOL-开源工具软件, CMPNT-开源组件 */
    private String swCategory;

    /** 适用场景：ALL_SCENE-全场景适用, INNER_SCENE-限定内部使用 */
    private String applicableScene;

    /** 适用职能范围：ALL_BANK_USE-全行使用, ONLY_BRANCH_USE-仅分行使用 */
    private String applicableFunctionRange;

    /** 是否主推荐使用：0-否, 1-是 */
    private String isMainUse;

    /** 版本类型：WHITELIST-白名单, GREYLIST-灰名单, BLACKLIST-黑名单 */
    private String verType;

    /** 产品类型：OS_DB-操作系统数据库, MIDDLEWARE-中间件, AI-人工智能, CLOUD-云计算 */
    private String productType;

    /** 应用场景描述，最大1024字符 */
    private String applicationScene;

    /** 引入团队ID */
    private String implTeamId;

    /** 引入团队名称 */
    private String implTeamName;

    /** 引入人ID */
    private String implUserId;

    /** 引入人名称 */
    private String implUserName;

    /** 开源许可协议ID */
    private String licId;

    /** 开源许可协议简称 */
    private String licAbbr;

    /** 软件使用机构ID */
    private String useBranchId;

    /** 软件使用机构名称 */
    private String useBranchName;

    /** 是否安全工具相关: 0-否, 1-是 */
    private String secInstrt;

    /** 操作系统类型: 0-Linux, 1-Windows, 2-AIX等 */
    private String osType;

    /** 操作系统版本 */
    private String osVersion;

    /** 操作系统位数 */
    private String osDigit;

    /** 申请研发团队ID */
    private String applyTeamId;

    /** 申请研发团队名称 */
    private String applyTeamName;

    /** 应用编号 */
    private String useAppNo;

    /** 投产版本 */
    private String launchVersion;

    /** 任务编号及名称 */
    private String launchTaskInfo;

    /** 申请说明 */
    private String implCmnt;

    /** 联系人ID */
    private String contactUserId;

    /** 联系人名称 */
    private String contactUserName;

    /** 联系人电话 */
    private String contactTelNo;

    /** 评审背景 */
    private String evalBackground;

    /** 系统环境 */
    private String systemEnv;

    /** 功能介绍 */
    private String functionIntro;

    /** 评审结论 */
    private String evalConclusion;

    /** 审批时间 */
    private LocalDateTime approveDatetime;

    /** 流程实例ID(关联wf_instance) */
    private Long procInstId;

    /** 备注 */
    private String remark;

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
}
