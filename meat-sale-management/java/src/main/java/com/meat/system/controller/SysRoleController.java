package com.meat.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meat.common.R;
import com.meat.system.entity.SysRole;
import com.meat.system.mapper.SysMenuMapper;
import com.meat.system.mapper.SysRoleMapper;
import com.meat.system.mapper.SysMenuMapper;
import com.meat.system.service.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "角色管理")
@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;

    @ApiOperation("分页查询角色列表（或全部）")
    @GetMapping
    public R<?> list(@RequestParam(required = false) String roleName) {
        LambdaQueryWrapper<SysRole> w = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isEmpty()) w.like(SysRole::getRoleName, roleName);
        w.orderByDesc(SysRole::getCreateTime);
        return R.ok(sysRoleService.list(w));
    }

    @ApiOperation("根据ID查询角色详情")
    @GetMapping("/{id}")
    public R<SysRole> getById(@PathVariable Long id) {
        SysRole role = sysRoleService.getById(id);
        if (role == null) return R.fail("角色不存在");
        return R.ok(role);
    }

    @ApiOperation("新增角色")
    @PostMapping
    public R<Void> add(@RequestBody SysRole sysRole) {
        sysRoleService.save(sysRole);
        return R.ok();
    }

    @ApiOperation("修改角色")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody SysRole sysRole) {
        sysRole.setId(id);
        sysRoleService.updateById(sysRole);
        return R.ok();
    }

    @ApiOperation("删除角色")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysRoleService.removeById(id);
        return R.ok();
    }

    @ApiOperation("分配角色菜单权限")
    @PutMapping("/{roleId}/menus")
    public R<Void> assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        sysRoleService.assignMenus(roleId, menuIds.toArray(new Long[0]));
        return R.ok();
    }

    @ApiOperation("获取角色拥有的菜单ID列表")
    @GetMapping("/{roleId}/menus")
    public R<List<Long>> getRoleMenuIds(@PathVariable Long roleId) {
        return R.ok(sysMenuMapper.selectMenuIdsByRoleId(roleId));
    }
}
