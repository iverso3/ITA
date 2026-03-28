package com.bank.itarch.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_team")
public class SysTeam extends BaseEntity {
    private Long departmentId;
    private String teamCode;
    private String teamName;
    private Long leaderId;
    private String leaderName;
    private Integer sortOrder;
    private Integer isActive;
}
