-- Migration: Add application detail fields
-- Version: 1.4
-- Description: Add missing fields for bank application detail page

-- 系统所属层
ALTER TABLE arch_application ADD COLUMN system_layer VARCHAR(32) COMMENT '系统所属层（总行级/分行级）' AFTER importance_level;

-- 实施处室
ALTER TABLE arch_application ADD COLUMN implementation_division VARCHAR(128) COMMENT '实施处室' AFTER department_name;

-- 应用所属系统（上级应用）
ALTER TABLE arch_application ADD COLUMN parent_app_id BIGINT COMMENT '所属上级应用ID' AFTER team_name;

-- 实施单位
ALTER TABLE arch_application ADD COLUMN implementation_unit VARCHAR(128) COMMENT '实施单位' AFTER parent_app_id;

-- 实施项目组
ALTER TABLE arch_application ADD COLUMN implementation_team VARCHAR(128) COMMENT '实施项目组' AFTER implementation_unit;

-- 主业务功能领域
ALTER TABLE arch_application ADD COLUMN main_business_domain VARCHAR(128) COMMENT '主业务功能领域' AFTER business_description;

-- 辅业务功能领域
ALTER TABLE arch_application ADD COLUMN secondary_business_domain VARCHAR(128) COMMENT '辅业务功能领域' AFTER main_business_domain;

-- 是否互联网应用
ALTER TABLE arch_application ADD COLUMN is_internet_app TINYINT DEFAULT 0 COMMENT '是否互联网应用（0-否，1-是）' AFTER secondary_business_domain;

-- 是否支付应用
ALTER TABLE arch_application ADD COLUMN is_payment_app TINYINT DEFAULT 0 COMMENT '是否支付应用' AFTER is_internet_app;

-- 是否网上银行应用
ALTER TABLE arch_application ADD COLUMN is_online_banking_app TINYINT DEFAULT 0 COMMENT '是否网上银行应用' AFTER is_payment_app;

-- 是否票据应用
ALTER TABLE arch_application ADD COLUMN is_bill_app TINYINT DEFAULT 0 COMMENT '是否票据应用' AFTER is_online_banking_app;

-- 服务对象
ALTER TABLE arch_application ADD COLUMN service_object VARCHAR(128) COMMENT '服务对象' AFTER is_bill_app;

-- 是否电子银行应用
ALTER TABLE arch_application ADD COLUMN is_electronic_banking_app TINYINT DEFAULT 0 COMMENT '是否电子银行应用' AFTER service_object;

-- 是否移动应用
ALTER TABLE arch_application ADD COLUMN is_mobile_app TINYINT DEFAULT 0 COMMENT '是否移动应用' AFTER is_electronic_banking_app;

-- 是否互联网金融应用
ALTER TABLE arch_application ADD COLUMN is_internet_finance_app TINYINT DEFAULT 0 COMMENT '是否互联网金融应用' AFTER is_mobile_app;

-- Add foreign key for parent_app_id
ALTER TABLE arch_application ADD CONSTRAINT fk_parent_app_id FOREIGN KEY (parent_app_id) REFERENCES arch_application(id);
