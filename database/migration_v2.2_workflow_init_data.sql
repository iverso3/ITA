-- Migration: Workflow Initial Data
-- Version: 2.2
-- Description: Insert initial workflow nodes and lines for default approval process
-- Also fix missing columns in wf_definition_version

USE bank_it_arch;

-- Add missing columns to wf_definition_version if not exist
SET @dbname = DATABASE();
SET @tablename = 'wf_definition_version';
SET @columnname = 'creator';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_definition_version ADD COLUMN creator VARCHAR(50) DEFAULT NULL AFTER change_log'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'modifier';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_definition_version ADD COLUMN modifier VARCHAR(50) DEFAULT NULL AFTER creator'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- Insert workflow nodes for definition_id=1 (新建应用审批)
-- Only insert if no nodes exist
-- =============================================

INSERT IGNORE INTO wf_definition_node (definition_id, node_code, node_name, node_order, node_type, node_category, approval_type, approver_type, approver_id, approver_name, is_required, timeout_hours) VALUES
(1, 'start', '开始', 1, 'START', 'START', NULL, NULL, NULL, NULL, 0, 0),
(1, 'dept_approval', '部门负责人审批', 2, 'APPROVAL', 'APPROVAL', 'SINGLE', 'DEPT_HEAD', NULL, NULL, 1, 72),
(1, 'arch_approval', '架构委员会审批', 3, 'APPROVAL', 'APPROVAL', 'SINGLE', 'ROLE', 3, '架构管理员', 0, 72),
(1, 'it_approval', '科技管理层审批', 4, 'APPROVAL', 'APPROVAL', 'SINGLE', 'MANAGER', NULL, NULL, 0, 72),
(1, 'end', '结束', 5, 'END', 'END', NULL, NULL, NULL, NULL, 0, 0);

-- =============================================
-- Insert workflow lines using node codes to find IDs
-- =============================================

INSERT IGNORE INTO wf_definition_line (definition_id, source_node_id, target_node_id, line_code, line_name, condition_expr, line_order)
SELECT 1, src.id, tgt.id, 'line_1', '部门审批通过', NULL, 1
FROM wf_definition_node src, wf_definition_node tgt
WHERE src.definition_id = 1 AND src.node_code = 'start'
  AND tgt.definition_id = 1 AND tgt.node_code = 'dept_approval';

INSERT IGNORE INTO wf_definition_line (definition_id, source_node_id, target_node_id, line_code, line_name, condition_expr, line_order)
SELECT 1, src.id, tgt.id, 'line_2', '流转架构审批', NULL, 2
FROM wf_definition_node src, wf_definition_node tgt
WHERE src.definition_id = 1 AND src.node_code = 'dept_approval'
  AND tgt.definition_id = 1 AND tgt.node_code = 'arch_approval';

INSERT IGNORE INTO wf_definition_line (definition_id, source_node_id, target_node_id, line_code, line_name, condition_expr, line_order)
SELECT 1, src.id, tgt.id, 'line_3', '流转科技审批', NULL, 3
FROM wf_definition_node src, wf_definition_node tgt
WHERE src.definition_id = 1 AND src.node_code = 'arch_approval'
  AND tgt.definition_id = 1 AND tgt.node_code = 'it_approval';

INSERT IGNORE INTO wf_definition_line (definition_id, source_node_id, target_node_id, line_code, line_name, condition_expr, line_order)
SELECT 1, src.id, tgt.id, 'line_4', '审批通过', NULL, 4
FROM wf_definition_node src, wf_definition_node tgt
WHERE src.definition_id = 1 AND src.node_code = 'it_approval'
  AND tgt.definition_id = 1 AND tgt.node_code = 'end';

SELECT 'Migration v2.2 workflow_init_data completed!' AS result;
