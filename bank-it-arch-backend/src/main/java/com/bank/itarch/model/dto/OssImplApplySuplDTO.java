package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OssImplApplySuplDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    /** 申请单号 */
    private String implApplyNo;

    /** 评测信息列表json */
    private String evalInfoListJson;

    /** 评测结果列表json */
    private String evalResultListJson;

    /** 评测得分列表json */
    private String evalScoreListJson;

    /** 评测总结列表json */
    private String evalSummListJson;

    /** 评测附件json */
    private String evalAtchListJson;

    /** 介质前置仓库地址json */
    private String mediaPreWhsUrl;

    /** 流程实例ID */
    private Long procInstId;

    /** 同步时间 */
    private LocalDateTime syncDatetime;

    /** 创建方式: 0-本系统创建, 1-异地同步 */
    private Integer createMode;

    /** 创建者 */
    private String createUserId;

    /** 创建时间 */
    private LocalDateTime createDatetime;

    /** 更新者 */
    private String updateUserId;

    /** 更新时间 */
    private LocalDateTime updateDatetime;

    // ========== 审批时补充的字段（存储到 oss_impl_apply_info 表）==========

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
}
