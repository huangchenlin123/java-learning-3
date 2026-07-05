package com.meat.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meat.common.R;
import com.meat.purchase.mapper.GoodsSkuMapper;
import com.meat.warehouse.entity.Batch;
import com.meat.warehouse.entity.Stock;
import com.meat.warehouse.mapper.BatchMapper;
import com.meat.warehouse.mapper.StockMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "库存查询")
@RestController
@RequestMapping("/api/warehouse/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockMapper stockMapper;
    private final BatchMapper batchMapper;
    private final GoodsSkuMapper goodsSkuMapper;

    @GetMapping
    @ApiOperation("分页库存查询（含商品名/批次号）")
    public R<Page<Map<String, Object>>> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Stock> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Stock> w = new LambdaQueryWrapper<Stock>()
                .gt(Stock::getQuantity, 0).orderByDesc(Stock::getUpdateTime);
        Page<Stock> result = stockMapper.selectPage(page, w);

        // 姓名映射
        Map<Long, String> skuNameMap = goodsSkuMapper.selectList(null).stream()
                .collect(Collectors.toMap(s -> s.getId(), s -> s.getSkuName() != null ? s.getSkuName() : ""));
        Map<Long, String> batchNoMap = batchMapper.selectList(null).stream()
                .collect(Collectors.toMap(Batch::getId, b -> b.getBatchNo() != null ? b.getBatchNo() : ""));

        // 包装为 Map 附加名称字段
        Page<Map<String, Object>> enriched = new Page<>(pageNum, pageSize, result.getTotal());
        enriched.setRecords(result.getRecords().stream().map(s -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", s.getId());
            m.put("skuId", s.getSkuId());
            m.put("skuName", skuNameMap.getOrDefault(s.getSkuId(), "未知"));
            m.put("batchId", s.getBatchId());
            m.put("batchNo", batchNoMap.getOrDefault(s.getBatchId(), ""));
            m.put("warehouseId", s.getWarehouseId());
            m.put("warehouseName", s.getWarehouseId() == null ? "" : "仓库" + s.getWarehouseId());
            m.put("quantity", s.getQuantity());
            m.put("lockedQuantity", s.getLockedQuantity());
            return m;
        }).collect(Collectors.toList()));
        return R.ok(enriched);
    }
}
