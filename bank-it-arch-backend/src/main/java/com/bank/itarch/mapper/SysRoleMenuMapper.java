package com.bank.itarch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bank.itarch.model.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    @Select("<script>" +
            "SELECT menu_id FROM sys_role_menu WHERE role_id IN " +
            "<foreach item='item' index='index' collection='roleIds' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    List<Long> selectMenuIdsByRoleIds(@Param("roleIds") List<Long> roleIds);

    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    void deleteByRoleId(@Param("roleId") Long roleId);

    @Insert("<script>" +
            "INSERT INTO sys_role_menu (role_id, menu_id, create_time) VALUES " +
            "<foreach item='item' index='index' collection='menuIds' separator=','>" +
            "(#{roleId}, #{item}, NOW())" +
            "</foreach>" +
            "</script>")
    void insertBatch(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
}
