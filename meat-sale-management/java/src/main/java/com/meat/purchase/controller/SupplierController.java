package com.meat.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meat.common.R;
import com.meat.purchase.entity.Supplier;
import com.meat.purchase.service.SupplierService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "供应商管理")
@RestController
@RequestMapping("/api/purchase/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @ApiOperation("分页查询供应商")
    public R<Page<Supplier>> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String supplierName) {
        Page<Supplier> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Supplier> w = new LambdaQueryWrapper<>();
        if (supplierName != null && !supplierName.isEmpty()) w.like(Supplier::getSupplierName, supplierName);
        w.orderByDesc(Supplier::getCreateTime);
        return R.ok(supplierService.page(page, w));
    }

    @GetMapping("/all")
    @ApiOperation("全部供应商")
    public R<java.util.List<Supplier>> all() {
        return R.ok(supplierService.list());
    }

    @PostMapping
    @ApiOperation("新增供应商")
    public R<Void> save(@RequestBody Supplier supplier) { supplierService.save(supplier); return R.ok(); }

    @PutMapping("/{id}")
    @ApiOperation("修改供应商")
    public R<Void> update(@PathVariable Long id, @RequestBody Supplier supplier) { supplier.setId(id); supplierService.updateById(supplier); return R.ok(); }

    @DeleteMapping("/{id}")
    @ApiOperation("删除供应商")
    public R<Void> delete(@PathVariable Long id) { supplierService.removeById(id); return R.ok(); }
}
