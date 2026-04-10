package com.wanghao.modules.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserSaveDto {
    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotNull(message = "部门不能为空")
    private Long deptId;

    @NotBlank(message = "角色不能为空")
    private String roleCode;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}
