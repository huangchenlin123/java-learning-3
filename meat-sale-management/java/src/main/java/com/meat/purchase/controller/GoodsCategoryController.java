package com.meat.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meat.common.R;
import com.meat.purchase.entity.GoodsCategory;
import com.meat.purchase.service.GoodsCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品分类管理")
@RestController
@RequestMapping("/api/purchase/category")
@RequiredArgsConstructor
public class GoodsCategoryController {

    private final GoodsCategoryService categoryService;

    @GetMapping
    @ApiOperation("分类列表")
    public R<List<GoodsCategory>> list() {
        return R.ok(categoryService.list(
                new LambdaQueryWrapper<GoodsCategory>().orderByAsc(GoodsCategory::getSort)));
    }

    @PostMapping
    @ApiOperation("新增分类")
    public R<Void> save(@RequestBody GoodsCategory category) { categoryService.save(category); return R.ok(); }

    @PutMapping("/{id}")
    @ApiOperation("修改分类")
    public R<Void> update(@PathVariable Long id, @RequestBody GoodsCategory category) { category.setId(id); categoryService.updateById(category); return R.ok(); }

    @DeleteMapping("/{id}")
    @ApiOperation("删除分类")
    public R<Void> delete(@PathVariable Long id) { categoryService.removeById(id); return R.ok(); }
}
