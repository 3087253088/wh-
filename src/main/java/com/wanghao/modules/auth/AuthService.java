package com.wanghao.modules.auth;

import com.wanghao.common.exception.BusinessException;
import com.wanghao.common.utils.JwtUtils;
import com.wanghao.common.utils.PasswordUtils;
import com.wanghao.modules.auth.LoginDto;
import com.wanghao.modules.auth.UserInfoVo;
import com.wanghao.modules.user.entity.SysUser;
import com.wanghao.modules.user.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    public Map<String, Object> login(LoginDto loginDto) {
        SysUser user = userMapper.selectByUsername(loginDto.getUsername());

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        if (!PasswordUtils.matches(loginDto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 生成Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRoleCode());

        // 更新最后登录时间
        user.setLastLoginTime(new java.util.Date());
        userMapper.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", getUserInfoVo(user));

        return result;
    }

    public UserInfoVo getCurrentUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return getUserInfoVo(user);
    }

    private UserInfoVo getUserInfoVo(SysUser user) {
        UserInfoVo vo = new UserInfoVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setName(user.getName());
        vo.setDeptId(user.getDeptId());
        vo.setRoleCode(user.getRoleCode());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());

        // 根据角色设置权限
        String[] permissions;
        switch (user.getRoleCode()) {
            case "admin":
                permissions = new String[]{"myApproval", "personnelManage"};
                break;
            case "finance":
                permissions = new String[]{"workbench", "myExpense", "myInvoice", "myApproval",
                        "budgetManage", "invoiceManage", "employeeInfo", "myInfo"};
                break;
            default:
                permissions = new String[]{"workbench", "myExpense", "myInvoice", "myInfo"};
                break;
        }
        vo.setPermissions(permissions);

        return vo;
    }
}
