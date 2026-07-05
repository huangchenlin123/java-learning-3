package com.meat.warehouse.controller;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meat.common.R;
import com.meat.warehouse.entity.StockIn;
import com.meat.warehouse.service.StockInService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "入库管理")
@RestController
@RequestMapping("/api/warehouse/stockin")
@RequiredArgsConstructor
public class StockInController {
    private final StockInService stockInService;

    @GetMapping @ApiOperation("分页查询")
    public R<Page<StockIn>> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<StockIn> page = new Page<>(pageNum, pageSize);
        return R.ok(stockInService.page(page, new LambdaQueryWrapper<StockIn>().orderByDesc(StockIn::getCreateTime)));
    }

    @GetMapping("/{id}") @ApiOperation("详情")
    public R<StockIn> getById(@PathVariable Long id) { return R.ok(stockInService.getById(id)); }

    @PutMapping("/{id}/confirm") @ApiOperation("确认入库")
    public R<Void> confirm(@PathVariable Long id) {
        stockInService.confirm(id, StpUtil.getLoginIdAsLong()); return R.ok();
    }
}
