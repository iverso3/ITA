package com.bank.itarch.meta.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bank.itarch.meta.model.entity.MetaField;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字段定义Mapper
 */
@Mapper
public interface MetaFieldMapper extends BaseMapper<MetaField> {
}
