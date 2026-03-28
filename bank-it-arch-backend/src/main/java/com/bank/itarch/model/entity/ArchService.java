package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("arch_service")
public class ArchService extends BaseEntity {
    private Long appId;
    private Long moduleId;
    private String serviceCode;
    private String serviceName;
    private String serviceType;
    private String protocol;
    private Integer port;
    private String contextPath;
    private String version;
    private String description;
    private String status;
    private Long teamId;
    private String teamName;
}
