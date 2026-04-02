package com.bank.itarch.meta.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bank.itarch.meta.model.entity.MetaEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 元实体配置Mapper
 */
@Mapper
public interface MetaEntityMapper extends BaseMapper<MetaEntity> {
}
