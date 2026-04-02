package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class OssSoftwareMediaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 版本记录ID (baseline id) */
    private String baselineId;

    /** 软件ID */
    private String swId;

    /** 开源软件名称 */
    private String swName;

    /** 软件版本 */
    private String swVersion;

    /** 软件分类 */
    private String swCategory;

    /** 版本类型 */
    private String verType;

    /** 是否主推使用版本 */
    private String isMainUse;

    /** 主推荐使用版本 (来自软件清单) */
    private String recommendedVersion;

    /** 介质类型 (如: linux, windows) */
    private String mediaType;

    /** 介质名称 (文件名) */
    private String mediaName;

    /** 下载地址 */
    private String mediaUrl;
}
