package com.meat.system.controller;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meat.common.R;
import com.meat.system.entity.SysUser;
import com.meat.system.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @ApiOperation("分页查询用户列表")
    @GetMapping
    public R<Page<SysUser>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) String username,
                                  @RequestParam(required = false) Integer status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) w.like(SysUser::getUsername, username);
        if (status != null) w.eq(SysUser::getStatus, status);
        w.orderByDesc(SysUser::getCreateTime);
        return R.ok(sysUserService.page(page, w));
    }

    @ApiOperation("根据ID查询用户详情")
    @GetMapping("/{id}")
    public R<SysUser> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) return R.fail("用户不存在");
        user.setPassword(null);
        return R.ok(user);
    }

    @ApiOperation("新增用户")
    @PostMapping
    public R<Void> add(@RequestBody SysUser sysUser) {
        if (sysUser.getPassword() == null || sysUser.getPassword().isEmpty()) {
            sysUser.setPassword("123456");
        }
        sysUserService.save(sysUser);
        return R.ok();
    }

    @ApiOperation("修改用户")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody SysUser sysUser) {
        sysUser.setId(id);
        // 不修改密码（密码单独修改）
        sysUser.setPassword(null);
        sysUserService.updateById(sysUser);
        return R.ok();
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return R.ok();
    }

    @ApiOperation("重置用户密码")
    @PutMapping("/{id}/reset-pwd")
    public R<Void> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.isEmpty()) return R.fail("密码不能为空");
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(BCrypt.hashpw(newPassword));
        sysUserService.updateById(user);
        return R.ok();
    }

    @ApiOperation("修改用户状态")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        sysUserService.updateById(user);
        return R.ok();
    }
}
