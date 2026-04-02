package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("oss_flow_role")
public class OssFlowRole {

    @TableId(value = "flow_role_id", type = IdType.ASSIGN_UUID)
    private String flowRoleId;

    private String flowRoleName;

    private String flowRoleDesc;

    private Integer flowRowSeq;

    private String bindBranchFlag;

    private String bindTeamFlag;

    private String bindAppFlag;

    private LocalDateTime syncDatetime;

    private Integer createMode;

    @TableField(fill = FieldFill.INSERT)
    private String createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDatetime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private String logicStatus;

    private String flowRoleType;

    private String checkBranchFlag;

    private String bindInstitutionFlag;
}