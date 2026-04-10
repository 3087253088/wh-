package com.wanghao.modules.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String name;
    private Long deptId;
    private String position;
    private String roleCode;
    private String phone;
    private String email;
    private String avatar;
    private Integer status;
    private Date lastLoginTime;
    private String lastLoginIp;
    private Date entryDate;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    private String createBy;
    private String updateBy;
    @TableLogic
    private Integer deleted;
}
