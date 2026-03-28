package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cmdb_network")
public class CmdbNetwork extends BaseEntity {
    private String assetCode;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private String mgmtVlan;
    private Integer portCount;
    private Integer usedPortCount;
    private String bandwidth;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private String status;
    private Long departmentId;
    private String departmentName;
    private Long teamId;
    private String teamName;
    private String idc;
    private String cabinet;
    private String purchaseDate;
    private String warrantyExpire;
    private String remark;
    private String extraAttrs;
}
