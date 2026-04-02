package com.bank.itarch.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.bank.itarch.common.PageQuery;

@Data
@EqualsAndHashCode(callSuper = true)
public class OssSoftwareBaselineQueryDTO extends PageQuery {
    /** 开源软件名称 (支持精确/模糊) */
    private String swName;

    /** 是否精确匹配软件名称 */
    private Boolean exactMatch = false;

    /** 开源软件版本 (模糊查询) */
    private String swVersion;

    /** 软件分类 */
    private String swCategory;

    /** 软件类型 */
    private String swType;

    /** 版本类型 */
    private String verType;

    /** 是否主推使用版本 */
    private String isMainUse;

    /** 开源许可证 */
    private String licAbbr;

    /** 引入团队ID */
    private String implTeamId;
}
