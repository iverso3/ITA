-- Migration: Create Flow Role Tables and Menus
-- 流程角色表和用户流程角色关联表
-- 注意：此文件需要以UTF-8编码保存

USE bank_it_arch;

-- 1. 创建 oss_flow_role 表（如果不存在）
CREATE TABLE IF NOT EXISTS oss_flow_role (
    flow_role_id VARCHAR(32) PRIMARY KEY COMMENT '流程角色ID',
    flow_role_name VARCHAR(32) NOT NULL COMMENT '流程角色名称',
    flow_role_desc VARCHAR(512) NOT NULL COMMENT '流程角色描述',
    flow_row_seq INT(10) NOT NULL DEFAULT 1 COMMENT '排序',
    bind_branch_flag VARCHAR(8) NOT NULL DEFAULT '0' COMMENT '绑定部门标志(0-否,1-是)',
    bind_team_flag VARCHAR(8) NOT NULL DEFAULT '0' COMMENT '绑定团队标志(0-否,1-是)',
    bind_app_flag VARCHAR(8) NOT NULL DEFAULT '0' COMMENT '绑定应用标志(0-否,1-是)',
    sync_datetime DATETIME COMMENT '同步时间',
    create_mode INT(10) NOT NULL DEFAULT 0 COMMENT '创建模式(0-本地,1-远程同步)',
    create_user_id VARCHAR(32) NOT NULL COMMENT '创建人ID',
    create_datetime DATETIME NOT NULL COMMENT '创建时间',
    update_user_id VARCHAR(32) COMMENT '更新人ID',
    update_time DATETIME COMMENT '更新时间',
    logic_status VARCHAR(8) NOT NULL DEFAULT '0' COMMENT '逻辑状态(0-正常,1-删除)',
    flow_role_type VARCHAR(8) NOT NULL DEFAULT '00' COMMENT '角色类型(00-A角色,01-B角色)',
    check_branch_flag VARCHAR(8) NOT NULL DEFAULT '0' COMMENT '部门一致性校验(0-否,1-是)',
    bind_institution_flag VARCHAR(8) NOT NULL DEFAULT '0' COMMENT '绑定机构标志(0-否,1-是)',
    INDEX idx_flow_role_name (flow_role_name),
    INDEX idx_logic_status (logic_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程角色表';

-- 2. 创建 oss_flow_role_user_rel 表（如果不存在）
CREATE TABLE IF NOT EXISTS oss_flow_role_user_rel (
    rel_id VARCHAR(32) PRIMARY KEY COMMENT '关系ID',
    flow_role_id VARCHAR(32) NOT NULL COMMENT '流程角色ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    branch_id VARCHAR(32) COMMENT '部门ID',
    team_id VARCHAR(32) COMMENT '团队ID',
    sync_datetime DATETIME COMMENT '同步时间',
    create_mode INT(10) NOT NULL DEFAULT 0 COMMENT '创建模式(0-本地,1-远程同步)',
    create_user_id VARCHAR(32) NOT NULL COMMENT '创建人ID',
    create_datetime DATETIME COMMENT '创建时间',
    update_user_id VARCHAR(32) NOT NULL COMMENT '更新人ID',
    update_datetime DATETIME COMMENT '更新时间',
    logic_status VARCHAR(8) NOT NULL DEFAULT '0' COMMENT '逻辑状态(0-正常,1-删除)',
    user_name VARCHAR(100) NOT NULL COMMENT '用户名称',
    flow_role_name VARCHAR(32) NOT NULL COMMENT '流程角色名称',
    branch_name VARCHAR(128) COMMENT '部门名称',
    team_name VARCHAR(32) COMMENT '团队名称',
    flow_role_type VARCHAR(8) NOT NULL DEFAULT '0' COMMENT '角色类型(0-A角色,1-B角色)',
    org_id VARCHAR(128) COMMENT '机构ID',
    org_name VARCHAR(128) COMMENT '机构名称',
    INDEX idx_flow_role_id (flow_role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_logic_status (logic_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户流程角色关联表';

-- 3. 清理可能损坏的旧菜单数据
DELETE FROM sys_role_menu WHERE menu_id IN (2010, 2011);
DELETE FROM sys_menu WHERE id IN (2010, 2011);

-- 4. 设置字符集并插入正确的菜单数据
SET NAMES utf8mb4;

INSERT INTO sys_menu (id, parent_id, menu_code, menu_name, menu_type, icon, path, component, is_visible, sort_order) VALUES
(2010, 8, 'FLOW_ROLE', '流程角色管理', 'MENU', 'el-icon-postcard', '/flowRole/flowRole', 'flowRole/FlowRole', 1, 5);

INSERT INTO sys_menu (id, parent_id, menu_code, menu_name, menu_type, icon, path, component, is_visible, sort_order) VALUES
(2011, 8, 'FLOW_ROLE_USER_REL', '用户流程角色设置', 'MENU', 'el-icon-user', '/flowRole/userRel', 'flowRole/FlowRoleUserRel', 1, 6);

-- 5. 为超级管理员添加菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2010), (1, 2011);

-- 6. 验证表已创建
SHOW TABLES LIKE 'oss_flow%';

-- 7. 验证菜单数据
SELECT id, parent_id, menu_code, menu_name, menu_type, path, component
FROM sys_menu
WHERE menu_code IN ('FLOW_ROLE', 'FLOW_ROLE_USER_REL')
AND deleted = 0;

SELECT 'Flow role tables and menus migration completed!' AS Result;
