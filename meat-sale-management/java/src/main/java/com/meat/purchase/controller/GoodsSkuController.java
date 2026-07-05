package com.meat.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meat.common.R;
import com.meat.purchase.entity.GoodsCategory;
import com.meat.purchase.entity.GoodsSku;
import com.meat.purchase.service.GoodsCategoryService;
import com.meat.purchase.service.GoodsSkuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "商品SKU管理")
@RestController
@RequestMapping("/api/purchase/sku")
@RequiredArgsConstructor
public class GoodsSkuController {

    private final GoodsSkuService skuService;
    private final GoodsCategoryService categoryService;

    @GetMapping
    @ApiOperation("分页查询SKU")
    public R<Page<GoodsSku>> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String skuName) {
        Page<GoodsSku> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GoodsSku> w = new LambdaQueryWrapper<>();
        if (skuName != null && !skuName.isEmpty()) w.like(GoodsSku::getSkuName, skuName);
        w.orderByDesc(GoodsSku::getCreateTime);
        Page<GoodsSku> result = skuService.page(page, w);
        // 填充分类名称
        Map<Long, String> catMap = categoryService.list().stream()
                .collect(Collectors.toMap(GoodsCategory::getId, c -> c.getCategoryName() != null ? c.getCategoryName() : ""));
        result.getRecords().forEach(s -> s.setCategoryName(catMap.getOrDefault(s.getCategoryId(), "")));
        return R.ok(result);
    }

    @GetMapping("/all")
    @ApiOperation("全部SKU")
    public R<java.util.List<GoodsSku>> all() { return R.ok(skuService.list()); }

    @PostMapping
    @ApiOperation("新增SKU")
    public R<Void> save(@RequestBody GoodsSku sku) { skuService.save(sku); return R.ok(); }

    @PutMapping("/{id}")
    @ApiOperation("修改SKU")
    public R<Void> update(@PathVariable Long id, @RequestBody GoodsSku sku) { sku.setId(id); skuService.updateById(sku); return R.ok(); }

    @DeleteMapping("/{id}")
    @ApiOperation("删除SKU")
    public R<Void> delete(@PathVariable Long id) { skuService.removeById(id); return R.ok(); }
}
