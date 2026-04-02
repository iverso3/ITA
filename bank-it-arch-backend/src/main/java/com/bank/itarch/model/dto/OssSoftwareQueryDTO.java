package com.bank.itarch.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.bank.itarch.common.PageQuery;

@Data
@EqualsAndHashCode(callSuper = true)
public class OssSoftwareQueryDTO extends PageQuery {
    /** 开源软件名称 (模糊查询) */
    private String swName;

    /** 软件分类 */
    private String swCategory;

    /** 软件类型 */
    private String swType;

    /** 责任团队ID */
    private String rspTeamId;

    /** 责任人姓名 (模糊查询) */
    private String rspUserName;
}
