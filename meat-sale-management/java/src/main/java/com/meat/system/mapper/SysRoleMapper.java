package com.meat.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meat.system.entity.SysRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    void deleteRoleMenus(@Param("roleId") Long roleId);

    @Insert("INSERT INTO sys_role_menu (role_id, menu_id) VALUES (#{roleId}, #{menuId})")
    void insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    @Delete("DELETE FROM sys_user_role WHERE role_id = #{roleId}")
    void deleteRoleUsers(@Param("roleId") Long roleId);

    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
}
