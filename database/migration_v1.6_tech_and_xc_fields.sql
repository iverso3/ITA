-- Migration: Add technical info and Xinchuang (信创) fields
-- Version: 1.6
-- Description: Add technical security, deployment, and Xinchuang fields for application detail page

-- ==================== 基础实施信息 ====================

-- 实施方式（外购产品/定制化产品）
ALTER TABLE arch_application ADD COLUMN implementation_method VARCHAR(32) COMMENT '实施方式' AFTER version;

-- 实施类型（内部管理类/业务处理类等）
ALTER TABLE arch_application ADD COLUMN implementation_type VARCHAR(32) COMMENT '实施类型' AFTER implementation_method;

-- ==================== 安全基础信息 ====================

-- 登录用户字段名称
ALTER TABLE arch_application ADD COLUMN login_user_field VARCHAR(64) COMMENT '登录用户字段名称' AFTER implementation_type;

-- 登录密码字段名称
ALTER TABLE arch_application ADD COLUMN login_password_field VARCHAR(64) COMMENT '登录密码字段名称' AFTER login_user_field;

-- 是否部署在DMZ区（0-否，1-是）
ALTER TABLE arch_application ADD COLUMN is_deployed_dmz TINYINT DEFAULT 0 COMMENT '是否部署在DMZ区' AFTER login_password_field;

-- 是否存在上传功能（0-否，1-是）
ALTER TABLE arch_application ADD COLUMN has_upload_function TINYINT DEFAULT 0 COMMENT '是否存在上传功能' AFTER is_deployed_dmz;

-- 账号密码错误冻结机制
ALTER TABLE arch_application ADD COLUMN pwd_error_freeze_mechanism VARCHAR(256) COMMENT '账号密码错误冻结机制' AFTER has_upload_function;

-- 是否存在主动外发请求业务场景（0-否，1-是）
ALTER TABLE arch_application ADD COLUMN has_outbound_request TINYINT DEFAULT 0 COMMENT '是否存在主动外发请求业务场景' AFTER pwd_error_freeze_mechanism;

-- 外发业务场景描述
ALTER TABLE arch_application ADD COLUMN outbound_request_desc VARCHAR(512) COMMENT '外发业务场景描述' AFTER has_outbound_request;

-- 上传的功能描述和URL
ALTER TABLE arch_application ADD COLUMN upload_function_desc VARCHAR(512) COMMENT '上传的功能描述和URL' AFTER outbound_request_desc;

-- 文件上传路径
ALTER TABLE arch_application ADD COLUMN upload_file_path VARCHAR(256) COMMENT '文件上传路径' AFTER upload_function_desc;

-- 文件上传路径执行权限（可执行/不可执行）
ALTER TABLE arch_application ADD COLUMN upload_path_executable VARCHAR(16) COMMENT '文件上传路径执行权限' AFTER upload_file_path;

-- 上传文件类型
ALTER TABLE arch_application ADD COLUMN upload_file_types VARCHAR(256) COMMENT '上传文件类型' AFTER upload_path_executable;

-- 是否存在下载功能（0-否，1-是）
ALTER TABLE arch_application ADD COLUMN has_download_function TINYINT DEFAULT 0 COMMENT '是否存在下载功能' AFTER upload_file_types;

-- 下载的功能描述和URL
ALTER TABLE arch_application ADD COLUMN download_function_desc VARCHAR(512) COMMENT '下载的功能描述和URL' AFTER has_download_function;

-- 下载文件类型
ALTER TABLE arch_application ADD COLUMN download_file_types VARCHAR(256) COMMENT '下载文件类型' AFTER download_function_desc;

-- 是否APP应用（0-否，1-是）
ALTER TABLE arch_application ADD COLUMN is_app_application TINYINT DEFAULT 0 COMMENT '是否APP应用' AFTER download_file_types;

-- 开源有关信息
ALTER TABLE arch_application ADD COLUMN open_source_info VARCHAR(512) COMMENT '开源有关信息' AFTER is_app_application;

-- 访问主页
ALTER TABLE arch_application ADD COLUMN access_url VARCHAR(256) COMMENT '访问主页' AFTER open_source_info;

-- ==================== 其他信息 ====================

-- 域名
ALTER TABLE arch_application ADD COLUMN domain_name VARCHAR(128) COMMENT '域名' AFTER access_url;

-- 网络结构模式（B/S、C/S等）
ALTER TABLE arch_application ADD COLUMN network_mode VARCHAR(32) COMMENT '网络结构模式' AFTER domain_name;

-- 实施商信息（多个用逗号分隔）
ALTER TABLE arch_application ADD COLUMN vendor_info VARCHAR(512) COMMENT '实施商信息' AFTER network_mode;

-- 认证方式
ALTER TABLE arch_application ADD COLUMN auth_method VARCHAR(128) COMMENT '认证方式' AFTER vendor_info;

-- 是否与外部第三方系统对接（0-否，1-是）
ALTER TABLE arch_application ADD COLUMN has_third_party_integration TINYINT DEFAULT 0 COMMENT '是否与外部第三方系统对接' AFTER auth_method;

-- 与外部第三方系统对接补充说明
ALTER TABLE arch_application ADD COLUMN third_party_integration_desc VARCHAR(512) COMMENT '与外部第三方系统对接补充说明' AFTER has_third_party_integration;

-- 互联网线路类型（0-否，1-是）
ALTER TABLE arch_application ADD COLUMN is_internet_line TINYINT DEFAULT 0 COMMENT '互联网线路类型' AFTER third_party_integration_desc;

-- ==================== 信创情况（Xinchuang） ====================

-- 应用是否上信创容器云（已上云/未上云）
ALTER TABLE arch_application ADD COLUMN xc_cloud_status VARCHAR(32) COMMENT '应用是否上信创容器云' AFTER is_internet_line;

-- 服务器信创情况说明
ALTER TABLE arch_application ADD COLUMN server_xc_desc VARCHAR(256) COMMENT '服务器信创情况说明' AFTER xc_cloud_status;

-- 服务器信创情况（全栈信创/部分信创/非信创）
ALTER TABLE arch_application ADD COLUMN server_xc_status VARCHAR(32) COMMENT '服务器信创情况' AFTER server_xc_desc;

-- 是否使用第三方产品（0-否，1-是）
ALTER TABLE arch_application ADD COLUMN has_third_party_product TINYINT DEFAULT 0 COMMENT '是否使用第三方产品' AFTER server_xc_status;

-- 第三方产品是否信创产品（0-否，1-是）
ALTER TABLE arch_application ADD COLUMN third_party_is_xc TINYINT DEFAULT 0 COMMENT '第三方产品是否信创产品' AFTER has_third_party_product;

-- 是否全栈信创（0-否，1-是）
ALTER TABLE arch_application ADD COLUMN is_full_xc TINYINT DEFAULT 0 COMMENT '是否全栈信创' AFTER third_party_is_xc;

-- 数据库服务器信创情况
ALTER TABLE arch_application ADD COLUMN db_server_xc_status VARCHAR(32) COMMENT '数据库服务器信创情况' AFTER is_full_xc;

-- 数据库服务器操作系统
ALTER TABLE arch_application ADD COLUMN db_server_os VARCHAR(32) COMMENT '数据库服务器操作系统' AFTER db_server_xc_status;

-- 数据库信创情况
ALTER TABLE arch_application ADD COLUMN database_xc_status VARCHAR(32) COMMENT '数据库信创情况' AFTER db_server_os;

-- 数据库信创情况说明（如GDB数据库等）
ALTER TABLE arch_application ADD COLUMN database_xc_desc VARCHAR(256) COMMENT '数据库信创情况说明' AFTER database_xc_status;

-- 非全栈信创情况
ALTER TABLE arch_application ADD COLUMN partial_xc_desc VARCHAR(512) COMMENT '非全栈信创情况' AFTER database_xc_desc;

-- XC分类（一般/重要/核心）
ALTER TABLE arch_application ADD COLUMN xc_classification VARCHAR(32) COMMENT 'XC分类' AFTER partial_xc_desc;

-- 第三方产品信创情况说明
ALTER TABLE arch_application ADD COLUMN third_party_xc_desc VARCHAR(256) COMMENT '第三方产品信创情况说明' AFTER xc_classification;

-- 信创整体情况说明
ALTER TABLE arch_application ADD COLUMN xc_overall_desc VARCHAR(1024) COMMENT '信创整体情况说明' AFTER third_party_xc_desc;
