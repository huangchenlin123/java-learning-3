package com.meat.warehouse.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.meat.common.R;
import com.meat.warehouse.entity.BatchExpiryWarning;
import com.meat.warehouse.service.ExpiryWarningService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 效期预警查询控制器
 */
@Api(tags = "效期预警")
@RestController
@RequestMapping("/api/warehouse/expiry-warning")
@RequiredArgsConstructor
public class ExpiryWarningController {

    private final ExpiryWarningService expiryWarningService;

    @GetMapping
    @ApiOperation("未处理预警列表（可选按级别筛选）")
    public R<List<BatchExpiryWarning>> list(@RequestParam(required = false) String warnLevel) {
        return R.ok(expiryWarningService.listUnhandled(warnLevel));
    }

    @PostMapping("/scan")
    @ApiOperation("手动触发全库扫描")
    public R<Integer> manualScan() {
        return R.ok(expiryWarningService.scanAndWarn());
    }

    @PutMapping("/{id}/handle")
    @ApiOperation("标记预警为已处理")
    public R<Void> handle(@PathVariable Long id) {
        expiryWarningService.markHandled(id, StpUtil.getLoginIdAsLong());
        return R.ok();
    }
}
