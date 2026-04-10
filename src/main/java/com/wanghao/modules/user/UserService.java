package com.wanghao.modules.user;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanghao.common.exception.BusinessException;
import com.wanghao.common.utils.PasswordUtils;
import com.wanghao.modules.dept.entity.SysDept;
import com.wanghao.modules.dept.mapper.SysDeptMapper;
import com.wanghao.modules.user.entity.SysUser;
import com.wanghao.modules.user.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysDeptMapper deptMapper;

    public Page<Map<String, Object>> getList(Integer pageNum, Integer pageSize, String username,
                                             String name, String roleCode, String status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(username != null, SysUser::getUsername, username);
        wrapper.like(name != null, SysUser::getName, name);
        wrapper.eq(roleCode != null, SysUser::getRoleCode, roleCode);
        wrapper.eq(status != null, SysUser::getStatus, "active".equals(status) ? 1 : 0);
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> userPage = userMapper.selectPage(page, wrapper);
        Page<Map<String, Object>> resultPage = new Page<>();
        resultPage.setCurrent(userPage.getCurrent());
        resultPage.setSize(userPage.getSize());
        resultPage.setTotal(userPage.getTotal());

        resultPage.setRecords(userPage.getRecords().stream().map(user -> {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("name", user.getName());
            map.put("deptId", user.getDeptId());
            map.put("deptName", dept != null ? dept.getDeptName() : "");
            map.put("roleCode", user.getRoleCode());
            map.put("roleName", getRoleName(user.getRoleCode()));
            map.put("phone", user.getPhone());
            map.put("email", user.getEmail());
            map.put("status", user.getStatus() == 1 ? "active" : "disabled");
            map.put("createTime", user.getCreateTime());
            map.put("lastLoginTime", user.getLastLoginTime());
            return map;
        }).collect(Collectors.toList()));

        return resultPage;
    }

    public Map<String, Long> save(UserSaveDto saveDto) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, saveDto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(saveDto.getUsername());
        user.setPassword(PasswordUtils.encode(saveDto.getPassword()));
        user.setName(saveDto.getName());
        user.setDeptId(saveDto.getDeptId());
        user.setRoleCode(saveDto.getRoleCode());
        user.setPhone(saveDto.getPhone());
        user.setEmail(saveDto.getEmail());
        user.setStatus(1);

        userMapper.insert(user);

        Map<String, Long> result = new HashMap<>();
        result.put("id", user.getId());
        return result;
    }

    public void update(UserSaveDto saveDto) {
        SysUser user = userMapper.selectById(saveDto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setName(saveDto.getName());
        user.setDeptId(saveDto.getDeptId());
        user.setRoleCode(saveDto.getRoleCode());
        user.setPhone(saveDto.getPhone());
        user.setEmail(saveDto.getEmail());

        userMapper.updateById(user);
    }

    public void updateStatus(Long id, String status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setStatus("active".equals(status) ? 1 : 0);
        userMapper.updateById(user);
    }

    public Map<String, String> resetPassword(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String defaultPassword = "123456";
        user.setPassword(PasswordUtils.encode(defaultPassword));
        userMapper.updateById(user);

        Map<String, String> result = new HashMap<>();
        result.put("password", defaultPassword);
        return result;
    }

    public void assignRole(Long id, String roleCode) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setRoleCode(roleCode);
        userMapper.updateById(user);
    }

    private String getRoleName(String roleCode) {
        Map<String, String> map = new HashMap<>();
        map.put("admin", "管理员");
        map.put("finance", "财务人员");
        map.put("employee", "普通员工");
        return map.getOrDefault(roleCode, roleCode);
    }
}
