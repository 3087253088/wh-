package com.wanghao.modules.profile;

import cn.hutool.core.io.FileUtil;
import com.wanghao.common.exception.BusinessException;
import com.wanghao.common.utils.PasswordUtils;
import com.wanghao.modules.dept.entity.SysDept;
import com.wanghao.modules.dept.mapper.SysDeptMapper;
import com.wanghao.modules.user.entity.SysUser;
import com.wanghao.modules.user.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ProfileService {

    @Value("${file.avatar-path}")
    private String avatarPath;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysDeptMapper deptMapper;

    public Map<String, Object> getProfile(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        SysDept dept = deptMapper.selectById(user.getDeptId());
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("name", user.getName());
        map.put("deptId", user.getDeptId());
        map.put("deptName", dept != null ? dept.getDeptName() : "");
        map.put("position", user.getPosition());
        map.put("phone", user.getPhone());
        map.put("email", user.getEmail());
        map.put("avatar", user.getAvatar());
        map.put("entryDate", user.getEntryDate());
        return map;
    }

    public void updateProfile(Long userId, ProfileUpdateDto updateDto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setName(updateDto.getName());
        user.setPhone(updateDto.getPhone());
        user.setEmail(updateDto.getEmail());

        userMapper.updateById(user);
    }

    public void changePassword(Long userId, PasswordChangeDto changeDto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!PasswordUtils.matches(changeDto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(PasswordUtils.encode(changeDto.getNewPassword()));
        userMapper.updateById(user);
    }

    public Map<String, String> uploadAvatar(Long userId, MultipartFile file) {
        // 创建目录
        File dir = new File(avatarPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + ext;

        // 保存文件
        try {
            File dest = new File(avatarPath + filename);
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        }

        String avatarUrl = "/avatar/" + filename;

        SysUser user = userMapper.selectById(userId);
        if (user != null) {
            // 删除旧头像
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                File oldFile = new File(avatarPath + user.getAvatar().replace("/avatar/", ""));
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }

            user.setAvatar(avatarUrl);
            userMapper.updateById(user);
        }

        Map<String, String> result = new HashMap<>();
        result.put("avatarUrl", avatarUrl);
        return result;
    }
}
