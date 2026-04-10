package com.wanghao.modules.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class EmployeeSaveDto {
    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotNull(message = "部门不能为空")
    private Long deptId;

    @NotBlank(message = "职位不能为空")
    private String position;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "入职日期不能为空")
    private Date entryDate;

    @NotBlank(message = "角色不能为空")
    private String role;
}
