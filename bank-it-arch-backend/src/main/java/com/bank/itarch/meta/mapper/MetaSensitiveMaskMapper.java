package com.bank.itarch.meta.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bank.itarch.meta.model.entity.MetaSensitiveMask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 敏感数据脱敏规则Mapper
 */
@Mapper
public interface MetaSensitiveMaskMapper extends BaseMapper<MetaSensitiveMask> {
}
