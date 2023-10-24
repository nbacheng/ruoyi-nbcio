package com.ruoyi.system.mapper;


import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface CommonMapper extends BaseMapper<Object> {
	//根据角色id获取用户列表id
    @Select("SELECT user_id FROM sys_user_role WHERE role_id = #{roleId}")
    List<Long> selectUserIdByRoleId(@Param("roleId") Long roleId);
}
