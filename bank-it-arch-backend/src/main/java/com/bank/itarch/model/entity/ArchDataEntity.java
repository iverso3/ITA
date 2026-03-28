package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("arch_data_entity")
public class ArchDataEntity extends BaseEntity {
    private Long appId;
    private String entityCode;
    private String entityName;
    private String entityType;
    private String dbType;
    private String dbName;
    private String tableName;
    private String sensitivity;
    private String description;
    private Integer fieldCount;
    private Long recordCount;
    private Long dataVolume;
    private String status;
}
