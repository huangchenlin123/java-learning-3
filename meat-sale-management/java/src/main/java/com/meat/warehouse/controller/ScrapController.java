package com.meat.warehouse.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meat.common.R;
import com.meat.warehouse.entity.ScrapOrder;
import com.meat.warehouse.mapper.ScrapOrderMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "报损管理")
@RestController
@RequestMapping("/api/warehouse/scrap")
@RequiredArgsConstructor
public class ScrapController {
    private final ScrapOrderMapper scrapMapper;

    @GetMapping @ApiOperation("报损单分页")
    public R<Page<ScrapOrder>> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(scrapMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ScrapOrder>().orderByDesc(ScrapOrder::getCreateTime)));
    }

    @PostMapping @ApiOperation("创建报损申请")
    public R<Void> save(@RequestBody ScrapOrder order) { order.setStatus("PENDING"); scrapMapper.insert(order); return R.ok(); }

    @PutMapping("/{id}/approve") @ApiOperation("审批报损")
    public R<Void> approve(@PathVariable Long id) {
        ScrapOrder order = scrapMapper.selectById(id);
        if (order != null) { order.setStatus("APPROVED"); scrapMapper.updateById(order); }
        return R.ok();
    }
}
