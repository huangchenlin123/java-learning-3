package com.meat.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.meat.common.R;
import com.meat.system.service.SysMenuService;
import com.meat.system.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final SysMenuService sysMenuService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public R<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isEmpty()) {
            return R.fail("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            return R.fail("密码不能为空");
        }

        Map<String, Object> result = sysUserService.login(username, password);
        return R.ok(result);
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public R<?> getUserInfo() {
        Map<String, Object> result = sysUserService.getUserInfo();
        // 附加用户菜单
        Long userId = StpUtil.getLoginIdAsLong();
        result.put("menus", sysMenuService.listByUserId(userId));
        return R.ok(result);
    }

    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public R<Void> logout() {
        StpUtil.logout();
        return R.ok();
    }

    @ApiOperation("获取验证码")
    @GetMapping("/captcha")
    public R<?> getCaptcha() {
        return R.fail("验证码功能待实现");
    }
}
