package com.meat.purchase.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meat.common.R;
import com.meat.purchase.entity.PurchaseOrder;
import com.meat.purchase.service.PurchaseOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Api(tags = "采购订单管理")
@RestController
@RequestMapping("/api/purchase/order")
@RequiredArgsConstructor
public class PurchaseOrderController {
    private final PurchaseOrderService orderService;

    @GetMapping @ApiOperation("分页查询")
    public R<Page<PurchaseOrder>> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(required = false) String status) {
        Page<PurchaseOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PurchaseOrder> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) w.eq(PurchaseOrder::getStatus, status);
        w.orderByDesc(PurchaseOrder::getCreateTime);
        return R.ok(orderService.page(page, w));
    }

    @GetMapping("/{id}") @ApiOperation("详情")
    public R<PurchaseOrder> getById(@PathVariable Long id) { return R.ok(orderService.getById(id)); }

    @PostMapping @ApiOperation("创建采购单")
    public R<Void> save(@RequestBody PurchaseOrder order) { orderService.save(order); return R.ok(); }

    @PutMapping("/{id}/approve") @ApiOperation("审批")
    public R<Void> approve(@PathVariable Long id, @RequestBody Map<String, Boolean> params) {
        orderService.approve(id, params.getOrDefault("approved", true)); return R.ok();
    }
}
