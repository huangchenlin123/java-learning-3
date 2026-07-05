package com.meat.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.purchase.entity.GoodsSku;
import com.meat.purchase.mapper.GoodsSkuMapper;
import com.meat.warehouse.entity.Batch;
import com.meat.warehouse.entity.BatchExpiryWarning;
import com.meat.warehouse.entity.Stock;
import com.meat.warehouse.mapper.BatchExpiryWarningMapper;
import com.meat.warehouse.mapper.BatchMapper;
import com.meat.warehouse.mapper.StockMapper;
import com.meat.warehouse.service.ExpiryWarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 效期预警服务实现
 */
@Slf4j
@Service
public class ExpiryWarningServiceImpl extends ServiceImpl<BatchExpiryWarningMapper, BatchExpiryWarning>
        implements ExpiryWarningService {

    @Resource private StockMapper stockMapper;
    @Resource private BatchMapper batchMapper;
    @Resource private GoodsSkuMapper goodsSkuMapper;

    /** CRITICAL 阈值天数 */
    private static final int CRITICAL_DAYS = 3;
    /** WARNING 阈值天数 */
    private static final int WARNING_DAYS = 7;
    /** NOTICE 阈值天数 */
    private static final int NOTICE_DAYS = 30;

    @Override
    @Transactional
    public int scanAndWarn() {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(NOTICE_DAYS);

        // 清理今日之前的旧预警（避免重复）
        baseMapper.delete(new LambdaQueryWrapper<BatchExpiryWarning>()
                .lt(BatchExpiryWarning::getCreateTime, today.atStartOfDay()));

        // 查询所有有库存的批次
        List<Stock> stocks = stockMapper.selectList(
                new LambdaQueryWrapper<Stock>().gt(Stock::getQuantity, BigDecimal.ZERO));
        if (stocks.isEmpty()) return 0;

        List<Long> batchIds = stocks.stream()
                .map(Stock::getBatchId).distinct().collect(Collectors.toList());

        // 查询到期日在 NOTICE_DAYS 内的批次
        List<Batch> batches = batchMapper.selectBatchIds(batchIds).stream()
                .filter(b -> b.getExpireDate() != null
                        && !b.getExpireDate().isAfter(maxDate))
                .collect(Collectors.toList());
        if (batches.isEmpty()) return 0;

        // 构建 batch → stock 映射
        Map<Long, List<Stock>> batchStockMap = stocks.stream()
                .filter(s -> batchIds.contains(s.getBatchId()))
                .collect(Collectors.groupingBy(Stock::getBatchId));

        // 加载 SKU 名称
        List<Long> skuIds = batches.stream().map(Batch::getSkuId).distinct().collect(Collectors.toList());
        Map<Long, String> skuNameMap = goodsSkuMapper.selectBatchIds(skuIds).stream()
                .collect(Collectors.toMap(GoodsSku::getId, GoodsSku::getSkuName));

        int warnCount = 0;
        for (Batch batch : batches) {
            long daysRemaining = ChronoUnit.DAYS.between(today, batch.getExpireDate());
            if (daysRemaining < 0) daysRemaining = -1; // 已过期

            String warnLevel = determineLevel((int) daysRemaining);
            if (warnLevel == null) continue; // 超过 NOTICE 范围

            List<Stock> batchStocks = batchStockMap.get(batch.getId());
            if (batchStocks == null || batchStocks.isEmpty()) continue;

            String skuName = skuNameMap.getOrDefault(batch.getSkuId(), "未知商品");

            for (Stock stock : batchStocks) {
                if (stock.getQuantity().compareTo(BigDecimal.ZERO) <= 0) continue;

                BatchExpiryWarning warning = new BatchExpiryWarning();
                warning.setBatchId(batch.getId());
                warning.setSkuId(batch.getSkuId());
                warning.setWarehouseId(stock.getWarehouseId());
                warning.setQuantity(stock.getQuantity());
                warning.setExpireDate(batch.getExpireDate());
                warning.setDaysRemaining((int) daysRemaining);
                warning.setWarnLevel(warnLevel);
                warning.setIsHandled(0);
                warning.setCreateTime(LocalDateTime.now());
                baseMapper.insert(warning);
                warnCount++;

                log.warn("[{}] 批次[{}] 商品[{}] 到期日[{}] 剩余{}天 库存{}kg",
                        warnLevel, batch.getBatchNo(), skuName,
                        batch.getExpireDate(), daysRemaining, stock.getQuantity());
            }
        }

        log.info("效期扫描完成: 发现 {} 条预警", warnCount);
        return warnCount;
    }

    @Override
    public List<BatchExpiryWarning> listUnhandled(String warnLevel) {
        LambdaQueryWrapper<BatchExpiryWarning> w = new LambdaQueryWrapper<BatchExpiryWarning>()
                .eq(BatchExpiryWarning::getIsHandled, 0);
        if (warnLevel != null && !warnLevel.isEmpty()) {
            w.eq(BatchExpiryWarning::getWarnLevel, warnLevel);
        }
        w.orderByAsc(BatchExpiryWarning::getDaysRemaining);
        return baseMapper.selectList(w);
    }

    @Override
    public void markHandled(Long warningId, Long userId) {
        BatchExpiryWarning warning = new BatchExpiryWarning();
        warning.setId(warningId);
        warning.setIsHandled(1);
        warning.setHandledBy(userId);
        warning.setHandledTime(LocalDateTime.now());
        baseMapper.updateById(warning);
    }

    /**
     * 根据剩余天数判定预警级别
     */
    private String determineLevel(int daysRemaining) {
        if (daysRemaining < 0) return "CRITICAL";
        if (daysRemaining <= CRITICAL_DAYS) return "CRITICAL";
        if (daysRemaining <= WARNING_DAYS) return "WARNING";
        if (daysRemaining <= NOTICE_DAYS) return "NOTICE";
        return null;
    }
}
