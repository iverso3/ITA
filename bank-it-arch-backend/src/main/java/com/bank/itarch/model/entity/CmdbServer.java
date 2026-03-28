package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cmdb_server")
public class CmdbServer extends BaseEntity {
    private String assetCode;
    private String hostname;
    private String ipAddress;
    private String innerIp;
    private String cpu;
    private Integer cpuCount;
    private String memory;
    private Integer memorySize;
    private String disk;
    private Integer diskSize;
    private String os;
    private String osVersion;
    private String serverType;
    private String status;
    private Long departmentId;
    private String departmentName;
    private Long teamId;
    private String teamName;
    private String idc;
    private String cabinet;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private String purchaseDate;
    private String warrantyExpire;
    private String remark;
    private String extraAttrs;
}
