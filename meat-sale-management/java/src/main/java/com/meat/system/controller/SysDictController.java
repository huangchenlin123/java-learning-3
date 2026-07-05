package com.meat.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meat.common.R;
import com.meat.system.entity.SysDict;
import com.meat.system.entity.SysDictItem;
import com.meat.system.service.SysDictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "字典管理")
@RestController
@RequestMapping("/api/system/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService sysDictService;

    @ApiOperation("查询字典列表（全部或按类型过滤）")
    @GetMapping
    public R<List<SysDict>> list(@RequestParam(required = false) String dictType) {
        LambdaQueryWrapper<SysDict> w = new LambdaQueryWrapper<>();
        if (dictType != null && !dictType.isEmpty()) w.eq(SysDict::getDictCode, dictType);
        w.orderByAsc(SysDict::getCreateTime);
        return R.ok(sysDictService.list(w));
    }

    @ApiOperation("根据字典编码查询字典项")
    @GetMapping("/items/{dictCode}")
    public R<List<SysDictItem>> getItems(@PathVariable String dictCode) {
        return R.ok(sysDictService.listItemsByCode(dictCode));
    }

    @ApiOperation("根据ID查询字典详情")
    @GetMapping("/{id}")
    public R<SysDict> getById(@PathVariable Long id) {
        SysDict dict = sysDictService.getById(id);
        if (dict == null) return R.fail("字典不存在");
        return R.ok(dict);
    }

    @ApiOperation("新增字典")
    @PostMapping
    public R<Void> add(@RequestBody SysDict sysDict) {
        sysDictService.save(sysDict);
        return R.ok();
    }

    @ApiOperation("修改字典")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody SysDict sysDict) {
        sysDict.setId(id);
        sysDictService.updateById(sysDict);
        return R.ok();
    }

    @ApiOperation("删除字典")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysDictService.removeById(id);
        return R.ok();
    }
}
