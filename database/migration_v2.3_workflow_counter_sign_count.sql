-- Migration: Add counterSignCount field to wf_definition_node
-- Date: 2026-03-31
-- Description: Add dedicated counterSignCount field for workflow node to replace misuse of timeoutDuration

-- Add counter_sign_count column to wf_definition_node
ALTER TABLE wf_definition_node ADD COLUMN counter_sign_count INT DEFAULT NULL COMMENT '会签要求人数(仅会签模式使用)' AFTER approval_type;

-- Update existing counter sign nodes to use reasonable defaults (3 people)
UPDATE wf_definition_node
SET counter_sign_count = 3
WHERE approval_type IN ('MULTI_COUNTER', 'MULTI_OR')
AND counter_sign_count IS NULL;
