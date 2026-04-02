-- Migration: Add security level, service info, and ops info fields
-- Version: 1.7
-- Description: Add security level, service info, and ops info fields for application detail page

-- ==================== 安全等级信息 ====================

-- 所属系统等级保护安全等级
ALTER TABLE arch_application ADD COLUMN system_protection_level VARCHAR(32) COMMENT '所属系统等级保护安全等级' AFTER xc_overall_desc;

-- 等级保护安全等级
ALTER TABLE arch_application ADD COLUMN protection_level VARCHAR(32) COMMENT '等级保护安全等级' AFTER system_protection_level;

-- ==================== 服务信息 ====================

-- 客户类型（行内客户/外部客户）
ALTER TABLE arch_application ADD COLUMN customer_type VARCHAR(32) COMMENT '客户类型' AFTER protection_level;

-- 服务时间类型（7*24/5*8等）
ALTER TABLE arch_application ADD COLUMN service_time_type VARCHAR(32) COMMENT '服务时间类型' AFTER customer_type;

-- 服务窗口补充说明
ALTER TABLE arch_application ADD COLUMN service_window_desc VARCHAR(256) COMMENT '服务窗口补充说明' AFTER service_time_type;

-- 内部用户范围
ALTER TABLE arch_application ADD COLUMN internal_user_scope VARCHAR(128) COMMENT '内部用户范围' AFTER service_window_desc;

-- 使用范围补充说明
ALTER TABLE arch_application ADD COLUMN usage_scope_desc VARCHAR(512) COMMENT '使用范围补充说明' AFTER internal_user_scope;

-- ==================== 运维信息 ====================

-- 同城RPO
ALTER TABLE arch_application ADD COLUMN city_rpo VARCHAR(32) COMMENT '同城RPO' AFTER usage_scope_desc;

-- 同城RTO
ALTER TABLE arch_application ADD COLUMN city_rto VARCHAR(32) COMMENT '同城RTO' AFTER city_rpo;

-- 同城双活类型
ALTER TABLE arch_application ADD COLUMN city_active_type VARCHAR(32) COMMENT '同城双活类型' AFTER city_rto;

-- 是否具备同城环境
ALTER TABLE arch_application ADD COLUMN has_city_environment TINYINT DEFAULT 0 COMMENT '是否具备同城环境' AFTER city_active_type;

-- 异地RPO
ALTER TABLE arch_application ADD COLUMN remote_rpo VARCHAR(32) COMMENT '异地RPO' AFTER has_city_environment;

-- 异地RTO
ALTER TABLE arch_application ADD COLUMN remote_rto VARCHAR(32) COMMENT '异地RTO' AFTER remote_rpo;

-- 异地双活类型
ALTER TABLE arch_application ADD COLUMN remote_active_type VARCHAR(32) COMMENT '异地双活类型' AFTER remote_rto;

-- 是否具备灾备环境
ALTER TABLE arch_application ADD COLUMN has_dr_environment TINYINT DEFAULT 0 COMMENT '是否具备灾备环境' AFTER remote_active_type;

-- 运维等级
ALTER TABLE arch_application ADD COLUMN ops_level VARCHAR(16) COMMENT '运维等级' AFTER has_dr_environment;

-- 旧运维等级
ALTER TABLE arch_application ADD COLUMN old_ops_level VARCHAR(16) COMMENT '旧运维等级' AFTER ops_level;

-- 运维单位
ALTER TABLE arch_application ADD COLUMN ops_unit VARCHAR(128) COMMENT '运维单位' AFTER old_ops_level;

-- 远程访问权限分类
ALTER TABLE arch_application ADD COLUMN remote_access_class VARCHAR(16) COMMENT '远程访问权限分类' AFTER ops_unit;

-- 是否变更自动化
ALTER TABLE arch_application ADD COLUMN is_change_automation TINYINT DEFAULT 0 COMMENT '是否变更自动化' AFTER remote_access_class;

-- 变更投产时点
ALTER TABLE arch_application ADD COLUMN change_deploy_time VARCHAR(64) COMMENT '变更投产时点' AFTER is_change_automation;

-- 变更投产时点补充说明
ALTER TABLE arch_application ADD COLUMN change_deploy_time_desc VARCHAR(256) COMMENT '变更投产时点补充说明' AFTER change_deploy_time;

-- 主要业务时段
ALTER TABLE arch_application ADD COLUMN main_business_hours VARCHAR(64) COMMENT '主要业务时段' AFTER change_deploy_time_desc;

-- 电子数据资产提取审批部门
ALTER TABLE arch_application ADD COLUMN data_asset_approval_dept VARCHAR(256) COMMENT '电子数据资产提取审批部门' AFTER main_business_hours;

-- 是否容器化部署
ALTER TABLE arch_application ADD COLUMN is_containerized TINYINT DEFAULT 0 COMMENT '是否容器化部署' AFTER data_asset_approval_dept;

-- 部署环境
ALTER TABLE arch_application ADD COLUMN deployment_environment VARCHAR(64) COMMENT '部署环境' AFTER is_containerized;

-- 部署地点
ALTER TABLE arch_application ADD COLUMN deployment_location VARCHAR(128) COMMENT '部署地点' AFTER deployment_environment;

-- 部署地点补充说明
ALTER TABLE arch_application ADD COLUMN deployment_location_desc VARCHAR(512) COMMENT '部署地点补充说明' AFTER deployment_location;

-- 灾备等级
ALTER TABLE arch_application ADD COLUMN dr_level VARCHAR(16) COMMENT '灾备等级' AFTER deployment_location_desc;

-- 灾备恢复能力等级
ALTER TABLE arch_application ADD COLUMN dr_recovery_level VARCHAR(16) COMMENT '灾备恢复能力等级' AFTER dr_level;
