-- Migration: Workflow trace fix and menu reorganization
-- Version: 2.9
-- Description: Fix approval path display and reorganize menu structure
-- Date: 2026-04-03

USE bank_it_arch;

-- =============================================
-- Part 1: Fix node_order for workflow definition nodes
-- =============================================
-- Update node_order for imply workflow (definition_id=3)
UPDATE wf_definition_node SET node_order = 0 WHERE id = 149;  -- 开始
UPDATE wf_definition_node SET node_order = 1 WHERE id = 150;  -- 条件
UPDATE wf_definition_node SET node_order = 2 WHERE id = 151;  -- 领域架构师
UPDATE wf_definition_node SET node_order = 3 WHERE id = 152;  -- 团队负责人
UPDATE wf_definition_node SET node_order = 4 WHERE id = 153;  -- 技术专家
UPDATE wf_definition_node SET node_order = 5 WHERE id = 154;  -- 开源软件管理员
UPDATE wf_definition_node SET node_order = 6 WHERE id = 155;  -- 结束

-- Fix condition expressions to use string comparison
UPDATE wf_definition_node
SET condition_rule = '{"conditions":[{"label":"首次引入","expression":"implApplyType == ''0''","targetNodeId":"151"},{"label":"新版本引入","expression":"implApplyType == ''1''","targetNodeId":"152"}],"defaultNodeId":null}'
WHERE id = 150 AND node_category = 'CONDITION';

-- =============================================
-- Part 2: Menu reorganization
-- =============================================
-- Fix top-level menu sort order
UPDATE sys_menu SET sort_order = 0 WHERE id = 9001;  -- 首页
UPDATE sys_menu SET sort_order = 2 WHERE id = 2;     -- CMDB管理
UPDATE sys_menu SET sort_order = 3 WHERE id = 2000;  -- 企业资产
UPDATE sys_menu SET sort_order = 4 WHERE id = 2002;  -- 开源软件资产管理
UPDATE sys_menu SET sort_order = 5 WHERE id = 7;    -- 报表中心
UPDATE sys_menu SET sort_order = 6 WHERE id = 8;     -- 系统管理
UPDATE sys_menu SET sort_order = 7 WHERE id = 2004;  -- 元模型管理

-- Move 建模工具 under 开源软件资产管理
UPDATE sys_menu SET parent_id = 2002, sort_order = 4 WHERE id = 6;

-- Create 我的工作台 category (if not exists)
INSERT IGNORE INTO sys_menu (id, parent_id, tree_path, menu_code, menu_name, menu_type, icon, path, component, sort_order, is_visible, is_active, deleted, create_time, update_time)
VALUES (9003, 0, '/9003', 'MY_WORKBENCH', '我的工作台', 'CATALOG', 'Monitor', '/workbench', NULL, 1, 1, 1, 0, NOW(), NOW());

-- Move 待办任务 and 已办任务 under 我的工作台
UPDATE sys_menu SET parent_id = 9003, sort_order = 0 WHERE id = 23;  -- 待办任务
UPDATE sys_menu SET parent_id = 9003, sort_order = 1 WHERE id = 24;  -- 已办任务

-- Verify changes
SELECT 'Top-level menus:' as info;
SELECT id, parent_id, menu_name, path, sort_order FROM sys_menu WHERE deleted = 0 AND parent_id = 0 ORDER BY sort_order;

SELECT 'Children of 我的工作台 (9003):' as info;
SELECT id, parent_id, menu_name, path, sort_order FROM sys_menu WHERE parent_id = 9003;

SELECT 'Children of 开源软件资产管理 (2002):' as info;
SELECT id, parent_id, menu_name, path, sort_order FROM sys_menu WHERE parent_id = 2002;
