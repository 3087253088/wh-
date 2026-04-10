package com.wanghao.modules.auth;

import lombok.Data;

@Data
public class UserInfoVo {
    private Long id;
    private String username;
    private String name;
    private Long deptId;
    private String deptName;
    private String roleCode;
    private String roleName;
    private String phone;
    private String email;
    private String avatar;
    private String[] permissions;
}
