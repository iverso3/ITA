package com.bank.itarch.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.bank.itarch.common.PageQuery;

@Data
@EqualsAndHashCode(callSuper = true)
public class OssSoftwareMediaQueryDTO extends PageQuery {
    /** 开源软件名称 */
    private String swName;

    /** 开源软件版本 */
    private String swVersion;

    /** 版本类型 */
    private String verType;

    /** 是否主推使用版本 */
    private String isMainUse;

    /** 软件分类 */
    private String swCategory;
}
