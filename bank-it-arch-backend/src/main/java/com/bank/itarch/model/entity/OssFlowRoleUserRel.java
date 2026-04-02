package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("oss_flow_role_user_rel")
public class OssFlowRoleUserRel {

    @TableId(value = "rel_id", type = IdType.ASSIGN_UUID)
    private String relId;

    private String flowRoleId;

    private String userId;

    private String branchId;

    private String teamId;

    private LocalDateTime syncDatetime;

    private Integer createMode;

    @TableField(fill = FieldFill.INSERT)
    private String createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDatetime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateDatetime;

    @TableField(fill = FieldFill.INSERT)
    private String logicStatus;

    private String userName;

    private String flowRoleName;

    private String branchName;

    private String teamName;

    private String flowRoleType;

    private String orgId;

    private String orgName;
}