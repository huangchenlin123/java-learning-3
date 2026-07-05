package com.meat.sale.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meat.common.R;
import com.meat.sale.entity.SaleOrder;
import com.meat.sale.service.SaleOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Api(tags = "销售管理")
@RestController
@RequestMapping("/api/sale/order")
@RequiredArgsConstructor
public class SaleOrderController {
    private final SaleOrderService orderService;
    @GetMapping @ApiOperation("分页查询")
    public R<Page<SaleOrder>> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(required = false) String status) {
        Page<SaleOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SaleOrder> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) w.eq(SaleOrder::getStatus, status);
        w.orderByDesc(SaleOrder::getCreateTime);
        return R.ok(orderService.page(page, w));
    }
    @GetMapping("/{id}") @ApiOperation("详情")
    public R<SaleOrder> getById(@PathVariable Long id) { return R.ok(orderService.getById(id)); }
    @PostMapping("/pos") @ApiOperation("POS收银")
    public R<SaleOrder> posSale(@RequestBody SaleOrder order) { orderService.posSale(order); return R.ok(order); }
    @PutMapping("/{id}/approve") @ApiOperation("审批")
    public R<Void> approve(@PathVariable Long id, @RequestBody Map<String, Boolean> params) {
        orderService.approveOrder(id, params.getOrDefault("approved", true)); return R.ok();
    }
}
