package com.bank.itarch.meta.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.meta.constant.MetaStatus;
import com.bank.itarch.meta.mapper.MetaAuditLogMapper;
import com.bank.itarch.meta.mapper.MetaFieldMapper;
import com.bank.itarch.meta.model.entity.MetaAuditLog;
import com.bank.itarch.meta.model.entity.MetaField;
import com.bank.itarch.meta.service.MetaAuditService;
import com.bank.itarch.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetaAuditServiceImpl implements MetaAuditService {

    private final MetaAuditLogMapper metaAuditLogMapper;
    private final MetaFieldMapper metaFieldMapper;

    @Override
    public void logOperation(String auditType, String targetType, Long targetId,
                            String targetCode, String targetName,
                            Object beforeValue, Object afterValue,
                            List<String> changeFields) {
        try {
            MetaAuditLog log = new MetaAuditLog();
            log.setAuditType(auditType);
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            log.setTargetCode(targetCode);
            log.setTargetName(targetName);
            log.setBeforeValue(beforeValue != null ? JSON.toJSONString(beforeValue) : null);
            log.setAfterValue(afterValue != null ? JSON.toJSONString(afterValue) : null);
            log.setChangeFields(changeFields != null ? JSON.toJSONString(changeFields) : null);
            log.setOperateStatus("SUCCESS");
            log.setOperatorId(String.valueOf(UserContext.getUser().getUserId()));
            log.setOperatorName(UserContext.getUser().getUsername());
            log.setOperateTime(LocalDateTime.now());

            metaAuditLogMapper.insert(log);
        } catch (Exception e) {
            MetaAuditServiceImpl.log.error("记录审计日志失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public PageResult<MetaAuditLog> pageQuery(PageQuery query, String targetType,
                                               Long targetId, String operatorId,
                                               String auditType, String startTime,
                                               String endTime) {
        Page<MetaAuditLog> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<MetaAuditLog> wrapper = new LambdaQueryWrapper<>();

        if (targetType != null && !targetType.isEmpty()) {
            wrapper.eq(MetaAuditLog::getTargetType, targetType);
        }
        if (targetId != null) {
            wrapper.eq(MetaAuditLog::getTargetId, targetId);
        }
        if (operatorId != null && !operatorId.isEmpty()) {
            wrapper.eq(MetaAuditLog::getOperatorId, operatorId);
        }
        if (auditType != null && !auditType.isEmpty()) {
            wrapper.eq(MetaAuditLog::getAuditType, auditType);
        }

        wrapper.orderByDesc(MetaAuditLog::getOperateTime);
        IPage<MetaAuditLog> pageResult = metaAuditLogMapper.selectPage(page, wrapper);

        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(),
                query.getPage(), query.getPageSize());
    }

    @Override
    public List<MetaAuditLog> getChangeHistory(String targetType, Long targetId) {
        LambdaQueryWrapper<MetaAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaAuditLog::getTargetType, targetType)
                .eq(MetaAuditLog::getTargetId, targetId)
                .orderByDesc(MetaAuditLog::getOperateTime);
        return metaAuditLogMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> maskSensitiveData(Long modelId, Map<String, Object> data) {
        // 获取敏感字段
        LambdaQueryWrapper<MetaField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaField::getModelId, modelId)
                .eq(MetaField::getIsSensitive, true);
        List<MetaField> sensitiveFields = metaFieldMapper.selectList(wrapper);

        Map<String, Object> masked = new HashMap<>(data);
        for (MetaField field : sensitiveFields) {
            if (masked.containsKey(field.getFieldCode())) {
                Object value = masked.get(field.getFieldCode());
                masked.put(field.getFieldCode(), mask(value, field.getSensitiveType()));
            }
        }

        return masked;
    }

    // ========== 私有方法 ==========

    /**
     * 脱敏处理
     */
    private String mask(Object value, String sensitiveType) {
        if (value == null) return null;
        String str = String.valueOf(value);
        if (str.isEmpty()) return str;

        switch (sensitiveType) {
            case "PHONE":
                return maskPhone(str);
            case "ID_CARD":
                return maskIdCard(str);
            case "EMAIL":
                return maskEmail(str);
            case "BANK_CARD":
                return maskBankCard(str);
            default:
                return "****";
        }
    }

    private String maskPhone(String phone) {
        if (phone.length() == 11) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
        return "****";
    }

    private String maskIdCard(String idCard) {
        if (idCard.length() >= 18) {
            return idCard.substring(0, 6) + "********" + idCard.substring(14);
        }
        return "****";
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex > 2) {
            return email.substring(0, 2) + "***" + email.substring(atIndex);
        }
        return "***";
    }

    private String maskBankCard(String bankCard) {
        if (bankCard.length() >= 8) {
            return bankCard.substring(0, 4) + "****" + bankCard.substring(bankCard.length() - 4);
        }
        return "****";
    }
}
