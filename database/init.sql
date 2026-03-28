-- =============================================
-- 银行IT架构管理平台 - 数据库初始化脚本
-- 版本: V1.2
-- 日期: 2026-03-26
-- 数据库: MySQL 8.0
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS bank_it_arch DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bank_it_arch;

-- =============================================
-- 第一部分：CMDB管理模块 (5张表)
-- =============================================

-- 1. cmdb_server (服务器表)
CREATE TABLE cmdb_server (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    asset_code VARCHAR(64) NOT NULL COMMENT '资源编码（唯一索引）',
    hostname VARCHAR(128) COMMENT '主机名',
    ip_address VARCHAR(64) COMMENT 'IP地址',
    inner_ip VARCHAR(64) COMMENT '内网IP',
    cpu VARCHAR(64) COMMENT 'CPU规格',
    cpu_count INT DEFAULT 0 COMMENT 'CPU核数',
    memory VARCHAR(64) COMMENT '内存规格',
    memory_size INT DEFAULT 0 COMMENT '内存大小(GB)',
    disk VARCHAR(256) COMMENT '磁盘信息',
    disk_size INT DEFAULT 0 COMMENT '磁盘大小(GB)',
    os VARCHAR(128) COMMENT '操作系统',
    os_version VARCHAR(64) COMMENT '操作系统版本',
    server_type VARCHAR(32) DEFAULT '物理机' COMMENT '服务器类型（物理机/虚拟机/容器）',
    status VARCHAR(32) DEFAULT 'OFFLINE' COMMENT '状态',
    department_id BIGINT COMMENT '所属部门',
    department_name VARCHAR(128) COMMENT '所属部门名称',
    team_id BIGINT COMMENT '所属团队',
    team_name VARCHAR(128) COMMENT '所属团队名称',
    idc VARCHAR(64) COMMENT '机房',
    cabinet VARCHAR(64) COMMENT '机柜',
    manufacturer VARCHAR(128) COMMENT '厂商',
    model VARCHAR(128) COMMENT '型号',
    serial_number VARCHAR(128) COMMENT '序列号',
    purchase_date DATE COMMENT '采购日期',
    warranty_expire DATE COMMENT '保修到期',
    remark TEXT COMMENT '备注',
    extra_attrs TEXT COMMENT '扩展属性（JSON）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    UNIQUE KEY uk_asset_code (asset_code),
    INDEX idx_ip_address (ip_address),
    INDEX idx_department_id (department_id),
    INDEX idx_team_id (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务器表';

-- 2. cmdb_network (网络设备表)
CREATE TABLE cmdb_network (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    asset_code VARCHAR(64) NOT NULL COMMENT '资源编码（唯一索引）',
    device_name VARCHAR(128) COMMENT '设备名称',
    device_type VARCHAR(32) DEFAULT '交换机' COMMENT '设备类型（交换机/路由器/防火墙/负载均衡）',
    ip_address VARCHAR(64) COMMENT '管理IP',
    mgmt_vlan VARCHAR(32) COMMENT '管理VLAN',
    port_count INT DEFAULT 0 COMMENT '端口数',
    used_port_count INT DEFAULT 0 COMMENT '已用端口数',
    bandwidth VARCHAR(64) COMMENT '带宽',
    manufacturer VARCHAR(128) COMMENT '厂商',
    model VARCHAR(128) COMMENT '型号',
    serial_number VARCHAR(128) COMMENT '序列号',
    status VARCHAR(32) DEFAULT 'OFFLINE' COMMENT '状态',
    department_id BIGINT COMMENT '所属部门',
    department_name VARCHAR(128) COMMENT '所属部门名称',
    team_id BIGINT COMMENT '所属团队',
    team_name VARCHAR(128) COMMENT '所属团队名称',
    idc VARCHAR(64) COMMENT '机房',
    cabinet VARCHAR(64) COMMENT '机柜',
    purchase_date DATE COMMENT '采购日期',
    warranty_expire DATE COMMENT '保修到期',
    remark TEXT COMMENT '备注',
    extra_attrs TEXT COMMENT '扩展属性（JSON）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    UNIQUE KEY uk_asset_code (asset_code),
    INDEX idx_department_id (department_id),
    INDEX idx_team_id (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网络设备表';

-- 3. cmdb_storage (存储设备表)
CREATE TABLE cmdb_storage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    asset_code VARCHAR(64) NOT NULL COMMENT '资源编码（唯一索引）',
    device_name VARCHAR(128) COMMENT '设备名称',
    device_type VARCHAR(32) DEFAULT 'NAS' COMMENT '设备类型（NAS/SAN/对象存储）',
    total_capacity BIGINT DEFAULT 0 COMMENT '总容量(TB)',
    used_capacity BIGINT DEFAULT 0 COMMENT '已用容量(TB)',
    manufacturer VARCHAR(128) COMMENT '厂商',
    model VARCHAR(128) COMMENT '型号',
    serial_number VARCHAR(128) COMMENT '序列号',
    status VARCHAR(32) DEFAULT 'OFFLINE' COMMENT '状态',
    department_id BIGINT COMMENT '所属部门',
    department_name VARCHAR(128) COMMENT '所属部门名称',
    team_id BIGINT COMMENT '所属团队',
    team_name VARCHAR(128) COMMENT '所属团队名称',
    idc VARCHAR(64) COMMENT '机房',
    purchase_date DATE COMMENT '采购日期',
    warranty_expire DATE COMMENT '保修到期',
    remark TEXT COMMENT '备注',
    extra_attrs TEXT COMMENT '扩展属性（JSON）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    UNIQUE KEY uk_asset_code (asset_code),
    INDEX idx_department_id (department_id),
    INDEX idx_team_id (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储设备表';

-- 4. cmdb_cloud_resource (云资源表)
CREATE TABLE cmdb_cloud_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    resource_code VARCHAR(64) NOT NULL COMMENT '资源编码（唯一索引）',
    resource_name VARCHAR(128) COMMENT '资源名称',
    resource_type VARCHAR(32) DEFAULT '云主机' COMMENT '资源类型（云主机/云数据库/云存储/负载均衡）',
    cloud_provider VARCHAR(64) DEFAULT '行内云' COMMENT '云厂商（阿里云/华为云/腾讯云/行内云）',
    region VARCHAR(64) COMMENT '地域',
    zone VARCHAR(64) COMMENT '可用区',
    specification VARCHAR(128) COMMENT '规格',
    status VARCHAR(32) DEFAULT 'OFFLINE' COMMENT '状态',
    ip_address VARCHAR(64) COMMENT '公网IP',
    inner_ip VARCHAR(64) COMMENT '内网IP',
    cpu INT DEFAULT 0 COMMENT 'CPU核数',
    memory INT DEFAULT 0 COMMENT '内存(GB)',
    disk_size INT DEFAULT 0 COMMENT '磁盘大小(GB)',
    bandwidth INT DEFAULT 0 COMMENT '带宽(Mbps)',
    department_id BIGINT COMMENT '所属部门',
    department_name VARCHAR(128) COMMENT '所属部门名称',
    team_id BIGINT COMMENT '所属团队',
    team_name VARCHAR(128) COMMENT '所属团队名称',
    cost_per_month DECIMAL(10,2) COMMENT '月费用',
    expire_date DATE COMMENT '到期日期',
    remark TEXT COMMENT '备注',
    extra_attrs TEXT COMMENT '扩展属性（JSON）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    UNIQUE KEY uk_resource_code (resource_code),
    INDEX idx_department_id (department_id),
    INDEX idx_team_id (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='云资源表';

-- 5. cmdb_asset_change_log (资产变更日志表)
CREATE TABLE cmdb_asset_change_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    asset_type VARCHAR(32) NOT NULL COMMENT '资产类型（server/network/storage/cloud）',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    asset_code VARCHAR(64) COMMENT '资源编码',
    operate_type VARCHAR(32) NOT NULL COMMENT '操作类型（CREATE/UPDATE/DELETE）',
    before_value TEXT COMMENT '变更前值（JSON）',
    after_value TEXT COMMENT '变更后值（JSON）',
    operator VARCHAR(64) COMMENT '操作人',
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    operate_ip VARCHAR(64) COMMENT '操作IP',
    remark VARCHAR(256) COMMENT '备注',
    INDEX idx_asset (asset_type, asset_id),
    INDEX idx_operate_time (operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产变更日志表';

-- =============================================
-- 第二部分：应用架构模块 (9张表)
-- =============================================

-- 6. arch_application (应用系统表)
CREATE TABLE arch_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    app_code VARCHAR(64) NOT NULL COMMENT '应用编码（唯一）',
    app_name VARCHAR(128) NOT NULL COMMENT '应用名称',
    app_name_en VARCHAR(128) COMMENT '英文名称',
    app_type VARCHAR(32) DEFAULT '交易' COMMENT '应用类型（交易/渠道/管理/数据/基础设施）',
    importance_level VARCHAR(32) DEFAULT '一般' COMMENT '重要级别（核心/重要/一般/辅助）',
    lifecycle VARCHAR(32) DEFAULT 'PLANNING' COMMENT '生命周期阶段',
    business_domain VARCHAR(64) COMMENT '业务域',
    department_id BIGINT COMMENT '所属部门ID',
    department_name VARCHAR(128) COMMENT '所属部门名称',
    team_id BIGINT COMMENT '所属团队ID',
    team_name VARCHAR(128) COMMENT '所属团队名称',
    pm_name VARCHAR(64) COMMENT '产品经理',
    pm_email VARCHAR(128) COMMENT '产品经理邮箱',
    tech_lead VARCHAR(64) COMMENT '技术负责人',
    tech_lead_email VARCHAR(128) COMMENT '技术负责人邮箱',
    description TEXT COMMENT '应用描述',
    business_description TEXT COMMENT '业务描述',
    deployment_type VARCHAR(32) DEFAULT '单机' COMMENT '部署方式（单机/集群/分布式/云原生）',
    access_type VARCHAR(32) DEFAULT '内网' COMMENT '访问方式（内网/外网/内外网）',
    sla_level VARCHAR(32) COMMENT 'SLA级别',
    version VARCHAR(32) COMMENT '当前版本',
    go_live_date DATE COMMENT '上线日期',
    retire_date DATE COMMENT '计划下线日期',
    status VARCHAR(32) DEFAULT 'OFFLINE' COMMENT '状态',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    UNIQUE KEY uk_app_code (app_code),
    INDEX idx_department_id (department_id),
    INDEX idx_team_id (team_id),
    INDEX idx_lifecycle (lifecycle)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用系统表';

-- 7. arch_application_module (应用模块表)
CREATE TABLE arch_application_module (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    app_id BIGINT NOT NULL COMMENT '所属应用ID',
    module_code VARCHAR(64) NOT NULL COMMENT '模块编码',
    module_name VARCHAR(128) NOT NULL COMMENT '模块名称',
    module_type VARCHAR(32) DEFAULT '后端' COMMENT '模块类型（前端/后端/公共/数据）',
    description TEXT COMMENT '模块描述',
    tech_stack VARCHAR(256) COMMENT '技术栈',
    parent_module_id BIGINT COMMENT '父模块ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(32) DEFAULT 'ACTIVE' COMMENT '状态',
    team_id BIGINT COMMENT '所属团队',
    team_name VARCHAR(128) COMMENT '所属团队名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    INDEX idx_app_id (app_id),
    INDEX idx_team_id (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用模块表';

-- 8. arch_service (服务表)
CREATE TABLE arch_service (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    app_id BIGINT NOT NULL COMMENT '所属应用ID',
    module_id BIGINT COMMENT '所属模块ID',
    service_code VARCHAR(64) NOT NULL COMMENT '服务编码',
    service_name VARCHAR(128) NOT NULL COMMENT '服务名称',
    service_type VARCHAR(32) DEFAULT '微服务' COMMENT '服务类型（微服务/API/中间件服务）',
    protocol VARCHAR(32) DEFAULT 'HTTP' COMMENT '协议（HTTP/gRPC/Dubbo/消息）',
    port INT COMMENT '端口',
    context_path VARCHAR(128) COMMENT '上下文路径',
    version VARCHAR(32) COMMENT '版本',
    description TEXT COMMENT '描述',
    status VARCHAR(32) DEFAULT 'ACTIVE' COMMENT '状态',
    team_id BIGINT COMMENT '所属团队',
    team_name VARCHAR(128) COMMENT '所属团队名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    INDEX idx_app_id (app_id),
    INDEX idx_team_id (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务表';

-- 9. arch_dependency (依赖关系表)
CREATE TABLE arch_dependency (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    source_app_id BIGINT NOT NULL COMMENT '源应用ID',
    source_app_code VARCHAR(64) COMMENT '源应用编码',
    source_app_name VARCHAR(128) COMMENT '源应用名称',
    source_service_id BIGINT COMMENT '源服务ID',
    source_service_name VARCHAR(128) COMMENT '源服务名称',
    target_app_id BIGINT NOT NULL COMMENT '目标应用ID',
    target_app_code VARCHAR(64) COMMENT '目标应用编码',
    target_app_name VARCHAR(128) COMMENT '目标应用名称',
    target_service_id BIGINT COMMENT '目标服务ID',
    target_service_name VARCHAR(128) COMMENT '目标服务名称',
    dependency_type VARCHAR(32) DEFAULT '同步' COMMENT '依赖类型（同步/异步/数据）',
    description TEXT COMMENT '依赖描述',
    interface_type VARCHAR(64) DEFAULT 'REST' COMMENT '接口类型（REST/gRPC/消息/数据库）',
    interface_path VARCHAR(256) COMMENT '接口地址',
    remark VARCHAR(256) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    INDEX idx_source_app (source_app_id),
    INDEX idx_target_app (target_app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='依赖关系表';

-- 10. arch_application_server (应用-服务器关联表)
CREATE TABLE arch_application_server (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    server_id BIGINT NOT NULL COMMENT '服务器ID',
    `usage` VARCHAR(128) COMMENT '用途描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    creator VARCHAR(64) COMMENT '创建人',
    UNIQUE KEY uk_app_server (app_id, server_id),
    INDEX idx_app_id (app_id),
    INDEX idx_server_id (server_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用-服务器关联表';

-- 11. arch_application_cloud (应用-云资源关联表)
CREATE TABLE arch_application_cloud (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    cloud_resource_id BIGINT NOT NULL COMMENT '云资源ID',
    `usage` VARCHAR(128) COMMENT '用途描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    creator VARCHAR(64) COMMENT '创建人',
    UNIQUE KEY uk_app_cloud (app_id, cloud_resource_id),
    INDEX idx_app_id (app_id),
    INDEX idx_cloud_id (cloud_resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用-云资源关联表';

-- 12. arch_application_network (应用-网络设备关联表)
CREATE TABLE arch_application_network (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    network_id BIGINT NOT NULL COMMENT '网络设备ID',
    `usage` VARCHAR(128) COMMENT '用途描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    creator VARCHAR(64) COMMENT '创建人',
    UNIQUE KEY uk_app_network (app_id, network_id),
    INDEX idx_app_id (app_id),
    INDEX idx_network_id (network_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用-网络设备关联表';

-- 13. arch_application_storage (应用-存储关联表)
CREATE TABLE arch_application_storage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    storage_id BIGINT NOT NULL COMMENT '存储设备ID',
    `usage` VARCHAR(128) COMMENT '用途描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    creator VARCHAR(64) COMMENT '创建人',
    UNIQUE KEY uk_app_storage (app_id, storage_id),
    INDEX idx_app_id (app_id),
    INDEX idx_storage_id (storage_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用-存储关联表';

-- =============================================
-- 第三部分：技术架构模块 (2张表)
-- =============================================

-- 14. arch_tech_stack (技术栈字典表)
CREATE TABLE arch_tech_stack (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    stack_code VARCHAR(64) NOT NULL COMMENT '技术编码',
    stack_name VARCHAR(128) NOT NULL COMMENT '技术名称',
    stack_type VARCHAR(32) NOT NULL COMMENT '类型（语言/框架/中间件/数据库/操作系统/其他）',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    tree_path VARCHAR(256) DEFAULT '/' COMMENT '层级路径（如 /1/3/5/）',
    version VARCHAR(64) COMMENT '版本',
    vendor VARCHAR(128) COMMENT '供应商',
    license_type VARCHAR(64) COMMENT '许可证类型',
    is_standard TINYINT DEFAULT 0 COMMENT '是否标准技术（1-是，0-否）',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    description TEXT COMMENT '描述',
    usage_guide TEXT COMMENT '使用指南',
    remark VARCHAR(256) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    UNIQUE KEY uk_stack_code (stack_code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_stack_type (stack_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技术栈字典表';

-- 15. arch_deployment_architecture (部署架构表)
CREATE TABLE arch_deployment_architecture (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    app_id BIGINT NOT NULL COMMENT '关联应用ID',
    layer VARCHAR(32) NOT NULL COMMENT '层级（接入层/应用层/数据层/公共层）',
    node_name VARCHAR(128) NOT NULL COMMENT '节点名称',
    node_type VARCHAR(32) COMMENT '节点类型',
    count INT DEFAULT 1 COMMENT '节点数量',
    specification VARCHAR(256) COMMENT '规格',
    deployment_location VARCHAR(128) COMMENT '部署位置',
    description TEXT COMMENT '描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    INDEX idx_app_id (app_id),
    INDEX idx_layer (layer)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部署架构表';

-- =============================================
-- 第四部分：数据架构模块 (3张表)
-- =============================================

-- 16. arch_data_entity (数据实体表)
CREATE TABLE arch_data_entity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    app_id BIGINT NOT NULL COMMENT '所属应用ID',
    entity_code VARCHAR(64) NOT NULL COMMENT '实体编码',
    entity_name VARCHAR(128) NOT NULL COMMENT '实体名称',
    entity_type VARCHAR(32) DEFAULT '表' COMMENT '实体类型（表/集合/文件/消息）',
    db_type VARCHAR(32) DEFAULT 'MySQL' COMMENT '存储类型（MySQL/Oracle/Redis/MongoDB/Kafka）',
    db_name VARCHAR(64) COMMENT '库名',
    table_name VARCHAR(64) COMMENT '表名/集合名',
    sensitivity VARCHAR(32) DEFAULT 'INTERNAL' COMMENT '敏感等级',
    description TEXT COMMENT '描述',
    field_count INT DEFAULT 0 COMMENT '字段数量',
    record_count BIGINT DEFAULT 0 COMMENT '记录数',
    data_volume BIGINT DEFAULT 0 COMMENT '数据量(GB)',
    status VARCHAR(32) DEFAULT 'ACTIVE' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    INDEX idx_app_id (app_id),
    INDEX idx_entity_code (entity_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据实体表';

-- 17. arch_data_entity_field (数据实体字段表)
CREATE TABLE arch_data_entity_field (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    entity_id BIGINT NOT NULL COMMENT '所属实体ID',
    field_name VARCHAR(64) NOT NULL COMMENT '字段名',
    field_type VARCHAR(32) NOT NULL COMMENT '字段类型',
    field_length INT DEFAULT 0 COMMENT '长度',
    is_primary_key TINYINT DEFAULT 0 COMMENT '是否主键',
    is_nullable TINYINT DEFAULT 1 COMMENT '是否可空',
    is_sensitive TINYINT DEFAULT 0 COMMENT '是否敏感',
    default_value VARCHAR(128) COMMENT '默认值',
    description VARCHAR(256) COMMENT '字段描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    INDEX idx_entity_id (entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据实体字段表';

-- 18. arch_data_flow (数据流向表)
CREATE TABLE arch_data_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    flow_code VARCHAR(64) NOT NULL COMMENT '流向编码',
    flow_name VARCHAR(128) NOT NULL COMMENT '流向名称',
    source_app_id BIGINT NOT NULL COMMENT '源应用ID',
    source_app_name VARCHAR(128) COMMENT '源应用名称',
    source_entity_id BIGINT COMMENT '源实体ID',
    source_entity_name VARCHAR(128) COMMENT '源实体名称',
    target_app_id BIGINT NOT NULL COMMENT '目标应用ID',
    target_app_name VARCHAR(128) COMMENT '目标应用名称',
    target_entity_id BIGINT COMMENT '目标实体ID',
    target_entity_name VARCHAR(128) COMMENT '目标实体名称',
    flow_type VARCHAR(32) DEFAULT '同步' COMMENT '流向类型（同步/异步/批量）',
    transfer_type VARCHAR(32) DEFAULT 'CDC' COMMENT '传输方式（CDC/ETL/消息/接口）',
    frequency VARCHAR(32) DEFAULT '实时' COMMENT '频率（实时/定时/批量）',
    data_volume BIGINT DEFAULT 0 COMMENT '数据量(条/天)',
    description TEXT COMMENT '描述',
    remark VARCHAR(256) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    INDEX idx_source_app (source_app_id),
    INDEX idx_target_app (target_app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据流向表';

-- =============================================
-- 第五部分：审批流程模块 (6张表)
-- =============================================

-- 19. wf_definition (流程定义表)
CREATE TABLE wf_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    definition_code VARCHAR(64) NOT NULL COMMENT '流程定义编码',
    definition_name VARCHAR(128) NOT NULL COMMENT '流程名称',
    process_type VARCHAR(32) NOT NULL COMMENT '流程类型（NEW_APP/CHANGE_APP）',
    description TEXT COMMENT '描述',
    version VARCHAR(32) DEFAULT '1.0' COMMENT '版本',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    config TEXT COMMENT '流程配置（JSON）',
    remark VARCHAR(256) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    UNIQUE KEY uk_definition_code (definition_code),
    INDEX idx_process_type (process_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义表';

-- 20. wf_definition_node (流程节点表)
CREATE TABLE wf_definition_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    definition_id BIGINT NOT NULL COMMENT '所属流程定义ID',
    node_code VARCHAR(64) NOT NULL COMMENT '节点编码',
    node_name VARCHAR(128) NOT NULL COMMENT '节点名称',
    node_order INT DEFAULT 0 COMMENT '节点顺序',
    node_type VARCHAR(32) DEFAULT 'APPROVAL' COMMENT '节点类型（APPROVAL/COPY/CONDITION）',
    approver_type VARCHAR(32) DEFAULT 'USER' COMMENT '审批人类型（USER/ROLE/DEPT_HEAD/MANAGER）',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(128) COMMENT '审批人姓名',
    is_required TINYINT DEFAULT 1 COMMENT '是否必须审批',
    approve_type VARCHAR(32) DEFAULT 'ANY' COMMENT '审批方式（ANY/ALL）',
    timeout_hours INT DEFAULT 72 COMMENT '超时时间(小时)',
    auto_pass TINYINT DEFAULT 0 COMMENT '超时自动通过',
    condition_rule TEXT COMMENT '条件规则',
    remark VARCHAR(256) COMMENT '备注',
    INDEX idx_definition_id (definition_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点表';

-- 21. wf_instance (流程实例表)
CREATE TABLE wf_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    instance_code VARCHAR(64) NOT NULL COMMENT '实例编码',
    definition_id BIGINT NOT NULL COMMENT '流程定义ID',
    definition_name VARCHAR(128) COMMENT '流程名称',
    process_type VARCHAR(32) NOT NULL COMMENT '流程类型',
    business_type VARCHAR(32) COMMENT '业务类型',
    business_id BIGINT COMMENT '业务ID',
    business_key VARCHAR(128) COMMENT '业务标识',
    title VARCHAR(256) NOT NULL COMMENT '标题',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(64) NOT NULL COMMENT '申请人姓名',
    applicant_dept_id BIGINT COMMENT '申请人部门ID',
    applicant_dept_name VARCHAR(128) COMMENT '申请人部门名称',
    current_node_id BIGINT COMMENT '当前节点ID',
    current_node_name VARCHAR(128) COMMENT '当前节点名称',
    status VARCHAR(32) DEFAULT 'DRAFT' COMMENT '状态（DRAFT/RUNNING/SUSPENDED/COMPLETED/TERMINATED）',
    priority VARCHAR(32) DEFAULT 'NORMAL' COMMENT '优先级（LOW/NORMAL/HIGH/URGENT）',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration BIGINT DEFAULT 0 COMMENT '时长(秒)',
    result VARCHAR(32) COMMENT '结果（PASS/REJECT）',
    business_data TEXT COMMENT '业务数据（JSON）',
    remark VARCHAR(256) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    UNIQUE KEY uk_instance_code (instance_code),
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_status (status),
    INDEX idx_definition_id (definition_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例表';

-- 22. wf_task (审批任务表)
CREATE TABLE wf_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    instance_id BIGINT NOT NULL COMMENT '所属实例ID',
    node_id BIGINT NOT NULL COMMENT '节点定义ID',
    task_code VARCHAR(64) NOT NULL COMMENT '任务编码',
    task_name VARCHAR(128) NOT NULL COMMENT '任务名称',
    task_type VARCHAR(32) DEFAULT 'APPROVE' COMMENT '任务类型（APPROVE/COPY）',
    assignee_id BIGINT COMMENT '指定审批人ID',
    assignee_name VARCHAR(64) COMMENT '指定审批人姓名',
    status VARCHAR(32) DEFAULT 'PENDING' COMMENT '状态（PENDING/APPROVED/REJECTED/TRANSFERRED/DELEGATED/RETURNED）',
    approve_type VARCHAR(32) DEFAULT 'ANY' COMMENT '审批方式（ANY/ALL）',
    approved_count INT DEFAULT 0 COMMENT '已审批人数',
    required_count INT DEFAULT 1 COMMENT '需审批人数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expire_time DATETIME COMMENT '期望完成时间',
    complete_time DATETIME COMMENT '实际完成时间',
    delegate_from BIGINT COMMENT '委托来源',
    delegate_to BIGINT COMMENT '委托去向',
    opinion TEXT COMMENT '审批意见',
    remark VARCHAR(256) COMMENT '备注',
    INDEX idx_instance_id (instance_id),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批任务表';

-- 23. wf_task_candidate (审批任务候选人表)
CREATE TABLE wf_task_candidate (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    task_id BIGINT NOT NULL COMMENT '所属任务ID',
    candidate_id BIGINT NOT NULL COMMENT '候选人ID',
    candidate_name VARCHAR(64) COMMENT '候选人姓名',
    candidate_type VARCHAR(32) DEFAULT 'USER' COMMENT '候选人类型（USER/ROLE/DEPT_HEAD）',
    is_approved TINYINT DEFAULT 0 COMMENT '是否已审批（0-未审，1-已审）',
    approve_time DATETIME COMMENT '审批时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_id (task_id),
    INDEX idx_candidate_id (candidate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批任务候选人表';

-- 24. wf_history (审批历史表)
CREATE TABLE wf_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    instance_id BIGINT NOT NULL COMMENT '所属实例ID',
    task_id BIGINT COMMENT '关联任务ID',
    node_id BIGINT COMMENT '节点定义ID',
    node_name VARCHAR(128) COMMENT '节点名称',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(64) NOT NULL COMMENT '操作人姓名',
    operator_dept_name VARCHAR(128) COMMENT '操作人部门',
    action VARCHAR(32) NOT NULL COMMENT '操作（SUBMIT/APPROVE/REJECT/RETURN/TRANSFER/DELEGATE/WITHDRAW）',
    opinion TEXT COMMENT '审批意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    duration BIGINT DEFAULT 0 COMMENT '处理时长(秒)',
    INDEX idx_instance_id (instance_id),
    INDEX idx_operator_id (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批历史表';

-- =============================================
-- 第六部分：权限管理模块 (12张表)
-- =============================================

-- 25. sys_user (用户表)
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username VARCHAR(64) NOT NULL COMMENT '用户名（唯一）',
    password VARCHAR(128) NOT NULL COMMENT '密码（加密）',
    real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
    email VARCHAR(128) COMMENT '邮箱',
    phone VARCHAR(32) COMMENT '手机号',
    avatar VARCHAR(256) COMMENT '头像URL',
    department_id BIGINT COMMENT '部门ID',
    department_name VARCHAR(128) COMMENT '部门名称',
    status VARCHAR(32) DEFAULT 'ENABLED' COMMENT '状态（ENABLED/DISABLED/LOCKED）',
    user_type VARCHAR(32) DEFAULT 'LOCAL' COMMENT '用户类型（LOCAL/LDAP）',
    ldap_dn VARCHAR(256) COMMENT 'LDAP DN',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(64) COMMENT '最后登录IP',
    password_expire_time DATETIME COMMENT '密码过期时间',
    is_first_login TINYINT DEFAULT 1 COMMENT '首次登录',
    remark VARCHAR(256) COMMENT '备注',
    team_id BIGINT COMMENT '所属团队',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    UNIQUE KEY uk_username (username),
    INDEX idx_department_id (department_id),
    INDEX idx_team_id (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 26. sys_role (角色表)
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码（唯一）',
    role_name VARCHAR(128) NOT NULL COMMENT '角色名称',
    role_type VARCHAR(32) DEFAULT 'BUSINESS' COMMENT '角色类型（SYSTEM/BUSINESS）',
    data_scope VARCHAR(32) DEFAULT 'DEPT' COMMENT '数据权限范围（ALL/DEPT/DEPT_CHILDREN/SELF）',
    description TEXT COMMENT '描述',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    team_id BIGINT COMMENT '所属团队',
    remark VARCHAR(256) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    UNIQUE KEY uk_role_code (role_code),
    INDEX idx_team_id (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 27. sys_menu (菜单表)
CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    tree_path VARCHAR(256) DEFAULT '/' COMMENT '层级路径（如 /1/3/5/）',
    menu_code VARCHAR(64) NOT NULL COMMENT '菜单编码',
    menu_name VARCHAR(128) NOT NULL COMMENT '菜单名称',
    menu_type VARCHAR(32) NOT NULL COMMENT '菜单类型（CATALOG/MENU/BUTTON）',
    icon VARCHAR(64) COMMENT '图标',
    path VARCHAR(256) COMMENT '路由路径',
    component VARCHAR(256) COMMENT '组件路径',
    redirect VARCHAR(256) COMMENT '重定向路径',
    is_visible TINYINT DEFAULT 1 COMMENT '是否显示（0-隐藏，1-显示）',
    is_cache TINYINT DEFAULT 0 COMMENT '是否缓存',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    external_link VARCHAR(256) COMMENT '外部链接',
    permission VARCHAR(128) COMMENT '权限标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    UNIQUE KEY uk_menu_code (menu_code),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 28. sys_permission (权限表)
CREATE TABLE sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    permission_code VARCHAR(64) NOT NULL COMMENT '权限编码',
    permission_name VARCHAR(128) NOT NULL COMMENT '权限名称',
    permission_type VARCHAR(32) NOT NULL COMMENT '权限类型（BUTTON/DATA）',
    menu_id BIGINT COMMENT '关联菜单ID（用于按钮权限关联到菜单）',
    resource_type VARCHAR(32) COMMENT '资源类型（用于数据权限）',
    resource_id BIGINT COMMENT '资源ID（用于数据权限）',
    permission VARCHAR(64) COMMENT '按钮权限标识',
    method VARCHAR(16) COMMENT '请求方法',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    UNIQUE KEY uk_permission_code (permission_code),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 29. sys_user_role (用户角色关联表)
CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 30. sys_role_permission (角色权限关联表)
CREATE TABLE sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 31. sys_operation_log (操作日志表)
CREATE TABLE sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    module VARCHAR(64) COMMENT '模块',
    operation VARCHAR(64) COMMENT '操作',
    operation_type VARCHAR(32) COMMENT '操作类型',
    request_method VARCHAR(16) COMMENT '请求方法',
    request_url VARCHAR(256) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    request_body TEXT COMMENT '请求体',
    response TEXT COMMENT '响应结果',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(64) COMMENT '操作人姓名',
    operator_ip VARCHAR(64) COMMENT '操作IP',
    operator_location VARCHAR(128) COMMENT '操作地点',
    execute_time INT DEFAULT 0 COMMENT '执行时间(ms)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_operator_id (operator_id),
    INDEX idx_module (module),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 32. sys_department (部门表)
CREATE TABLE sys_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    tree_path VARCHAR(256) DEFAULT '/' COMMENT '层级路径（如 /1/3/5/）',
    department_code VARCHAR(64) NOT NULL COMMENT '部门编码（唯一）',
    department_name VARCHAR(128) NOT NULL COMMENT '部门名称',
    leader_id BIGINT COMMENT '负责人ID',
    leader_name VARCHAR(64) COMMENT '负责人姓名',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    UNIQUE KEY uk_department_code (department_code),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 33. sys_team (团队表)
CREATE TABLE sys_team (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    department_id BIGINT NOT NULL COMMENT '所属部门ID',
    team_code VARCHAR(64) NOT NULL COMMENT '团队编码（唯一）',
    team_name VARCHAR(128) NOT NULL COMMENT '团队名称',
    leader_id BIGINT COMMENT '团队负责人ID',
    leader_name VARCHAR(64) COMMENT '团队负责人姓名',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    UNIQUE KEY uk_team_code (team_code),
    INDEX idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队表';

-- 34. sys_dict (字典表)
CREATE TABLE sys_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    dict_code VARCHAR(64) NOT NULL COMMENT '字典编码（唯一）',
    dict_name VARCHAR(128) NOT NULL COMMENT '字典名称',
    dict_type VARCHAR(32) DEFAULT 'SYSTEM' COMMENT '字典类型（SYSTEM/BUSINESS）',
    description VARCHAR(256) COMMENT '描述',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    UNIQUE KEY uk_dict_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典表';

-- 35. sys_dict_item (字典项表)
CREATE TABLE sys_dict_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    dict_id BIGINT NOT NULL COMMENT '所属字典ID',
    item_code VARCHAR(64) NOT NULL COMMENT '字典项编码',
    item_name VARCHAR(128) NOT NULL COMMENT '字典项名称',
    item_value VARCHAR(256) NOT NULL COMMENT '字典项值',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认值',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    description VARCHAR(256) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator VARCHAR(64) COMMENT '创建人',
    modifier VARCHAR(64) COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删，1-已删）',
    INDEX idx_dict_id (dict_id),
    UNIQUE KEY uk_dict_item (dict_id, item_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典项表';

-- 36. sys_file_record (文件操作记录表)
CREATE TABLE sys_file_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    file_name VARCHAR(256) NOT NULL COMMENT '文件名',
    file_path VARCHAR(512) NOT NULL COMMENT '文件路径',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    file_type VARCHAR(32) COMMENT '文件类型',
    operate_type VARCHAR(32) NOT NULL COMMENT '操作类型（IMPORT/EXPORT）',
    business_type VARCHAR(32) COMMENT '业务类型',
    status VARCHAR(32) DEFAULT 'SUCCESS' COMMENT '状态（SUCCESS/FAIL）',
    error_msg VARCHAR(512) COMMENT '错误信息',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(64) COMMENT '操作人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_operator_id (operator_id),
    INDEX idx_operate_type (operate_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件操作记录表';

-- 37. sys_message (消息通知表)
CREATE TABLE sys_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    msg_type VARCHAR(32) NOT NULL COMMENT '消息类型（APPROVE/TODO/SYSTEM）',
    title VARCHAR(256) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    business_type VARCHAR(32) COMMENT '业务类型',
    business_id BIGINT COMMENT '业务ID',
    sender_id BIGINT COMMENT '发送人ID',
    sender_name VARCHAR(64) COMMENT '发送人姓名',
    receiver_id BIGINT NOT NULL COMMENT '接收人ID',
    receiver_name VARCHAR(64) COMMENT '接收人姓名',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读（0-未读，1-已读）',
    read_time DATETIME COMMENT '阅读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_is_read (is_read),
    INDEX idx_msg_type (msg_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入超级管理员角色
INSERT INTO sys_role (role_code, role_name, role_type, data_scope, description, is_active) VALUES
('ROLE_SUPER_ADMIN', '超级管理员', 'SYSTEM', 'ALL', '系统全部权限', 1),
('ROLE_ADMIN', '系统管理员', 'SYSTEM', 'ALL', '系统配置、用户管理、权限配置', 1),
('ROLE_ARCHITECT', '架构师', 'BUSINESS', 'DEPT', '架构资产管理、编辑', 1),
('ROLE_DEVELOPER', '开发人员', 'BUSINESS', 'DEPT', '查看架构、发起变更申请', 1),
('ROLE_PM', '项目经理', 'BUSINESS', 'DEPT', '查看架构、了解系统归属', 1),
('ROLE_IT_MANAGER', '科技管理层', 'BUSINESS', 'DEPT', '查看报表和总览', 1),
('ROLE_AUDITOR', '审计人员', 'BUSINESS', 'ALL', '只读、查看变更历史', 1);

-- 插入默认管理员用户 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, email, status, user_type) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@bank.com', 'ENABLED', 'LOCAL');

-- 插入超级管理员-用户关联
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 插入系统菜单
INSERT INTO sys_menu (parent_id, menu_code, menu_name, menu_type, icon, path, component, is_visible, sort_order) VALUES
(0, 'DASHBOARD', '仪表盘', 'CATALOG', 'el-icon-monitor', '/dashboard', NULL, 1, 1),
(0, 'CMDB', 'CMDB管理', 'CATALOG', 'el-icon-s-management', '/cmdb', NULL, 1, 2),
(0, 'ARCH', '应用架构', 'CATALOG', 'el-icon-s-grid', '/arch', NULL, 1, 3),
(0, 'TECH', '技术架构', 'CATALOG', 'el-icon-s-opportunity', '/tech', NULL, 1, 4),
(0, 'DATA', '数据架构', 'CATALOG', 'el-icon-s-data', '/data', NULL, 1, 5),
(0, 'WF', '审批流程', 'CATALOG', 'el-icon-s-claim', '/wf', NULL, 1, 6),
(0, 'REPORT', '报表中心', 'CATALOG', 'el-icon-s-report', '/report', NULL, 1, 7),
(0, 'SYSTEM', '系统管理', 'CATALOG', 'el-icon-s-setting', '/system', NULL, 1, 8);

-- 插入子菜单
INSERT INTO sys_menu (parent_id, menu_code, menu_name, menu_type, icon, path, component, is_visible, sort_order) VALUES
(1, 'DASHBOARD_HOME', '首页', 'MENU', 'el-icon-home', '/dashboard', 'dashboard/index', 1, 1),
(2, 'CMDB_OVERVIEW', '概览', 'MENU', 'el-icon-data-line', '/cmdb/overview', 'cmdb/overview', 1, 1),
(2, 'CMDB_SERVER', '服务器', 'MENU', 'el-icon-box', '/cmdb/servers', 'cmdb/servers', 1, 2),
(2, 'CMDB_NETWORK', '网络设备', 'MENU', 'el-icon-connection', '/cmdb/network', 'cmdb/network', 1, 3),
(2, 'CMDB_STORAGE', '存储设备', 'MENU', 'el-icon-document', '/cmdb/storage', 'cmdb/storage', 1, 4),
(2, 'CMDB_CLOUD', '云资源', 'MENU', 'el-icon-cloud', '/cmdb/cloud', 'cmdb/cloud', 1, 5),
(3, 'ARCH_APPLICATION', '应用列表', 'MENU', 'el-icon-document', '/arch/application', 'arch/application', 1, 1),
(3, 'ARCH_DEPENDENCY', '依赖关系', 'MENU', 'el-icon-s-release', '/arch/dependency', 'arch/dependency', 1, 2),
(4, 'TECH_OVERVIEW', '技术概览', 'MENU', 'el-icon-pie-chart', '/tech/overview', 'tech/overview', 1, 1),
(4, 'TECH_STACK', '技术栈', 'MENU', 'el-icon-coin', '/tech/stack', 'tech/stack', 1, 2),
(4, 'TECH_DEPLOYMENT', '部署架构', 'MENU', 'el-icon-s-management', '/tech/deployment', 'tech/deployment', 1, 3),
(5, 'DATA_ENTITY', '数据实体', 'MENU', 'el-icon-coin', '/data/entity', 'data/entity', 1, 1),
(5, 'DATA_FLOW', '数据流向', 'MENU', 'el-icon-right', '/data/flow', 'data/flow', 1, 2),
(5, 'DATA_DIST', '数据分布', 'MENU', 'el-icon-data-analysis', '/data/distribution', 'data/distribution', 1, 3),
(6, 'WF_TODO', '待办任务', 'MENU', 'el-icon-tickets', '/wf/todo', 'wf/todo', 1, 1),
(6, 'WF_DONE', '已办任务', 'MENU', 'el-icon-circle-check', '/wf/done', 'wf/done', 1, 2),
(6, 'WF_DEFINITION', '流程定义', 'MENU', 'el-icon-s-order', '/wf/definition', 'wf/definition', 1, 3),
(7, 'REPORT_STAT', '统计报表', 'MENU', 'el-icon-data-line', '/report/statistics', 'report/statistics', 1, 1),
(7, 'REPORT_PANORAMA', '全景图', 'MENU', 'el-icon-picture', '/report/panorama', 'report/panorama', 1, 2),
(7, 'REPORT_COMPLIANCE', '合规报告', 'MENU', 'el-icon-document-checked', '/report/compliance', 'report/compliance', 1, 3),
(8, 'SYSTEM_USER', '用户管理', 'MENU', 'el-icon-user', '/system/user', 'system/user', 1, 1),
(8, 'SYSTEM_ROLE', '角色管理', 'MENU', 'el-icon-postcard', '/system/role', 'system/role', 1, 2),
(8, 'SYSTEM_MENU', '菜单管理', 'MENU', 'el-icon-menu', '/system/menu', 'system/menu', 1, 3),
(8, 'SYSTEM_LOG', '操作日志', 'MENU', 'el-icon-document', '/system/log', 'system/log', 1, 4);

-- 插入字典数据
INSERT INTO sys_dict (dict_code, dict_name, dict_type, is_active) VALUES
('ASSET_STATUS', '资产状态', 'SYSTEM', 1),
('SERVER_TYPE', '服务器类型', 'SYSTEM', 1),
('NETWORK_DEVICE_TYPE', '网络设备类型', 'SYSTEM', 1),
('STORAGE_DEVICE_TYPE', '存储设备类型', 'SYSTEM', 1),
('CLOUD_RESOURCE_TYPE', '云资源类型', 'SYSTEM', 1),
('CLOUD_PROVIDER', '云厂商', 'SYSTEM', 1),
('APP_TYPE', '应用类型', 'SYSTEM', 1),
('IMPORTANCE_LEVEL', '重要级别', 'SYSTEM', 1),
('LIFECYCLE', '生命周期', 'SYSTEM', 1),
('DEPLOYMENT_TYPE', '部署方式', 'SYSTEM', 1),
('ACCESS_TYPE', '访问方式', 'SYSTEM', 1),
('WORKFLOW_STATUS', '流程状态', 'SYSTEM', 1),
('TASK_STATUS', '任务状态', 'SYSTEM', 1);

-- 插入字典项
INSERT INTO sys_dict_item (dict_id, item_code, item_name, item_value, is_default, is_active, sort_order) VALUES
-- 资产状态
(1, 'ONLINE', '运行中', 'ONLINE', 1, 1, 1),
(1, 'OFFLINE', '已下线', 'OFFLINE', 0, 1, 2),
(1, 'MAINTENANCE', '维护中', 'MAINTENANCE', 0, 1, 3),
(1, 'STOCK', '库存', 'STOCK', 0, 1, 4),
-- 服务器类型
(2, 'PHYSICAL', '物理机', '物理机', 1, 1, 1),
(2, 'VIRTUAL', '虚拟机', '虚拟机', 0, 1, 2),
(2, 'CONTAINER', '容器', '容器', 0, 1, 3),
-- 应用类型
(7, 'TRANSACTION', '交易类', '交易', 1, 1, 1),
(7, 'CHANNEL', '渠道类', '渠道', 0, 1, 2),
(7, 'MANAGEMENT', '管理类', '管理', 0, 1, 3),
(7, 'DATA', '数据类', '数据', 0, 1, 4),
(7, 'INFRA', '基础设施类', '基础设施', 0, 1, 5),
-- 重要级别
(8, 'CORE', '核心', '核心', 0, 1, 1),
(8, 'IMPORTANT', '重要', '重要', 0, 1, 2),
(8, 'NORMAL', '一般', '一般', 1, 1, 3),
(8, 'AUXILIARY', '辅助', '辅助', 0, 1, 4),
-- 生命周期
(9, 'PLANNING', '规划中', 'PLANNING', 0, 1, 1),
(9, 'DEVELOPMENT', '开发中', 'DEVELOPMENT', 0, 1, 2),
(9, 'TESTING', '测试中', 'TESTING', 0, 1, 3),
(9, 'PRODUCTION', '生产运行', 'PRODUCTION', 1, 1, 4),
(9, 'DEPRECATED', '已废弃', 'DEPRECATED', 0, 1, 5),
-- 流程状态
(12, 'DRAFT', '草稿', 'DRAFT', 0, 1, 1),
(12, 'RUNNING', '进行中', 'RUNNING', 1, 1, 2),
(12, 'SUSPENDED', '已暂停', 'SUSPENDED', 0, 1, 3),
(12, 'COMPLETED', '已完成', 'COMPLETED', 0, 1, 4),
(12, 'TERMINATED', '已终止', 'TERMINATED', 0, 1, 5),
-- 任务状态
(13, 'PENDING', '待处理', 'PENDING', 1, 1, 1),
(13, 'APPROVED', '已通过', 'APPROVED', 0, 1, 2),
(13, 'REJECTED', '已拒绝', 'REJECTED', 0, 1, 3),
(13, 'RETURNED', '已退回', 'RETURNED', 0, 1, 4),
(13, 'DELEGATED', '已委托', 'DELEGATED', 0, 1, 5);

-- =============================================
-- 创建默认应用审批流程
-- =============================================
INSERT INTO wf_definition (definition_code, definition_name, process_type, description, is_active, config) VALUES
('WF_NEW_APP', '新建应用审批', 'NEW_APP', '新建应用多级审批流程', 1, '{"nodes":[{"code":"dept_approval","name":"部门负责人审批","type":"APPROVAL","approverType":"DEPT_HEAD","isRequired":true},{"code":"arch_approval","name":"架构委员会审批","type":"APPROVAL","approverType":"ROLE","approverId":3,"isRequired":false,"condition":"importanceLevel==核心"},{"code":"it_approval","name":"科技管理层审批","type":"APPROVAL","approverType":"MANAGER","isRequired":false,"condition":"importanceLevel==核心||importanceLevel==重要"}]}');

SELECT 'Database initialization completed! 37 tables created.' AS Result;
