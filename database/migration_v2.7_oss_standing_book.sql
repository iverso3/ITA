-- Migration v2.7: OSS Usage Standing Book Tables
-- 银行IT架构管理平台 - 开源软件使用台账管理

-- ============================================
-- Table: oss_use_standing_book_main_info
-- 开源软件使用台账主表
-- ============================================
CREATE TABLE `oss_use_standing_book_main_info` (
    `id` VARCHAR(64) NOT NULL PRIMARY KEY COMMENT '主键ID',
    `sw_name` VARCHAR(256) COMMENT '开源软件名称',
    `sw_version` VARCHAR(256) COMMENT '开源软件版本',
    `sw_full_name` VARCHAR(512) COMMENT '开源软件全称',
    `lic_abbr` VARCHAR(500) COMMENT '开源许可证集合',
    `environment` VARCHAR(12) COMMENT '所属环境: PROD-生产环境; DEVTEST-开发测试环境',
    `scan_date` DATE COMMENT '扫描日期',
    `sw_category` VARCHAR(32) COMMENT '软件分类: 开源组件框架/开源基础软件',
    `app_no` VARCHAR(32) COMMENT '应用编号',
    `app_name` VARCHAR(128) COMMENT '应用全称',
    `sync_datetime` DATETIME COMMENT '同步时间（两地三中心）',
    `create_mode` INT(10) NOT NULL DEFAULT 0 COMMENT '创建方式: 0-本系统创建; 1-异地同步',
    `create_user_id` VARCHAR(32) COMMENT '创建者',
    `create_datetime` DATETIME COMMENT '创建时间',
    `update_user_id` VARCHAR(32) COMMENT '更新者',
    `update_datetime` DATETIME COMMENT '更新时间',
    `logic_status` INT(10) NOT NULL DEFAULT 0 COMMENT '逻辑状态: 0-正常; 1-逻辑删除',
    `is_commerc` VARCHAR(10) COMMENT '是否普遍（是否商用）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='开源软件使用台账主表';

-- ============================================
-- Table: oss_use_standing_book_details_info
-- 开源软件使用台账明细表
-- ============================================
CREATE TABLE `oss_use_standing_book_details_info` (
    `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    `sw_name` VARCHAR(256) COMMENT '开源软件名称',
    `sw_version` VARCHAR(256) COMMENT '开源软件版本',
    `sw_full_name` VARCHAR(256) COMMENT '开源软件全称',
    `lic_abbr` VARCHAR(500) COMMENT '开源许可证集合',
    `environment` VARCHAR(12) COMMENT '所属环境: PROD/DEVTEST',
    `scan_time` DATETIME COMMENT '扫描时间',
    `sw_category` VARCHAR(32) COMMENT '软件分类',
    `app_no` VARCHAR(32) COMMENT '应用编号',
    `app_name` VARCHAR(128) COMMENT '应用全称',
    `parent_id` VARCHAR(64) COMMENT '父编号(Main表ID)',
    `install_path` VARCHAR(255) COMMENT '项目路径/安装路径',
    `source` INT(10) COMMENT '台账来源: 0-系统同步; 1-手工上传',
    `ip_or_host_name` VARCHAR(256) COMMENT 'IP/主机名称',
    `command` VARCHAR(256) COMMENT '软件启动命令',
    `detailed_info` VARCHAR(256) COMMENT '版本详细信息',
    `file_type` VARCHAR(32) COMMENT '组件文件类型: tgz/jar',
    `depend_type` VARCHAR(32) COMMENT '组件依赖类型: maven/npm/go/python',
    `sync_datetime` DATETIME COMMENT '同步时间',
    `create_mode` INT(10) NOT NULL DEFAULT 0 COMMENT '创建方式: 0-本系统创建; 1-异地同步',
    `create_user_id` VARCHAR(32) COMMENT '创建者',
    `create_datetime` DATETIME COMMENT '创建时间',
    `update_user_id` VARCHAR(32) COMMENT '更新者',
    `update_datetime` DATETIME COMMENT '更新时间',
    `logic_status` INT(10) NOT NULL DEFAULT 0 COMMENT '逻辑状态: 0-正常; 1-逻辑删除',
    `is_commerc` VARCHAR(10) COMMENT '是否普遍',
    `commerc_product_name` VARCHAR(256) COMMENT '产品名称',
    `commerc_product_version` VARCHAR(256) COMMENT '产品版本',
    `project_name` VARCHAR(64) COMMENT '项目名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='开源软件使用台账明细表';

-- ============================================
-- Test Data: Main Table
-- ============================================
INSERT INTO `oss_use_standing_book_main_info` (`id`, `sw_name`, `sw_version`, `sw_full_name`, `lic_abbr`, `environment`, `scan_date`, `sw_category`, `app_no`, `app_name`, `create_mode`, `logic_status`) VALUES
('MAIN001', 'vue', '3.4.0', 'vue:3.4.0', 'MIT', 'DEVTEST', '2026-03-31', '开源组件框架', 'APP001', '测试应用A', 0, 0),
('MAIN002', 'springboot', '2.7.0', 'springboot:2.7.0', 'Apache-2.0', 'PROD', '2026-03-31', '开源基础软件', 'APP002', '测试应用B', 0, 0);

-- ============================================
-- Test Data: Detail Table (Part 1 - with project info)
-- ============================================
INSERT INTO `oss_use_standing_book_details_info` (`id`, `sw_name`, `sw_version`, `sw_full_name`, `environment`, `scan_time`, `sw_category`, `app_no`, `app_name`, `parent_id`, `install_path`, `source`, `file_type`, `depend_type`, `project_name`, `create_mode`, `logic_status`) VALUES
('DETAIL001', 'vue', '3.4.0', 'vue:3.4.0', 'DEVTEST', '2026-03-31 10:00:00', '开源组件框架', 'APP001', '测试应用A', 'MAIN001', 'project-a/src/main/vue', 0, 'jar', 'maven', 'ProjectA', 0, 0),
('DETAIL002', 'vue', '3.4.0', 'vue:3.4.0', 'DEVTEST', '2026-03-31 11:00:00', '开源组件框架', 'APP001', '测试应用A', 'MAIN001', 'project-b/pom.xml', 0, 'tgz', 'npm', 'ProjectB', 0, 0);

-- ============================================
-- Test Data: Detail Table (Part 2 - with commercial info)
-- ============================================
INSERT INTO `oss_use_standing_book_details_info` (`id`, `sw_name`, `sw_version`, `sw_full_name`, `environment`, `scan_time`, `sw_category`, `app_no`, `app_name`, `parent_id`, `install_path`, `source`, `ip_or_host_name`, `command`, `is_commerc`, `commerc_product_name`, `commerc_product_version`, `create_mode`, `logic_status`) VALUES
('DETAIL003', 'springboot', '2.7.0', 'springboot:2.7.0', 'PROD', '2026-03-31 14:00:00', '开源基础软件', 'APP002', '测试应用B', 'MAIN002', '/opt/app/springboot.jar', 0, '192.168.1.100', 'java -jar springboot.jar', '否', '', '', 0, 0);
