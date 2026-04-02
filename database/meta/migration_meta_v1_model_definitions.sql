-- =============================================
-- 元模型驱动架构 - 第一层：元模型定义
-- 版本: v1.0
-- 日期: 2026-03-31
-- 说明: 15张元模型定义表
-- =============================================

-- 1. meta_model: 模型定义
CREATE TABLE IF NOT EXISTS meta_model (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    model_code VARCHAR(64) NOT NULL COMMENT '模型编码（如: app, server, middleware）',
    model_name VARCHAR(128) NOT NULL COMMENT '模型名称',
    model_type VARCHAR(32) NOT NULL COMMENT '模型类型（ENTITY/RELATION/ABSTRACT）',
    table_name VARCHAR(64) COMMENT '关联物理表名（为空表示纯动态表）',
    table_alias VARCHAR(32) COMMENT '物理表别名',
    parent_model_id BIGINT COMMENT '父模型ID（继承关系）',
    tree_path VARCHAR(256) COMMENT '树形路径',
    version INT DEFAULT 1 COMMENT '当前版本号',
    status VARCHAR(16) DEFAULT 'DRAFT' COMMENT '状态（DRAFT/PUBLISHED/ARCHIVED）',
    is_generatable TINYINT DEFAULT 1 COMMENT '是否生成物理表',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    description TEXT COMMENT '描述',
    extra_config JSON COMMENT '扩展配置',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_model_code (model_code),
    INDEX idx_parent_model_id (parent_model_id),
    INDEX idx_status (status),
    INDEX idx_model_type (model_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='元模型定义表';

-- 2. meta_group: 字段分组定义
CREATE TABLE IF NOT EXISTS meta_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    group_code VARCHAR(64) NOT NULL COMMENT '分组编码（如: security_group, ops_group）',
    group_name VARCHAR(128) NOT NULL COMMENT '分组名称',
    group_type VARCHAR(32) DEFAULT 'CUSTOM' COMMENT '分组类型（SECURITY/OPS/BASIC/DESCRIPTION/RELATION）',
    model_id BIGINT NOT NULL COMMENT '所属模型ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    description TEXT COMMENT '描述',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    modifier VARCHAR(64),
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_model_group_code (model_id, group_code),
    INDEX idx_model_id (model_id),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字段分组定义表';

-- 3. meta_field: 字段定义
CREATE TABLE IF NOT EXISTS meta_field (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    field_code VARCHAR(64) NOT NULL COMMENT '字段编码（英文）',
    field_name VARCHAR(128) NOT NULL COMMENT '字段名称（中文）',
    field_type VARCHAR(32) NOT NULL COMMENT '字段类型（STRING/INT/BIGINT/DECIMAL/BOOLEAN/DATE/DATETIME/TEXT/JSON/FILE/ENUM）',
    storage_mode VARCHAR(16) DEFAULT 'COLUMN' COMMENT '存储模式（COLUMN/JSON/TAG）',
    json_path VARCHAR(128) COMMENT 'JSON路径（storage_mode=JSON时使用）',
    column_name VARCHAR(64) COMMENT '物理列名（storage_mode=COLUMN时使用）',
    column_type VARCHAR(64) COMMENT '物理列类型',
    column_size INT COMMENT '列大小',
    dict_code VARCHAR(64) COMMENT '字典编码（枚举类型使用）',
    validation_rule VARCHAR(256) COMMENT '校验规则（正则表达式）',
    default_value VARCHAR(128) COMMENT '默认值',
    is_required TINYINT DEFAULT 0 COMMENT '是否必填',
    is_unique TINYINT DEFAULT 0 COMMENT '是否唯一',
    is_indexed TINYINT DEFAULT 0 COMMENT '是否索引',
    is_queryable TINYINT DEFAULT 1 COMMENT '是否可查询',
    is_sensitive TINYINT DEFAULT 0 COMMENT '是否敏感（脱敏）',
    sensitive_type VARCHAR(32) COMMENT '敏感类型（PHONE/ID_CARD/EMAIL/BANK_CARD）',
    group_id BIGINT COMMENT '所属分组ID',
    model_id BIGINT NOT NULL COMMENT '所属模型ID',
    field_level VARCHAR(16) DEFAULT 'NORMAL' COMMENT '字段级别（NORMAL/SYSTEM/HIDDEN）',
    display_type VARCHAR(32) COMMENT '前端显示类型（INPUT/SELECT/DATE/DATERANGE/IMAGE/FILE/EDITOR/SWITCH）',
    option_config JSON COMMENT '选项配置（下拉/单选/多选等）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    description TEXT COMMENT '描述',
    help_text VARCHAR(256) COMMENT '帮助文本',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    modifier VARCHAR(64),
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_model_field_code (model_id, field_code),
    INDEX idx_model_id (model_id),
    INDEX idx_group_id (group_id),
    INDEX idx_dict_code (dict_code),
    INDEX idx_field_type (field_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字段定义表';

-- 4. meta_relationship: 关系定义
CREATE TABLE IF NOT EXISTS meta_relationship (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    rel_code VARCHAR(64) NOT NULL COMMENT '关系编码',
    rel_name VARCHAR(128) NOT NULL COMMENT '关系名称',
    rel_type VARCHAR(16) NOT NULL COMMENT '关系类型（N:1/1:N/N:N/1:1）',
    source_model_id BIGINT NOT NULL COMMENT '源模型ID',
    source_field_id BIGINT COMMENT '源字段ID（可选）',
    target_model_id BIGINT NOT NULL COMMENT '目标模型ID',
    target_field_id BIGINT COMMENT '目标字段ID（可选）',
    is_cascade_delete TINYINT DEFAULT 0 COMMENT '是否级联删除',
    is_self_rel TINYINT DEFAULT 0 COMMENT '是否自引用',
    rel_config JSON COMMENT '关系配置',
    description TEXT,
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    modifier VARCHAR(64),
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_rel_code (rel_code),
    INDEX idx_source_model_id (source_model_id),
    INDEX idx_target_model_id (target_model_id),
    INDEX idx_rel_type (rel_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关系定义表';

-- 5. meta_model_version: 版本管理
CREATE TABLE IF NOT EXISTS meta_model_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    version INT NOT NULL COMMENT '版本号',
    version_name VARCHAR(64) COMMENT '版本名称（如: v1.0.0）',
    status VARCHAR(16) NOT NULL COMMENT '状态（DRAFT/TESTING/PUBLISHED/ARCHIVED）',
    change_summary TEXT COMMENT '变更摘要',
    diff_content JSON COMMENT '变更内容（JSON格式）',
    published_at DATETIME COMMENT '发布时间',
    published_by VARCHAR(64) COMMENT '发布人',
    archived_at DATETIME COMMENT '归档时间',
    archived_by VARCHAR(64) COMMENT '归档人',
    rollback_from_version INT COMMENT '回滚来源版本',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_model_version (model_id, version),
    INDEX idx_model_id (model_id),
    INDEX idx_status (status),
    INDEX idx_published_at (published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型版本管理表';

-- 6. meta_publish_record: 发布记录
CREATE TABLE IF NOT EXISTS meta_publish_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    record_code VARCHAR(64) NOT NULL COMMENT '记录编码',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    version_from INT COMMENT '源版本（回滚时使用）',
    version_to INT NOT NULL COMMENT '目标版本',
    action_type VARCHAR(16) NOT NULL COMMENT '操作类型（PUBLISH/ARCHIVE/ROLLBACK）',
    status VARCHAR(16) DEFAULT 'PENDING' COMMENT '状态（PROCESSING/SUCCESS/FAILED）',
    execute_config JSON COMMENT '执行配置',
    execute_result JSON COMMENT '执行结果',
    error_message TEXT COMMENT '错误信息',
    operator VARCHAR(64) COMMENT '操作人',
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_record_code (record_code),
    INDEX idx_model_id (model_id),
    INDEX idx_action_type (action_type),
    INDEX idx_status (status),
    INDEX idx_operate_time (operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发布记录表';

-- 7. meta_data_dict_ext: 扩展字典（支持动态字典项）
CREATE TABLE IF NOT EXISTS meta_data_dict_ext (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    dict_code VARCHAR(64) NOT NULL COMMENT '字典编码',
    dict_name VARCHAR(128) NOT NULL COMMENT '字典名称',
    item_code VARCHAR(64) NOT NULL COMMENT '字典项编码',
    item_name VARCHAR(128) NOT NULL COMMENT '字典项名称',
    item_value VARCHAR(256) COMMENT '字典项值',
    parent_item_code VARCHAR(64) COMMENT '父级编码（树形字典）',
    tree_path VARCHAR(256) COMMENT '树形路径',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    description TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    modifier VARCHAR(64),
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_dict_item (dict_code, item_code),
    INDEX idx_parent_item (parent_item_code),
    INDEX idx_dict_code (dict_code),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='扩展字典表';

-- 8. meta_tag: 标签表（支持灵活打标）
CREATE TABLE IF NOT EXISTS meta_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tag_code VARCHAR(64) NOT NULL COMMENT '标签编码',
    tag_name VARCHAR(128) NOT NULL COMMENT '标签名称',
    tag_type VARCHAR(32) DEFAULT 'CUSTOM' COMMENT '标签类型（CUSTOM/SYSTEM/AUTO）',
    color VARCHAR(16) COMMENT '颜色',
    description TEXT,
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    modifier VARCHAR(64),
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_tag_code (tag_code),
    INDEX idx_tag_type (tag_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签定义表';

-- 9. meta_entity_tag: 实体标签关联
CREATE TABLE IF NOT EXISTS meta_entity_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    entity_id BIGINT NOT NULL COMMENT '实体ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_entity_tag (entity_id, tag_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实体标签关联表';

-- =============================================
-- 第二层：元数据配置
-- =============================================

-- 10. meta_entity: 实体配置（哪个模型发布后的哪个实例）
CREATE TABLE IF NOT EXISTS meta_entity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    entity_code VARCHAR(128) NOT NULL COMMENT '实体编码（业务标识）',
    entity_name VARCHAR(256) COMMENT '实体名称',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    model_version_id BIGINT NOT NULL COMMENT '模型版本ID（发布版本）',
    model_code VARCHAR(64) NOT NULL COMMENT '模型编码（冗余便于查询）',
    business_id BIGINT COMMENT '关联业务表主键（混表模式时使用）',
    status VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态（ACTIVE/LOCKED/DELETED）',
    lock_reason VARCHAR(256) COMMENT '锁定原因',
    locked_at DATETIME COMMENT '锁定时间',
    locked_by VARCHAR(64) COMMENT '锁定人',
    owner_id VARCHAR(64) COMMENT '拥有者ID（数据权限）',
    owner_type VARCHAR(32) COMMENT '拥有者类型（USER/ROLE/DEPARTMENT）',
    dept_id BIGINT COMMENT '所属部门（数据权限）',
    dept_path VARCHAR(256) COMMENT '部门路径',
    data_version INT DEFAULT 1 COMMENT '数据版本（乐观锁）',
    description TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    modifier VARCHAR(64),
    creator_dept_id BIGINT COMMENT '创建人部门',
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_entity_code (entity_code),
    UNIQUE KEY uk_business_id (model_id, business_id),
    INDEX idx_model_id (model_id),
    INDEX idx_model_version_id (model_version_id),
    INDEX idx_status (status),
    INDEX idx_owner_id (owner_id),
    INDEX idx_dept_id (dept_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='元实体配置表';

-- 11. meta_value: 字段值（entity_id, field_code, value）
CREATE TABLE IF NOT EXISTS meta_value (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    entity_id BIGINT NOT NULL COMMENT '实体ID',
    field_id BIGINT NOT NULL COMMENT '字段ID',
    field_code VARCHAR(64) NOT NULL COMMENT '字段编码（冗余）',
    storage_mode VARCHAR(16) NOT NULL COMMENT '存储模式（COLUMN/JSON/TAG）',
    -- 多态值存储（根据field_type使用不同字段）
    value_text VARCHAR(4000) COMMENT '文本值',
    value_int BIGINT COMMENT '整数值',
    value_decimal DECIMAL(20,6) COMMENT '小数值',
    value_boolean TINYINT COMMENT '布尔值',
    value_date DATE COMMENT '日期值',
    value_datetime DATETIME COMMENT '日期时间值',
    value_json JSON COMMENT 'JSON/对象值',
    value_long_text TEXT COMMENT '长文本值',
    -- 版本管理
    version INT DEFAULT 1 COMMENT '值版本',
    is_current TINYINT DEFAULT 1 COMMENT '是否当前值',
    -- 审计
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    deleted TINYINT DEFAULT 0,
    INDEX idx_entity_id (entity_id),
    INDEX idx_field_id (field_id),
    INDEX idx_field_code (field_code),
    INDEX idx_is_current (is_current),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字段值表';

-- 12. meta_relation: 关系实例
CREATE TABLE IF NOT EXISTS meta_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    rel_id BIGINT NOT NULL COMMENT '关系定义ID',
    rel_code VARCHAR(64) NOT NULL COMMENT '关系编码（冗余）',
    rel_type VARCHAR(16) NOT NULL COMMENT '关系类型（N:1/1:N/N:N/1:1）',
    source_entity_id BIGINT NOT NULL COMMENT '源实体ID',
    source_entity_code VARCHAR(128) COMMENT '源实体编码（冗余）',
    target_entity_id BIGINT NOT NULL COMMENT '目标实体ID',
    target_entity_code VARCHAR(128) COMMENT '目标实体编码（冗余）',
    relation_level VARCHAR(16) DEFAULT 'NORMAL' COMMENT '关系级别（NORMAL/IMPORTANT/CRITICAL）',
    description TEXT COMMENT '关系描述',
    start_date DATE COMMENT '关系开始日期',
    end_date DATE COMMENT '关系结束日期',
    is_active TINYINT DEFAULT 1 COMMENT '是否有效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_rel_instance (rel_id, source_entity_id, target_entity_id),
    INDEX idx_source_entity_id (source_entity_id),
    INDEX idx_target_entity_id (target_entity_id),
    INDEX idx_rel_id (rel_id),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关系实例表';

-- =============================================
-- 审计与权限
-- =============================================

-- 13. meta_audit_log: 元数据操作日志
CREATE TABLE IF NOT EXISTS meta_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    audit_type VARCHAR(32) NOT NULL COMMENT '操作类型（CREATE/UPDATE/DELETE/PUBLISH/ARCHIVE/ROLLBACK）',
    target_type VARCHAR(32) NOT NULL COMMENT '目标类型（MODEL/FIELD/RELATION/ENTITY/VALUE）',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    target_code VARCHAR(128) COMMENT '目标编码',
    target_name VARCHAR(256) COMMENT '目标名称',
    before_value JSON COMMENT '变更前值',
    after_value JSON COMMENT '变更后值',
    change_fields JSON COMMENT '变更字段列表',
    operate_status VARCHAR(16) DEFAULT 'SUCCESS' COMMENT '操作状态',
    error_message TEXT COMMENT '错误信息',
    operate_ip VARCHAR(64) COMMENT '操作IP',
    operate_location VARCHAR(128) COMMENT '操作位置',
    execute_time INT COMMENT '执行时长(ms)',
    operator_id VARCHAR(64) NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(128) COMMENT '操作人名称',
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_target (target_type, target_id),
    INDEX idx_operator_id (operator_id),
    INDEX idx_operate_time (operate_time),
    INDEX idx_audit_type (audit_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='元数据操作日志表';

-- 14. meta_data_permission: 数据权限配置
CREATE TABLE IF NOT EXISTS meta_data_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    permission_code VARCHAR(64) NOT NULL COMMENT '权限编码',
    permission_name VARCHAR(128) NOT NULL COMMENT '权限名称',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    permission_type VARCHAR(16) NOT NULL COMMENT '权限类型（FIELD/ROW/ACTION）',
    field_codes JSON COMMENT '字段编码列表（字段级权限）',
    filter_rule JSON COMMENT '过滤规则（行级权限）',
    allowed_actions JSON COMMENT '允许的操作（CREATE/READ/UPDATE/DELETE）',
    role_id BIGINT COMMENT '关联角色ID',
    description TEXT,
    is_active TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    modifier VARCHAR(64),
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_permission_code (permission_code),
    INDEX idx_model_id (model_id),
    INDEX idx_role_id (role_id),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据权限配置表';

-- 15. meta_sensitive_mask: 敏感数据脱敏规则
CREATE TABLE IF NOT EXISTS meta_sensitive_mask (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    mask_code VARCHAR(64) NOT NULL COMMENT '规则编码',
    mask_name VARCHAR(128) NOT NULL COMMENT '规则名称',
    field_type VARCHAR(32) NOT NULL COMMENT '字段类型',
    mask_pattern VARCHAR(64) NOT NULL COMMENT '脱敏模式（PHONE/ID_CARD/EMAIL/BANK_CARD/CUSTOM）',
    custom_pattern VARCHAR(128) COMMENT '自定义正则',
    replace_format VARCHAR(128) COMMENT '替换格式（如: $1****$2）',
    is_active TINYINT DEFAULT 1,
    description TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_mask_code (mask_code),
    INDEX idx_mask_pattern (mask_pattern),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感数据脱敏规则表';

-- =============================================
-- 初始化敏感数据脱敏规则
-- =============================================
INSERT INTO meta_sensitive_mask (mask_code, mask_name, field_type, mask_pattern, replace_format, description) VALUES
('PHONE', '手机号脱敏', 'PHONE', 'PHONE', NULL, '手机号脱敏：138****5678'),
('ID_CARD', '身份证脱敏', 'ID_CARD', 'ID_CARD', NULL, '身份证脱敏：430*******1234567'),
('EMAIL', '邮箱脱敏', 'EMAIL', 'EMAIL', NULL, '邮箱脱敏：t***@example.com'),
('BANK_CARD', '银行卡脱敏', 'BANK_CARD', 'BANK_CARD', NULL, '银行卡脱敏：6222**********1234');
