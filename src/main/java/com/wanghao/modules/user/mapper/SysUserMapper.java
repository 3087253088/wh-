package com.wanghao.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanghao.modules.user.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM smart_finance.sys_user WHERE username = #{username} AND deleted = 0")
    SysUser selectByUsername(@Param("username") String username);
}
