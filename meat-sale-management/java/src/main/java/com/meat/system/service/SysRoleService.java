package com.meat.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meat.system.entity.SysRole;

public interface SysRoleService extends IService<SysRole> {
    void assignMenus(Long roleId, Long[] menuIds);
}
