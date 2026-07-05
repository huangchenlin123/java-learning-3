package com.meat.purchase.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meat.common.R;
import com.meat.purchase.entity.PurchaseReceipt;
import com.meat.purchase.service.PurchaseReceiptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "采购验收管理")
@RestController
@RequestMapping("/api/purchase/receipt")
@RequiredArgsConstructor
public class PurchaseReceiptController {
    private final PurchaseReceiptService receiptService;

    @GetMapping @ApiOperation("分页查询")
    public R<Page<PurchaseReceipt>> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<PurchaseReceipt> page = new Page<>(pageNum, pageSize);
        return R.ok(receiptService.page(page, new LambdaQueryWrapper<PurchaseReceipt>().orderByDesc(PurchaseReceipt::getCreateTime)));
    }

    @GetMapping("/{id}") @ApiOperation("详情")
    public R<PurchaseReceipt> getById(@PathVariable Long id) { return R.ok(receiptService.getById(id)); }

    @PostMapping @ApiOperation("创建验收单")
    public R<Void> save(@RequestBody PurchaseReceipt receipt) { receiptService.save(receipt); return R.ok(); }

    @PutMapping("/{id}/confirm") @ApiOperation("确认验收(生成入库单)")
    public R<Void> confirm(@PathVariable Long id) {
        PurchaseReceipt receipt = new PurchaseReceipt(); receipt.setId(id);
        receiptService.confirmReceipt(receipt); return R.ok();
    }
}
