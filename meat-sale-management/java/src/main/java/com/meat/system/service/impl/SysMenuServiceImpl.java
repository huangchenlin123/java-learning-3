package com.meat.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.common.exception.BusinessException;
import com.meat.system.entity.SysMenu;
import com.meat.system.mapper.SysMenuMapper;
import com.meat.system.service.SysMenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Resource
    private SysMenuMapper sysMenuMapper;

    @Override
    public List<SysMenu> listTree() {
        List<SysMenu> allMenus = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort));
        return buildTree(allMenus);
    }

    @Override
    public List<SysMenu> listByUserId(Long userId) {
        List<SysMenu> allMenus = sysMenuMapper.selectMenusByUserId(userId);
        return buildTree(allMenus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(java.io.Serializable id) {
        Long count = sysMenuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (count != null && count > 0) {
            throw new BusinessException("该菜单下存在子菜单，无法删除");
        }
        return super.removeById(id);
    }

    private List<SysMenu> buildTree(List<SysMenu> allMenus) {
        if (allMenus == null || allMenus.isEmpty()) {
            return new ArrayList<>();
        }
        List<SysMenu> topLevel = allMenus.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0L)
                .collect(Collectors.toList());
        for (SysMenu menu : topLevel) {
            setChildren(menu, allMenus);
        }
        return topLevel;
    }

    private void setChildren(SysMenu parent, List<SysMenu> allMenus) {
        List<SysMenu> children = allMenus.stream()
                .filter(m -> parent.getId().equals(m.getParentId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        for (SysMenu child : children) {
            setChildren(child, allMenus);
        }
    }
}
