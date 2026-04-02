package com.bank.itarch.meta.service;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.meta.model.entity.MetaAuditLog;

import java.util.List;
import java.util.Map;

/**
 * 审计服务接口
 */
public interface MetaAuditService {

    /**
     * 记录操作日志
     *
     * @param auditType 操作类型
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param targetCode 目标编码
     * @param targetName 目标名称
     * @param beforeValue 变更前值
     * @param afterValue 变更后值
     * @param changeFields 变更字段
     */
    void logOperation(String auditType, String targetType, Long targetId, String targetCode,
                      String targetName, Object beforeValue, Object afterValue, List<String> changeFields);

    /**
     * 分页查询审计日志
     *
     * @param query 分页参数
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param operatorId 操作人ID
     * @param auditType 操作类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    PageResult<MetaAuditLog> pageQuery(PageQuery query, String targetType, Long targetId,
                                        String operatorId, String auditType,
                                        String startTime, String endTime);

    /**
     * 获取实体的变更历史
     *
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 变更历史
     */
    List<MetaAuditLog> getChangeHistory(String targetType, Long targetId);

    /**
     * 数据脱敏
     *
     * @param modelId 模型ID
     * @param data 原始数据
     * @return 脱敏后的数据
     */
    Map<String, Object> maskSensitiveData(Long modelId, Map<String, Object> data);
}
