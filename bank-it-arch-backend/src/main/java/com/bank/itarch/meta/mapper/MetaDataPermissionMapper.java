package com.bank.itarch.meta.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bank.itarch.meta.model.entity.MetaDataPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据权限配置Mapper
 */
@Mapper
public interface MetaDataPermissionMapper extends BaseMapper<MetaDataPermission> {
}
