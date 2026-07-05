package com.meat.warehouse.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meat.common.R;
import com.meat.warehouse.entity.TransferOrder;
import com.meat.warehouse.mapper.TransferOrderMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "调拨管理")
@RestController
@RequestMapping("/api/warehouse/transfer")
@RequiredArgsConstructor
public class TransferController {
    private final TransferOrderMapper transferMapper;

    @GetMapping @ApiOperation("调拨单分页")
    public R<Page<TransferOrder>> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(transferMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<TransferOrder>().orderByDesc(TransferOrder::getCreateTime)));
    }

    @PostMapping @ApiOperation("创建调拨申请")
    public R<Void> save(@RequestBody TransferOrder order) { order.setStatus("PENDING"); transferMapper.insert(order); return R.ok(); }

    @PutMapping("/{id}/approve") @ApiOperation("审批")
    public R<Void> approve(@PathVariable Long id) {
        TransferOrder order = transferMapper.selectById(id);
        if (order != null) { order.setStatus("APPROVED"); transferMapper.updateById(order); }
        return R.ok();
    }

    @PutMapping("/{id}/complete") @ApiOperation("完成调拨")
    public R<Void> complete(@PathVariable Long id) {
        TransferOrder order = transferMapper.selectById(id);
        if (order != null) { order.setStatus("COMPLETED"); transferMapper.updateById(order); }
        return R.ok();
    }
}
