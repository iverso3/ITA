-- Migration: Add "引入申请流程" menu under "开源软件资产管理"
-- Version: 2.5
-- Description: Add a new menu item for direct access to the impl apply form

USE bank_it_arch;

-- Parent menu ID for "开源软件资产管理" is 2002
SET @oss_parent_id = 2002;

-- Insert the new menu
INSERT INTO sys_menu (parent_id, menu_code, menu_name, menu_type, icon, path, component, is_visible, sort_order, permission)
VALUES (
    @oss_parent_id,
    'OSS_IMPL_APPLY_FORM',
    '引入申请流程',
    'MENU',
    'Document',
    '/oss/impl-apply/form',
    'oss/ImplApplyForm',
    1,
    3,
    'oss:impl:apply:add'
)
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name),
    path = VALUES(path),
    component = VALUES(component);

-- Grant permission to admin role (role_id = 1)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE menu_code = 'OSS_IMPL_APPLY_FORM' AND deleted = 0
ON DUPLICATE KEY UPDATE menu_id = VALUES(menu_id);

-- Verify the menu structure
SELECT id, parent_id, menu_code, menu_name, menu_type, path, component
FROM sys_menu
WHERE deleted = 0 AND (id = @oss_parent_id OR menu_code = 'OSS_IMPL_APPLY_FORM')
ORDER BY parent_id, sort_order;
