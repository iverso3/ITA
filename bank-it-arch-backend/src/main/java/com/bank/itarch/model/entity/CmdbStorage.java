package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cmdb_storage")
public class CmdbStorage extends BaseEntity {
    private String assetCode;
    private String deviceName;
    private String deviceType;
    private Long totalCapacity;
    private Long usedCapacity;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private String status;
    private Long departmentId;
    private String departmentName;
    private Long teamId;
    private String teamName;
    private String idc;
    private String purchaseDate;
    private String warrantyExpire;
    private String remark;
    private String extraAttrs;
}
