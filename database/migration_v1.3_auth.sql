-- =============================================
-- 银行IT架构管理平台 - 认证与权限管理迁移脚本
-- 版本: V1.3
-- 日期: 2026-03-27
-- 数据库: MySQL 8.0
-- =============================================

USE bank_it_arch;

-- =============================================
-- 1. 创建角色菜单权限表 (sys_role_menu)
-- =============================================
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID（可以是菜单或按钮）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单权限表';

-- =============================================
-- 2. 更新sys_menu表添加tree_path（如果不存在）
-- 注：init.sql中已包含tree_path字段
-- =============================================

-- =============================================
-- 3. 更新超级管理员密码为MD5格式
-- 原密码: admin123 -> MD5: 0192023a7bbd73250516f069df18b500
-- =============================================
UPDATE sys_user SET password = '0192023a7bbd73250516f069df18b500' WHERE username = 'admin';

-- =============================================
-- 4. 创建普通用户用于测试
-- =============================================
INSERT INTO sys_user (username, password, real_name, email, status, user_type, department_id, department_name) VALUES
('user01', 'c33367701511b4f6020ec61ded352059', '普通用户', 'user01@bank.com', 'ENABLED', 'LOCAL', 1, '科技部')
ON DUPLICATE KEY UPDATE password = 'c33367701511b4f6020ec61ded352059';

-- 创建普通用户角色
INSERT INTO sys_role (role_code, role_name, role_type, data_scope, description, is_active) VALUES
('ROLE_USER', '普通用户', 'BUSINESS', 'DEPT', '普通用户角色', 1)
ON DUPLICATE KEY UPDATE description = '普通用户角色';

-- 给普通用户分配角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'user01' AND r.role_code = 'ROLE_USER'
ON DUPLICATE KEY UPDATE role_id = (SELECT id FROM sys_role WHERE role_code = 'ROLE_USER');

-- =============================================
-- 5. 插入完整菜单树（带按钮权限）
-- =============================================

-- 清理现有菜单数据（如果需要重新初始化）
-- DELETE FROM sys_menu WHERE id > 0;

-- 一级目录
INSERT INTO sys_menu (id, parent_id, menu_code, menu_name, menu_type, icon, path, is_visible, sort_order) VALUES
(1, 0, 'DASHBOARD', '首页概览', 'CATALOG', 'HomeFilled', '/', 1, 1),
(2, 0, 'CMDB', '资产管理', 'CATALOG', 'DataLine', '/cmdb', 1, 2),
(3, 0, 'ARCH', '架构管理', 'CATALOG', 'Document', '/arch', 1, 3),
(4, 0, 'WF', '流程审批', 'CATALOG', 'Tickets', '/wf', 1, 4),
(5, 0, 'REPORT', '报表中心', 'CATALOG', 'TrendCharts', '/report', 1, 5),
(6, 0, 'SYSTEM', '系统管理', 'CATALOG', 'User', '/system', 1, 6)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 首页子菜单
INSERT INTO sys_menu (id, parent_id, menu_code, menu_name, menu_type, icon, path, component, is_visible, sort_order) VALUES
(101, 1, 'DASHBOARD_HOME', '首页', 'MENU', 'HomeFilled', '/dashboard', 'Dashboard', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- CMDB子菜单（含按钮）
INSERT INTO sys_menu (id, parent_id, menu_code, menu_name, menu_type, icon, path, component, is_visible, sort_order, permission) VALUES
-- CMDB概览
(201, 2, 'CMDB_OVERVIEW', 'CMDB概览', 'MENU', 'DataLine', '/cmdb/overview', 'cmdb/Overview', 1, 1, 'cmdb:overview:view'),
-- 服务器菜单及按钮
(202, 2, 'CMDB_SERVER', '服务器', 'MENU', 'Box', '/cmdb/servers', 'cmdb/Servers', 1, 2, 'cmdb:server:list'),
(203, 202, 'SERVER_ADD', '新增', 'BUTTON', '', '', '', 1, 1, 'cmdb:server:add'),
(204, 202, 'SERVER_EDIT', '编辑', 'BUTTON', '', '', '', 1, 2, 'cmdb:server:edit'),
(205, 202, 'SERVER_DELETE', '删除', 'BUTTON', '', '', '', 1, 3, 'cmdb:server:delete'),
(206, 202, 'SERVER_EXPORT', '导出', 'BUTTON', '', '', '', 1, 4, 'cmdb:server:export'),
(207, 202, 'SERVER_IMPORT', '导入', 'BUTTON', '', '', '', 1, 5, 'cmdb:server:import'),
-- 网络设备菜单及按钮
(208, 2, 'CMDB_NETWORK', '网络设备', 'MENU', 'Connection', '/cmdb/network', 'cmdb/Network', 1, 3, 'cmdb:network:list'),
(209, 208, 'NETWORK_ADD', '新增', 'BUTTON', '', '', '', 1, 1, 'cmdb:network:add'),
(210, 208, 'NETWORK_EDIT', '编辑', 'BUTTON', '', '', '', 1, 2, 'cmdb:network:edit'),
(211, 208, 'NETWORK_DELETE', '删除', 'BUTTON', '', '', '', 1, 3, 'cmdb:network:delete'),
-- 存储设备菜单及按钮
(212, 2, 'CMDB_STORAGE', '存储设备', 'MENU', 'Document', '/cmdb/storage', 'cmdb/Storage', 1, 4, 'cmdb:storage:list'),
(213, 212, 'STORAGE_ADD', '新增', 'BUTTON', '', '', '', 1, 1, 'cmdb:storage:add'),
(214, 212, 'STORAGE_EDIT', '编辑', 'BUTTON', '', '', '', 1, 2, 'cmdb:storage:edit'),
(215, 212, 'STORAGE_DELETE', '删除', 'BUTTON', '', '', '', 1, 3, 'cmdb:storage:delete'),
-- 云资源菜单及按钮
(216, 2, 'CMDB_CLOUD', '云资源', 'MENU', 'Cloud', '/cmdb/cloud', 'cmdb/Cloud', 1, 5, 'cmdb:cloud:list'),
(217, 216, 'CLOUD_ADD', '新增', 'BUTTON', '', '', '', 1, 1, 'cmdb:cloud:add'),
(218, 216, 'CLOUD_EDIT', '编辑', 'BUTTON', '', '', '', 1, 2, 'cmdb:cloud:edit'),
(219, 216, 'CLOUD_DELETE', '删除', 'BUTTON', '', '', '', 1, 3, 'cmdb:cloud:delete')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 架构管理子菜单
INSERT INTO sys_menu (id, parent_id, menu_code, menu_name, menu_type, icon, path, component, is_visible, sort_order, permission) VALUES
(301, 3, 'ARCH_APPLICATION', '应用架构', 'MENU', 'Document', '/arch/application', 'arch/Application', 1, 1, 'arch:application:list'),
(302, 301, 'APP_ADD', '新增', 'BUTTON', '', '', '', 1, 1, 'arch:application:add'),
(303, 301, 'APP_EDIT', '编辑', 'BUTTON', '', '', '', 1, 2, 'arch:application:edit'),
(304, 301, 'APP_DELETE', '删除', 'BUTTON', '', '', '', 1, 3, 'arch:application:delete'),
(305, 3, 'TECH_ARCH', '技术架构', 'CATALOG', 'PieChart', '/tech', NULL, 1, 2, NULL),
(306, 3, 'DATA_ARCH', '数据架构', 'CATALOG', 'Coin', '/data', NULL, 1, 3, NULL),
(307, 305, 'TECH_STACK', '技术栈', 'MENU', 'Coin', '/tech/stack', 'tech/Stack', 1, 1, 'tech:stack:list'),
(308, 305, 'TECH_DEPLOYMENT', '部署架构', 'MENU', 'Management', '/tech/deployment', 'tech/Deployment', 1, 2, 'tech:deployment:list'),
(309, 306, 'DATA_FLOW', '数据流向', 'MENU', 'Right', '/data/flow', 'data/Flow', 1, 1, 'data:flow:list'),
(310, 306, 'DATA_DISTRIBUTION', '数据分布', 'MENU', 'DataAnalysis', '/data/distribution', 'data/Distribution', 1, 2, 'data:distribution:view')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), menu_type = VALUES(menu_type), path = VALUES(path), component = VALUES(component);

-- 流程审批子菜单
INSERT INTO sys_menu (id, parent_id, menu_code, menu_name, menu_type, icon, path, component, is_visible, sort_order, permission) VALUES
(401, 4, 'WF_TODO', '待办任务', 'MENU', 'Tickets', '/wf/todo', 'wf/Todo', 1, 1, 'wf:task:todo'),
(402, 4, 'WF_DONE', '已办任务', 'MENU', 'CircleCheck', '/wf/done', 'wf/Done', 1, 2, 'wf:task:done')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 报表中心子菜单
INSERT INTO sys_menu (id, parent_id, menu_code, menu_name, menu_type, icon, path, component, is_visible, sort_order, permission) VALUES
(501, 5, 'REPORT_STATISTICS', '统计报表', 'MENU', 'DataLine', '/report/statistics', 'report/Statistics', 1, 1, 'report:statistics:view'),
(502, 5, 'REPORT_PANORAMA', '全景图', 'MENU', 'Picture', '/report/panorama', 'report/Panorama', 1, 2, 'report:panorama:view')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 系统管理子菜单（仅超级管理员可见）
INSERT INTO sys_menu (id, parent_id, menu_code, menu_name, menu_type, icon, path, component, is_visible, sort_order, permission) VALUES
(601, 6, 'SYSTEM_USER', '用户管理', 'MENU', 'User', '/system/user', 'system/User', 1, 1, 'system:user:list'),
(602, 601, 'USER_ADD', '新增', 'BUTTON', '', '', '', 1, 1, 'system:user:add'),
(603, 601, 'USER_EDIT', '编辑', 'BUTTON', '', '', '', 1, 2, 'system:user:edit'),
(604, 601, 'USER_DELETE', '删除', 'BUTTON', '', '', '', 1, 3, 'system:user:delete'),
(605, 6, 'SYSTEM_ROLE', '角色管理', 'MENU', 'Postcard', '/system/role', 'system/Role', 1, 2, 'system:role:list'),
(606, 605, 'ROLE_ADD', '新增', 'BUTTON', '', '', '', 1, 1, 'system:role:add'),
(607, 605, 'ROLE_EDIT', '编辑', 'BUTTON', '', '', '', 1, 2, 'system:role:edit'),
(608, 605, 'ROLE_DELETE', '删除', 'BUTTON', '', '', '', 1, 3, 'system:role:delete'),
(609, 605, 'ROLE_PERMISSION', '菜单权限', 'BUTTON', '', '', '', 1, 4, 'system:role:permission'),
(610, 6, 'SYSTEM_MENU', '菜单管理', 'MENU', 'Menu', '/system/menu', 'system/Menu', 1, 3, 'system:menu:list'),
(611, 610, 'MENU_ADD', '新增', 'BUTTON', '', '', '', 1, 1, 'system:menu:add'),
(612, 610, 'MENU_EDIT', '编辑', 'BUTTON', '', '', '', 1, 2, 'system:menu:edit'),
(613, 610, 'MENU_DELETE', '删除', 'BUTTON', '', '', '', 1, 3, 'system:menu:delete')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- =============================================
-- 6. 给超级管理员分配所有菜单权限
-- =============================================
-- 先清除旧关联
DELETE FROM sys_role_menu WHERE role_id = 1;

-- 插入超级管理员的所有菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1 AS role_id, id FROM sys_menu WHERE id > 0;

-- 给普通用户分配部分菜单权限（仅CMDB概览和报表中心）
DELETE FROM sys_role_menu WHERE role_id = (SELECT id FROM sys_role WHERE role_code = 'ROLE_USER');
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT (SELECT id FROM sys_role WHERE role_code = 'ROLE_USER') AS role_id, id FROM sys_menu
WHERE menu_code IN ('DASHBOARD', 'DASHBOARD_HOME', 'CMDB', 'CMDB_OVERVIEW', 'REPORT', 'REPORT_STATISTICS', 'REPORT_PANORAMA');

-- =============================================
-- 7. 更新tree_path字段
-- =============================================
-- 需要程序启动时自动更新，或者手动执行以下SQL
UPDATE sys_menu SET tree_path = '/' WHERE parent_id = 0;
UPDATE sys_menu m1, sys_menu m2 SET m1.tree_path = CONCAT('/', m2.id, '/') WHERE m1.parent_id = m2.id AND m2.parent_id = 0;
UPDATE sys_menu m1, sys_menu m2, sys_menu m3 SET m1.tree_path = CONCAT('/', m2.id, '/', m3.id, '/') WHERE m1.parent_id = m3.id AND m3.parent_id = m2.id AND m2.parent_id = 0;

SELECT 'Auth migration completed!' AS Result;
