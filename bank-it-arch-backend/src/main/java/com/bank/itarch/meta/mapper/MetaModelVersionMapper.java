package com.bank.itarch.meta.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bank.itarch.meta.model.entity.MetaModelVersion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模型版本管理Mapper
 */
@Mapper
public interface MetaModelVersionMapper extends BaseMapper<MetaModelVersion> {
}
