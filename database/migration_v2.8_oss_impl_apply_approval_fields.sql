-- Migration: OSS Implementation Apply Approval Supplementary Fields
-- Version: 2.8
-- Description: Add fields for open source software administrator to supplement during approval

USE bank_it_arch;

-- oss_impl_apply_info 表新增字段 - 用于存储开源软件管理员审批时补充的信息
ALTER TABLE oss_impl_apply_info
ADD COLUMN `applicable_scene` VARCHAR(32) DEFAULT NULL COMMENT '适用场景：ALL_SCENE-全场景适用, INNER_SCENE-限定内部使用' AFTER `sw_category`,
ADD COLUMN `applicable_function_range` VARCHAR(32) DEFAULT NULL COMMENT '适用职能范围：ALL_BANK_USE-全行使用, ONLY_BRANCH_USE-仅分行使用' AFTER `applicable_scene`,
ADD COLUMN `is_main_use` VARCHAR(8) DEFAULT '0' COMMENT '是否主推荐使用：0-否, 1-是' AFTER `applicable_function_range`,
ADD COLUMN `ver_type` VARCHAR(32) DEFAULT NULL COMMENT '版本类型：WHITELIST-白名单, GREYLIST-灰名单, BLACKLIST-黑名单' AFTER `is_main_use`,
ADD COLUMN `product_type` VARCHAR(64) DEFAULT NULL COMMENT '产品类型：OS_DB-操作系统数据库, MIDDLEWARE-中间件, AI-人工智能, CLOUD-云计算' AFTER `ver_type`,
ADD COLUMN `application_scene` VARCHAR(1024) DEFAULT NULL COMMENT '应用场景描述，最大1024字符' AFTER `product_type`;

-- 添加索引
ALTER TABLE oss_impl_apply_info
ADD INDEX `idx_applicable_scene` (`applicable_scene`),
ADD INDEX `idx_ver_type` (`ver_type`);
