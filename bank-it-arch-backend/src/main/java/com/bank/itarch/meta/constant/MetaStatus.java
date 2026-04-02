package com.bank.itarch.meta.constant;

/**
 * 元模型状态常量
 */
public interface MetaStatus {

    // ========== 模型状态 ==========
    String MODEL_DRAFT = "DRAFT";
    String MODEL_PUBLISHED = "PUBLISHED";
    String MODEL_ARCHIVED = "ARCHIVED";

    // ========== 版本状态 ==========
    String VERSION_DRAFT = "DRAFT";
    String VERSION_TESTING = "TESTING";
    String VERSION_PUBLISHED = "PUBLISHED";
    String VERSION_ARCHIVED = "ARCHIVED";

    // ========== 发布记录状态 ==========
    String RECORD_PENDING = "PENDING";
    String RECORD_PROCESSING = "PROCESSING";
    String RECORD_SUCCESS = "SUCCESS";
    String RECORD_FAILED = "FAILED";

    // ========== 操作类型 ==========
    String ACTION_PUBLISH = "PUBLISH";
    String ACTION_ARCHIVE = "ARCHIVE";
    String ACTION_ROLLBACK = "ROLLBACK";

    // ========== 实体状态 ==========
    String ENTITY_ACTIVE = "ACTIVE";
    String ENTITY_LOCKED = "LOCKED";
    String ENTITY_DELETED = "DELETED";

    // ========== 审计类型 ==========
    String AUDIT_CREATE = "CREATE";
    String AUDIT_UPDATE = "UPDATE";
    String AUDIT_DELETE = "DELETE";
    String AUDIT_PUBLISH = "PUBLISH";
    String AUDIT_ARCHIVE = "ARCHIVE";
    String AUDIT_ROLLBACK = "ROLLBACK";
}
