package com.bank.itarch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bank.itarch.model.entity.OssSoftwareBaseline;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OssSoftwareBaselineMapper extends BaseMapper<OssSoftwareBaseline> {

    @Select("<script>" +
            "SELECT b.id as baseline_id, b.sw_id, b.sw_name, b.sw_version, b.sw_category, " +
            "       b.ver_type, b.is_main_use, b.media_addr, " +
            "       i.recommended_version " +
            "FROM oss_software_baseline b " +
            "LEFT JOIN oss_software_info i ON CAST(b.sw_id AS CHAR) = CAST(i.id AS CHAR) " +
            "WHERE b.logic_status = 0 " +
            "<if test='swName != null and swName != \"\"'> AND b.sw_name LIKE CONCAT('%', #{swName}, '%') </if>" +
            "<if test='swVersion != null and swVersion != \"\"'> AND b.sw_version LIKE CONCAT('%', #{swVersion}, '%') </if>" +
            "<if test='verType != null and verType != \"\"'> AND b.ver_type = #{verType} </if>" +
            "<if test='isMainUse != null and isMainUse != \"\"'> AND b.is_main_use = #{isMainUse} </if>" +
            "<if test='swCategory != null and swCategory != \"\"'> AND b.sw_category = #{swCategory} </if>" +
            "ORDER BY b.create_datetime DESC" +
            "</script>")
    List<Map<String, Object>> selectMediaList(@Param("swName") String swName,
                                              @Param("swVersion") String swVersion,
                                              @Param("verType") String verType,
                                              @Param("isMainUse") String isMainUse,
                                              @Param("swCategory") String swCategory);
}
