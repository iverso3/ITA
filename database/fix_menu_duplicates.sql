-- =============================================
-- 修复菜单名称错位 + 删除重复菜单
-- =============================================

USE bank_it_arch;

-- 1. 修正顶级菜单名称（数据库中名称与init.sql不符）
UPDATE sys_menu SET menu_name = '仪表盘' WHERE id = 1;
UPDATE sys_menu SET menu_name = 'CMDB管理' WHERE id = 2;
UPDATE sys_menu SET menu_name = '应用架构' WHERE id = 3;
UPDATE sys_menu SET menu_name = '技术架构' WHERE id = 4;
UPDATE sys_menu SET menu_name = '数据架构' WHERE id = 5;
UPDATE sys_menu SET menu_name = '审批流程' WHERE id = 6;
UPDATE sys_menu SET menu_name = '报表中心' WHERE id = 7;
UPDATE sys_menu SET menu_name = '系统管理' WHERE id = 8;

-- 2. 修正子菜单名称
UPDATE sys_menu SET menu_name = '技术概览' WHERE id = 17;
UPDATE sys_menu SET menu_name = '数据实体' WHERE id = 20;
UPDATE sys_menu SET menu_name = '数据分布' WHERE id = 22;

-- 3. 删除 REPORT 下重复的统计报表（id=501 和 id=26 重复）
-- 保留 id=26（init.sql原始），删除 id=501（migration残留）
DELETE FROM sys_menu WHERE id = 501;

-- 4. 删除孤儿按钮（parent MENU 已不存在）
CREATE TEMPORARY TABLE IF NOT EXISTS valid_menus AS SELECT id FROM sys_menu WHERE deleted=0 AND is_active=1 AND menu_type IN ('CATALOG','MENU');
DELETE FROM sys_menu WHERE deleted=0 AND menu_type='BUTTON' AND parent_id NOT IN (SELECT id FROM valid_menus);
DROP TEMPORARY TABLE IF EXISTS valid_menus;

-- 5. 重新分配超级管理员菜单权限
DELETE FROM sys_role_menu WHERE role_id = 1;
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1 AS role_id, id FROM sys_menu WHERE deleted=0 AND is_active=1;

-- 6. 验证最终结构
SELECT id, parent_id, menu_code, menu_name, menu_type, path
FROM sys_menu WHERE deleted=0 AND is_active=1 ORDER BY parent_id, sort_order;

SELECT CONCAT('菜单总数: ', COUNT(*)) AS info FROM sys_menu WHERE deleted=0 AND is_active=1;
SELECT CONCAT('超级管理员权限数: ', COUNT(*)) AS info FROM sys_role_menu WHERE role_id=1;
