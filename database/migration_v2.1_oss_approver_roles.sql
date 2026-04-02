-- Migration: OSS Approver Roles
-- Version: 2.1
-- Description: Add OSS implementation approval roles

SET NAMES utf8mb4;
USE bank_it_arch;

-- ============================================
-- 1. Insert OSS approval roles
-- ============================================
INSERT INTO `sys_role` (`role_code`, `role_name`, `role_type`, `data_scope`, `description`, `is_active`, `sort_order`, `remark`) VALUES
('OSS_SECURITY_APPROVER', '开源软件安全审批员', 'BUSINESS', 'ALL', '负责开源软件引入的安全审批，包括许可证检查、漏洞扫描、恶意代码检测等', 1, 100, '开源软件安全审批角色'),
('OSS_TECH_APPROVER', '开源软件技术审批员', 'BUSINESS', 'ALL', '负责开源软件引入的技术审批，包括功能评估、性能评估、技术可行性等', 1, 101, '开源软件技术审批角色'),
('OSS_MANAGER_APPROVER', '开源软件主管审批员', 'BUSINESS', 'ALL', '负责开源软件引入的主管审批，最终审批权限', 1, 102, '开源软件主管审批角色')
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`);
