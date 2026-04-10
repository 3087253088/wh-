package com.wanghao.modules.employee;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanghao.modules.employee.dto.EmployeeSaveDto;
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
public class EmployeeService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysDeptMapper deptMapper;

    public Page<Map<String, Object>> getList(Integer pageNum, Integer pageSize, String name, Long deptId, String status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(SysUser::getRoleCode, "admin");
        wrapper.like(name != null, SysUser::getName, name);
        wrapper.eq(deptId != null, SysUser::getDeptId, deptId);
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
            map.put("employeeNo", "EMP" + String.format("%08d", user.getId()));
            map.put("name", user.getName());
            map.put("deptId", user.getDeptId());
            map.put("deptName", dept != null ? dept.getDeptName() : "");
            map.put("position", user.getPosition());
            map.put("phone", user.getPhone());
            map.put("email", user.getEmail());
            map.put("entryDate", user.getEntryDate());
            map.put("status", user.getStatus() == 1 ? "active" : "inactive");
            return map;
        }).collect(Collectors.toList()));

        return resultPage;
    }

    public Map<String, Object> getDetail(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("员工不存在");
        }

        SysDept dept = deptMapper.selectById(user.getDeptId());
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("employeeNo", "EMP" + String.format("%08d", user.getId()));
        map.put("name", user.getName());
        map.put("deptId", user.getDeptId());
        map.put("deptName", dept != null ? dept.getDeptName() : "");
        map.put("position", user.getPosition());
        map.put("phone", user.getPhone());
        map.put("email", user.getEmail());
        map.put("entryDate", user.getEntryDate());
        map.put("status", user.getStatus() == 1 ? "active" : "inactive");
        return map;
    }

    public Map<String, String> save(EmployeeSaveDto saveDto) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, saveDto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        String defaultPassword = "123456";

        SysUser user = new SysUser();
        user.setUsername(saveDto.getUsername());
        user.setPassword(PasswordUtils.encode(defaultPassword));
        user.setName(saveDto.getName());
        user.setDeptId(saveDto.getDeptId());
        user.setPosition(saveDto.getPosition());
        user.setRoleCode(saveDto.getRole());
        user.setPhone(saveDto.getPhone());
        user.setEmail(saveDto.getEmail());
        user.setEntryDate(saveDto.getEntryDate());
        user.setStatus(1);

        userMapper.insert(user);

        Map<String, String> result = new HashMap<>();
        result.put("defaultPassword", defaultPassword);
        return result;
    }

    public void update(EmployeeSaveDto saveDto) {
        SysUser user = userMapper.selectById(saveDto.getId());
        if (user == null) {
            throw new BusinessException("员工不存在");
        }

        user.setName(saveDto.getName());
        user.setDeptId(saveDto.getDeptId());
        user.setPosition(saveDto.getPosition());
        user.setPhone(saveDto.getPhone());
        user.setEmail(saveDto.getEmail());
        user.setEntryDate(saveDto.getEntryDate());

        userMapper.updateById(user);
    }

    public void updateStatus(Long id, String status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("员工不存在");
        }

        user.setStatus("active".equals(status) ? 1 : 0);
        userMapper.updateById(user);
    }
}
