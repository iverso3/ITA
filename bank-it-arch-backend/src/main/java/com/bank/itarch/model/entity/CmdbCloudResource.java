package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cmdb_cloud_resource")
public class CmdbCloudResource extends BaseEntity {
    private String resourceCode;
    private String resourceName;
    private String resourceType;
    private String cloudProvider;
    private String region;
    private String zone;
    private String specification;
    private String status;
    private String ipAddress;
    private String innerIp;
    private Integer cpu;
    private Integer memory;
    private Integer diskSize;
    private Integer bandwidth;
    private Long departmentId;
    private String departmentName;
    private Long teamId;
    private String teamName;
    private String costPerMonth;
    private String expireDate;
    private String remark;
    private String extraAttrs;
}
