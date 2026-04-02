package com.bank.itarch.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.bank.itarch.common.PageQuery;

@Data
@EqualsAndHashCode(callSuper = true)
public class OssImplApplyQueryDTO extends PageQuery {
    /** 申请单号 */
    private String implApplyNo;

    /** 申请类型: 0-首次引入, 1-新版本引入 */
    private String implApplyType;

    /** 软件分类 */
    private String swCategory;

    /** 开源软件名称 */
    private String swName;

    /** 开源软件ID */
    private String swId;

    /** 流程实例ID */
    private Long procInstId;

    /** 申请人ID */
    private String implUserId;

    /** 申请团队ID */
    private String implTeamId;
}
