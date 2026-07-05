package com.meat.warehouse.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meat.common.R;
import com.meat.warehouse.entity.InventoryCheck;
import com.meat.warehouse.mapper.InventoryCheckMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "库存盘点")
@RestController
@RequestMapping("/api/warehouse/check")
@RequiredArgsConstructor
public class InventoryCheckController {
    private final InventoryCheckMapper checkMapper;

    @GetMapping @ApiOperation("盘点单分页")
    public R<Page<InventoryCheck>> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(checkMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<InventoryCheck>().orderByDesc(InventoryCheck::getCreateTime)));
    }

    @PostMapping @ApiOperation("创建盘点单")
    public R<Void> save(@RequestBody InventoryCheck check) { check.setStatus("CHECKING"); checkMapper.insert(check); return R.ok(); }

    @PutMapping("/{id}/submit") @ApiOperation("提交盘点")
    public R<Void> submit(@PathVariable Long id) {
        InventoryCheck check = checkMapper.selectById(id);
        if (check != null) { check.setStatus("SUBMITTED"); checkMapper.updateById(check); }
        return R.ok();
    }

    @PutMapping("/{id}/approve") @ApiOperation("审核盘点")
    public R<Void> approve(@PathVariable Long id) {
        InventoryCheck check = checkMapper.selectById(id);
        if (check != null) { check.setStatus("APPROVED"); checkMapper.updateById(check); }
        return R.ok();
    }
}
