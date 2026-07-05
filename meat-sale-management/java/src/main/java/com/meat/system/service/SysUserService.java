package com.meat.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meat.system.entity.SysUser;
import java.util.Map;

public interface SysUserService extends IService<SysUser> {
    Map<String, Object> login(String username, String password);
    Map<String, Object> getUserInfo();
    void updatePassword(Long userId, String oldPassword, String newPassword);
}
