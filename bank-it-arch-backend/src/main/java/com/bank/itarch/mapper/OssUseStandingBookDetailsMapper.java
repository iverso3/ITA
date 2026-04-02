package com.bank.itarch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bank.itarch.model.entity.OssUseStandingBookDetails;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OssUseStandingBookDetailsMapper extends BaseMapper<OssUseStandingBookDetails> {
    /**
     * 根据 swName + swVersion + appNo 硬删除 detail 表数据
     */
    @Select("DELETE FROM oss_use_standing_book_details_info WHERE sw_name = #{swName} AND sw_version = #{swVersion} AND app_no = #{appNo}")
    Integer hardDeleteBySwKey(@Param("swName") String swName, @Param("swVersion") String swVersion, @Param("appNo") String appNo);
}