package com.meat.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.common.exception.BusinessException;
import com.meat.system.entity.SysRole;
import com.meat.system.entity.SysUser;
import com.meat.system.mapper.SysRoleMapper;
import com.meat.system.mapper.SysUserMapper;
import com.meat.system.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Override
    public Map<String, Object> login(String username, String password) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        StpUtil.login(user.getId());

        // 查询用户角色
        List<String> roles = getRolesByUserId(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", StpUtil.getTokenValue());
        user.setPassword(null);
        result.put("user", user);
        result.put("roles", roles);
        return result;
    }

    @Override
    public Map<String, Object> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);

        List<String> roles = getRolesByUserId(userId);
        List<String> perms = sysUserMapper.selectPermsByUserId(userId);
        if (perms == null) {
            perms = new ArrayList<>();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("roles", roles);
        result.put("perms", perms);
        return result;
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(BCrypt.hashpw(newPassword));
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysUser entity) {
        if (entity.getPassword() != null && !entity.getPassword().isEmpty()) {
            entity.setPassword(BCrypt.hashpw(entity.getPassword()));
        }
        return super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(java.util.Collection<?> list) {
        if (list != null && !list.isEmpty()) {
            // 删除 sys_user_role 中的关联记录
            for (Object id : list) {
                sysUserMapper.delete(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getId, id));
            }
        }
        return super.removeByIds(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(java.io.Serializable id) {
        // 删除 sys_user_role 中的关联记录
        sysUserMapper.deleteUserRoles((Long) id);
        return super.removeById(id);
    }

    private List<String> getRolesByUserId(Long userId) {
        List<SysRole> roleList = sysRoleMapper.selectRolesByUserId(userId);
        if (roleList == null || roleList.isEmpty()) {
            return new ArrayList<>();
        }
        return roleList.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
    }
}
