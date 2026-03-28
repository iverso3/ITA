package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String module;
    private String operation;
    private String operationType;
    private String requestMethod;
    private String requestUrl;
    private String requestParams;
    private String requestBody;
    private String response;
    private Long operatorId;
    private String operatorName;
    private String operatorIp;
    private String operatorLocation;
    private Integer executeTime;
    private LocalDateTime createTime;
}
