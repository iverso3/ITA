-- Migration: Add flowRoleId field to workflow definition node
-- Version: 2.6
-- Description: Add flow_role_id column to support workflow role based task assignment

USE bank_it_arch;

-- Add flow_role_id column to wf_definition_node table
ALTER TABLE wf_definition_node ADD COLUMN flow_role_id VARCHAR(32) COMMENT '流程角色ID' AFTER assigned_role_id;

-- Verify the change
DESCRIBE wf_definition_node;
