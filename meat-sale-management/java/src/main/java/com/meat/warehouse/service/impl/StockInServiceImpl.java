package com.meat.warehouse.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.common.exception.BusinessException;
import com.meat.warehouse.entity.*;
import com.meat.warehouse.mapper.*;
import com.meat.warehouse.service.StockInService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class StockInServiceImpl extends ServiceImpl<StockInMapper, StockIn> implements StockInService {

    @Resource private StockInItemMapper stockInItemMapper;
    @Resource private StockMapper stockMapper;

    @Override
    @Transactional
    public void confirm(Long stockInId, Long userId) {
        StockIn stockIn = getById(stockInId);
        if (stockIn == null) throw new BusinessException("入库单不存在");
        if (!"PENDING".equals(stockIn.getStatus())) throw new BusinessException("入库单状态不正确");

        for (StockInItem item : stockInItemMapper.selectList(
                new LambdaQueryWrapper<StockInItem>().eq(StockInItem::getInId, stockInId))) {
            Stock stock = stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                    .eq(Stock::getSkuId, item.getSkuId())
                    .eq(Stock::getBatchId, item.getBatchId())
                    .eq(Stock::getWarehouseId, stockIn.getWarehouseId()));
            if (stock != null) {
                stock.setQuantity(stock.getQuantity().add(item.getQuantity()));
                stockMapper.updateById(stock);
            } else {
                stock = new Stock();
                stock.setSkuId(item.getSkuId());
                stock.setBatchId(item.getBatchId());
                stock.setWarehouseId(stockIn.getWarehouseId());
                stock.setQuantity(item.getQuantity());
                stock.setLockedQuantity(BigDecimal.ZERO);
                stockMapper.insert(stock);
            }
        }

        stockIn.setStatus("CONFIRMED");
        stockIn.setConfirmBy(userId);
        stockIn.setConfirmTime(LocalDateTime.now());
        updateById(stockIn);
    }
}
