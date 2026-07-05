package com.meat.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.system.entity.SysRole;
import com.meat.system.mapper.SysRoleMapper;
import com.meat.system.service.SysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, Long[] menuIds) {
        sysRoleMapper.deleteRoleMenus(roleId);
        if (menuIds != null && menuIds.length > 0) {
            for (Long menuId : menuIds) {
                sysRoleMapper.insertRoleMenu(roleId, menuId);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(java.io.Serializable id) {
        Long roleId = (Long) id;
        sysRoleMapper.deleteRoleMenus(roleId);
        sysRoleMapper.deleteRoleUsers(roleId);
        return super.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(java.util.Collection<?> list) {
        if (list != null && !list.isEmpty()) {
            for (Object id : list) {
                Long roleId = (Long) id;
                sysRoleMapper.deleteRoleMenus(roleId);
                sysRoleMapper.deleteRoleUsers(roleId);
            }
        }
        return super.removeByIds(list);
    }
}
