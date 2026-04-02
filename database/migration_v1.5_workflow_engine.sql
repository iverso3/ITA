-- Migration: Workflow Engine Enhancement
-- Version: 1.5
-- Description: Add complete workflow engine tables for visual approval workflow system

USE bank_it_arch;

-- ============================================
-- 1. Enhance wf_definition_node with new fields
-- Check if column exists before adding
-- ============================================
SET @dbname = DATABASE();
SET @tablename = 'wf_definition_node';
SET @columnname = 'node_category';
SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @dbname
        AND TABLE_NAME = @tablename
        AND COLUMN_NAME = @columnname
    ) > 0,
    'SELECT 1',
    'ALTER TABLE wf_definition_node ADD COLUMN node_category VARCHAR(20) DEFAULT \"APPROVAL\" COMMENT \"节点类别:START/END/APPROVAL/CONDITION/PARALLEL_BRANCH/PARALLEL_JOIN\" AFTER node_type'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'approval_type';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_definition_node ADD COLUMN approval_type VARCHAR(20) COMMENT \"审批方式:SINGLE/GRAB/MULTI_COUNTER/MULTI_OR\" AFTER node_category'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'assigned_role_id';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_definition_node ADD COLUMN assigned_role_id BIGINT COMMENT \"审批角色ID\" AFTER approval_type'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'timeout_duration';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_definition_node ADD COLUMN timeout_duration INT COMMENT \"超时时长(分钟)\" AFTER assigned_role_id'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'timeout_action';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_definition_node ADD COLUMN timeout_action VARCHAR(20) COMMENT \"超时动作:TRANSFER/SKIP/AUTO_APPROVE\" AFTER timeout_duration'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'timeout_transfer_user';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_definition_node ADD COLUMN timeout_transfer_user VARCHAR(50) COMMENT \"超时转办人\" AFTER timeout_action'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'form_schema';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_definition_node ADD COLUMN form_schema TEXT COMMENT \"表单配置JSON\" AFTER timeout_transfer_user'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'notice_config';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_definition_node ADD COLUMN notice_config TEXT COMMENT \"通知配置JSON\" AFTER form_schema'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'extra_attrs';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_definition_node ADD COLUMN extra_attrs TEXT COMMENT \"扩展属性JSON\" AFTER notice_config'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 2. Create wf_definition_line (流程连线)
-- ============================================
CREATE TABLE IF NOT EXISTS wf_definition_line (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    definition_id BIGINT NOT NULL COMMENT '流程定义ID',
    source_node_id BIGINT NOT NULL COMMENT '源节点ID',
    target_node_id BIGINT NOT NULL COMMENT '目标节点ID',
    line_code VARCHAR(50) COMMENT '连线编码',
    line_name VARCHAR(100) COMMENT '连线名称',
    condition_expr TEXT COMMENT '条件表达式(JSON)',
    line_order INT DEFAULT 0 COMMENT '连线顺序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator VARCHAR(50),
    modifier VARCHAR(50),
    deleted TINYINT DEFAULT 0,
    INDEX idx_definition_id (definition_id),
    INDEX idx_source_node (source_node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程连线定义';

-- ============================================
-- 3. Create wf_definition_version (流程版本)
-- ============================================
CREATE TABLE IF NOT EXISTS wf_definition_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    definition_id BIGINT NOT NULL COMMENT '流程定义ID',
    version INT NOT NULL COMMENT '版本号',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态:DRAFT/PUBLISHED/OBSOLETE',
    publish_time DATETIME COMMENT '发布时间',
    publish_user VARCHAR(50) COMMENT '发布人',
    change_log TEXT COMMENT '变更日志',
    creator VARCHAR(50) COMMENT '创建人',
    modifier VARCHAR(50) COMMENT '更新人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_definition_version (definition_id, version),
    INDEX idx_definition_id (definition_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程版本记录';

-- ============================================
-- 4. Create wf_instance_variable (流程实例变量)
-- ============================================
CREATE TABLE IF NOT EXISTS wf_instance_variable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id BIGINT NOT NULL COMMENT '流程实例ID',
    variable_key VARCHAR(100) NOT NULL COMMENT '变量名',
    variable_value TEXT COMMENT '变量值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_instance_variable (instance_id, variable_key),
    INDEX idx_instance_id (instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例变量';

-- ============================================
-- 5. Enhance wf_task with new fields
-- ============================================
SET @tablename = 'wf_task';

SET @columnname = 'task_category';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_task ADD COLUMN task_category VARCHAR(20) DEFAULT \"NORMAL\" COMMENT \"任务类型:NORMAL/COUNTER_SIGN/SUB_TASK\" AFTER status'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'parent_task_id';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_task ADD COLUMN parent_task_id BIGINT COMMENT \"父任务ID(加签时)\" AFTER task_category'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'counter_sign_type';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_task ADD COLUMN counter_sign_type VARCHAR(20) COMMENT \"会签类型:COUNTER/OR\" AFTER parent_task_id'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'counter_sign_count';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_task ADD COLUMN counter_sign_count INT COMMENT \"会签要求人数\" AFTER counter_sign_type'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'counter_sign_approved';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_task ADD COLUMN counter_sign_approved INT DEFAULT 0 COMMENT \"已审批人数\" AFTER counter_sign_count'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'is_timeout';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_task ADD COLUMN is_timeout TINYINT DEFAULT 0 COMMENT \"是否已超时\" AFTER counter_sign_approved'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'task_action';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_task ADD COLUMN task_action VARCHAR(20) COMMENT \"任务动作:APPROVE/REJECT/TRANSFER/DELEGATE/RETURN\" AFTER is_timeout'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @columnname = 'claim_time';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_task ADD COLUMN claim_time DATETIME COMMENT \"签收时间\" AFTER task_action'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add indexes if not exist
SET @indexname = 'idx_instance_id';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND INDEX_NAME = @indexname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_task ADD INDEX idx_instance_id (instance_id)'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 6. Create wf_task_counter_sign (会签记录)
-- ============================================
CREATE TABLE IF NOT EXISTS wf_task_counter_sign (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    approved TINYINT COMMENT '是否同意:1同意/0拒绝',
    comment TEXT COMMENT '审批意见',
    sign_time DATETIME COMMENT '签署时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_task_id (task_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会签记录';

-- ============================================
-- 7. Create wf_task_timeout_rule (超时规则)
-- ============================================
CREATE TABLE IF NOT EXISTS wf_task_timeout_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    definition_id BIGINT NOT NULL COMMENT '流程定义ID',
    node_id BIGINT NOT NULL COMMENT '节点ID',
    timeout_duration INT NOT NULL COMMENT '超时时长(分钟)',
    timeout_action VARCHAR(20) NOT NULL COMMENT '超时动作',
    timeout_notice_times TEXT COMMENT '提醒次数(JSON)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_definition_id (definition_id),
    INDEX idx_node_id (node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点超时规则';

-- ============================================
-- 8. Create wf_parallel_join (并行节点汇聚)
-- ============================================
CREATE TABLE IF NOT EXISTS wf_parallel_join (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id BIGINT NOT NULL COMMENT '流程实例ID',
    parallel_node_id BIGINT NOT NULL COMMENT '并行汇聚节点ID',
    branch_node_id BIGINT NOT NULL COMMENT '分支节点ID',
    branch_status VARCHAR(20) DEFAULT 'WAITING' COMMENT '分支状态:WAITING/RUNNING/COMPLETED/CANCELLED',
    complete_time DATETIME COMMENT '完成时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_instance_parallel (instance_id, parallel_node_id),
    INDEX idx_instance_id (instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='并行节点汇聚记录';

-- ============================================
-- 9. Create wf_token (流程令牌)
-- ============================================
CREATE TABLE IF NOT EXISTS wf_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id BIGINT NOT NULL COMMENT '流程实例ID',
    current_node_id BIGINT COMMENT '当前节点ID',
    token_status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '令牌状态:ACTIVE/WAITING/COMPLETED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_instance_id (instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程令牌';

-- ============================================
-- 10. Add indexes to wf_instance
-- ============================================
SET @tablename = 'wf_instance';
SET @indexname = 'idx_wf_instance_def';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND INDEX_NAME = @indexname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_instance ADD INDEX idx_definition_id (definition_id)'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @indexname = 'idx_wf_instance_biz';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND INDEX_NAME = @indexname) > 0,
    'SELECT 1',
    'ALTER TABLE wf_instance ADD INDEX idx_business_id (business_id)'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Migration v1.5 workflow_engine completed successfully!' AS result;
