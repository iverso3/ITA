package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cmdb_asset_change_log")
public class CmdbAssetChangeLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String assetType;
    private Long assetId;
    private String assetCode;
    private String operateType;
    private String beforeValue;
    private String afterValue;
    private String operator;
    private LocalDateTime operateTime;
    private String operateIp;
    private String remark;
}
