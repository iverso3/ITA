package com.bank.itarch.meta.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bank.itarch.meta.model.entity.MetaAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 元数据操作日志Mapper
 */
@Mapper
public interface MetaAuditLogMapper extends BaseMapper<MetaAuditLog> {
}
