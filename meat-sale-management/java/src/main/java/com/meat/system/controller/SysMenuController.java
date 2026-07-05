package com.meat.system.controller;

import com.meat.common.R;
import com.meat.system.entity.SysMenu;
import com.meat.system.service.SysMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "菜单管理")
@RestController
@RequestMapping("/api/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    @ApiOperation("查询菜单树")
    @GetMapping("/tree")
    public R<List<SysMenu>> tree() {
        return R.ok(sysMenuService.listTree());
    }

    @ApiOperation("查询所有菜单列表")
    @GetMapping
    public R<List<SysMenu>> list() {
        return R.ok(sysMenuService.list());
    }

    @ApiOperation("根据ID查询菜单详情")
    @GetMapping("/{id}")
    public R<SysMenu> getById(@PathVariable Long id) {
        SysMenu menu = sysMenuService.getById(id);
        if (menu == null) return R.fail("菜单不存在");
        return R.ok(menu);
    }

    @ApiOperation("新增菜单")
    @PostMapping
    public R<Void> add(@RequestBody SysMenu sysMenu) {
        sysMenuService.save(sysMenu);
        return R.ok();
    }

    @ApiOperation("修改菜单")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody SysMenu sysMenu) {
        sysMenu.setId(id);
        sysMenuService.updateById(sysMenu);
        return R.ok();
    }

    @ApiOperation("删除菜单")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysMenuService.removeById(id);
        return R.ok();
    }
}
