-- Migration: OSS Implementation Application
-- Version: 2.0
-- Description: Add OSS implementation application tables for open source software introduction approval workflow

USE bank_it_arch;

-- ============================================
-- 1. 开源软件引入申请主表 oss_impl_apply_info
-- ============================================
CREATE TABLE IF NOT EXISTS `oss_impl_apply_info` (
    `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
    `impl_apply_no` VARCHAR(32) NOT NULL COMMENT '申请单号',
    `flow_title` VARCHAR(64) DEFAULT NULL COMMENT '流程标题',
    `impl_apply_type` VARCHAR(8) NOT NULL DEFAULT '1' COMMENT '申请类型: 0-首次引入, 1-新版本引入',
    `sw_id` VARCHAR(32) DEFAULT NULL COMMENT '开源软件ID(关联oss_software_info)',
    `sw_name` VARCHAR(64) DEFAULT NULL COMMENT '开源软件名称',
    `sw_version` VARCHAR(24) DEFAULT NULL COMMENT '开源软件版本',
    `sw_type` VARCHAR(12) DEFAULT NULL COMMENT '软件类型: MAIN-主推, LIMIT-限制使用, QUIT-已退出',
    `rsp_user_id` VARCHAR(32) DEFAULT NULL COMMENT '责任人ID',
    `rsp_user_name` VARCHAR(100) DEFAULT NULL COMMENT '责任人名称',
    `rsp_team_id` VARCHAR(32) DEFAULT NULL COMMENT '责任团队ID',
    `rsp_team_name` VARCHAR(128) DEFAULT NULL COMMENT '责任团队名称',
    `sw_category` VARCHAR(12) DEFAULT NULL COMMENT '软件分类: BASE-开源基础软件, TOOL-开源工具软件, CMPNT-开源组件',
    `impl_team_id` VARCHAR(32) DEFAULT NULL COMMENT '引入团队ID',
    `impl_team_name` VARCHAR(128) DEFAULT NULL COMMENT '引入团队名称',
    `impl_user_id` VARCHAR(32) DEFAULT NULL COMMENT '引入人ID',
    `impl_user_name` VARCHAR(100) DEFAULT NULL COMMENT '引入人名称',
    `lic_id` VARCHAR(256) DEFAULT NULL COMMENT '开源许可协议ID',
    `lic_abbr` VARCHAR(256) DEFAULT NULL COMMENT '开源许可协议简称',
    `use_branch_id` VARCHAR(32) DEFAULT NULL COMMENT '软件使用机构ID',
    `use_branch_name` VARCHAR(128) DEFAULT NULL COMMENT '软件使用机构名称',
    `sec_instrt` VARCHAR(8) DEFAULT '0' COMMENT '是否安全工具相关: 0-否, 1-是',
    `os_type` VARCHAR(64) DEFAULT NULL COMMENT '操作系统类型: 0-Linux, 1-Windows, 2-AIX等',
    `os_version` VARCHAR(64) DEFAULT NULL COMMENT '操作系统版本',
    `os_digit` VARCHAR(32) DEFAULT NULL COMMENT '操作系统位数',
    `apply_team_id` VARCHAR(32) DEFAULT NULL COMMENT '申请研发团队ID',
    `apply_team_name` VARCHAR(128) DEFAULT NULL COMMENT '申请研发团队名称',
    `use_app_no` VARCHAR(32) DEFAULT NULL COMMENT '应用编号',
    `launch_version` VARCHAR(32) DEFAULT NULL COMMENT '投产版本',
    `launch_task_info` VARCHAR(100) DEFAULT NULL COMMENT '任务编号及名称',
    `impl_cmnt` VARCHAR(200) DEFAULT NULL COMMENT '申请说明',
    `contact_user_id` VARCHAR(32) DEFAULT NULL COMMENT '联系人ID',
    `contact_user_name` VARCHAR(100) DEFAULT NULL COMMENT '联系人名称',
    `contact_tel_no` VARCHAR(32) DEFAULT NULL COMMENT '联系人电话',
    `eval_background` TEXT DEFAULT NULL COMMENT '评审背景',
    `system_env` TEXT DEFAULT NULL COMMENT '系统环境',
    `function_intro` TEXT DEFAULT NULL COMMENT '功能介绍',
    `eval_conclusion` TEXT DEFAULT NULL COMMENT '评审结论',
    `approve_datetime` DATETIME DEFAULT NULL COMMENT '审批时间',
    `proc_inst_id` BIGINT DEFAULT NULL COMMENT '流程实例ID(关联wf_instance)',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `sync_datetime` DATETIME DEFAULT NULL COMMENT '同步时间',
    `create_mode` INT(10) NOT NULL DEFAULT 0 COMMENT '创建方式: 0-本系统创建, 1-异地同步',
    `create_user_id` VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    `create_datetime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id` VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    `update_datetime` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `logic_status` INT(10) NOT NULL DEFAULT 0 COMMENT '逻辑状态: 0-正常, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_impl_apply_no` (`impl_apply_no`),
    KEY `idx_impl_apply_type` (`impl_apply_type`),
    KEY `idx_sw_category` (`sw_category`),
    KEY `idx_sw_id` (`sw_id`),
    KEY `idx_sw_name` (`sw_name`),
    KEY `idx_proc_inst_id` (`proc_inst_id`),
    KEY `idx_logic_status` (`logic_status`),
    KEY `idx_create_datetime` (`create_datetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='开源软件引入申请表';

-- ============================================
-- 2. 开源软件引入申请拓展表 oss_impl_apply_supl
-- ============================================
CREATE TABLE IF NOT EXISTS `oss_impl_apply_supl` (
    `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
    `impl_apply_no` VARCHAR(32) NOT NULL COMMENT '申请单号',
    `eval_info_list_json` JSON DEFAULT NULL COMMENT '评测信息列表json',
    `eval_result_list_json` JSON DEFAULT NULL COMMENT '评测结果列表json',
    `eval_score_list_json` JSON DEFAULT NULL COMMENT '评测得分列表json',
    `eval_summ_list_json` JSON DEFAULT NULL COMMENT '评测总结列表json',
    `eval_atch_list_json` JSON DEFAULT NULL COMMENT '评测附件json',
    `media_pre_whs_url` JSON DEFAULT NULL COMMENT '介质前置仓库地址json',
    `proc_inst_id` BIGINT DEFAULT NULL COMMENT '流程实例ID',
    `sync_datetime` DATETIME DEFAULT NULL COMMENT '同步时间',
    `create_mode` INT(10) NOT NULL DEFAULT 0 COMMENT '创建方式: 0-本系统创建, 1-异地同步',
    `create_user_id` VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    `create_datetime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id` VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    `update_datetime` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `logic_status` INT(10) NOT NULL DEFAULT 0 COMMENT '逻辑状态: 0-正常, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_impl_apply_no` (`impl_apply_no`),
    KEY `idx_proc_inst_id` (`proc_inst_id`),
    KEY `idx_logic_status` (`logic_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='开源软件引入申请拓展表';
