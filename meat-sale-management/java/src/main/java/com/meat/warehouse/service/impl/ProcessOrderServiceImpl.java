package com.meat.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.common.exception.BusinessException;
import com.meat.warehouse.entity.*;
import com.meat.warehouse.mapper.*;
import com.meat.warehouse.service.ProcessOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分割加工服务实现 — 核心：原料出库 → 成本分摊 → 产出品入库
 */
@Slf4j
@Service
public class ProcessOrderServiceImpl extends ServiceImpl<ProcessOrderMapper, ProcessOrder>
        implements ProcessOrderService {

    @Resource private ProcessBomMapper bomMapper;
    @Resource private ProcessBomItemMapper bomItemMapper;
    @Resource private ProcessItemOutMapper processItemOutMapper;
    @Resource private StockMapper stockMapper;
    @Resource private BatchMapper batchMapper;
    @Resource private StockOutMapper stockOutMapper;
    @Resource private StockOutItemMapper stockOutItemMapper;
    @Resource private StockInMapper stockInMapper;
    @Resource private StockInItemMapper stockInItemMapper;

    @Override
    @Transactional
    public void completeProcess(Long processId, Long userId) {
        // ---- 1. 校验 ----
        ProcessOrder order = getById(processId);
        if (order == null) throw new BusinessException("加工单不存在");
        if (!"PENDING".equals(order.getStatus())) throw new BusinessException("加工单状态不正确，当前：" + order.getStatus());

        // 加载 BOM
        ProcessBom bom = null;
        List<ProcessBomItem> bomItems = Collections.emptyList();
        if (order.getBomId() != null) {
            bom = bomMapper.selectById(order.getBomId());
            if (bom != null) {
                bomItems = bomItemMapper.selectList(
                        new LambdaQueryWrapper<ProcessBomItem>()
                                .eq(ProcessBomItem::getBomId, order.getBomId())
                                .orderByAsc(ProcessBomItem::getSort));
            }
        }

        // 加载/保存产出明细
        List<ProcessItemOut> outputs = order.getOutputs();
        if (outputs == null || outputs.isEmpty()) {
            // 尝试从 DB 加载已有输出明细
            outputs = processItemOutMapper.selectList(
                    new LambdaQueryWrapper<ProcessItemOut>().eq(ProcessItemOut::getProcessId, processId));
        }
        if (outputs == null || outputs.isEmpty()) {
            throw new BusinessException("请先填写加工产出明细");
        }

        // 计算预计产出（基于 BOM 比例）
        if (!bomItems.isEmpty()) {
            for (ProcessBomItem bomItem : bomItems) {
                for (ProcessItemOut out : outputs) {
                    if (out.getSkuId().equals(bomItem.getOutputSkuId())) {
                        BigDecimal expected = order.getRawQuantity()
                                .multiply(bomItem.getOutputRatio())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        out.setExpectedQuantity(expected);
                    }
                }
            }
        }

        // 确保 actualQuantity 不为空
        for (ProcessItemOut out : outputs) {
            if (out.getActualQuantity() == null) {
                out.setActualQuantity(out.getExpectedQuantity() != null
                        ? out.getExpectedQuantity() : BigDecimal.ZERO);
            }
            out.setProcessId(processId);
        }

        // ---- 2. 计算/确认原料成本 ----
        BigDecimal rawCost = order.getRawCost();
        if (rawCost == null || rawCost.compareTo(BigDecimal.ZERO) <= 0) {
            // 尝试从原料批次的入库记录获取成本
            rawCost = lookupRawCost(order.getRawSkuId(), order.getRawBatchId());
        }
        if (rawCost == null || rawCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("原料成本未设置，无法分摊");
        }
        order.setRawCost(rawCost);

        // ---- 3. 原料 FIFO 出库 ----
        StockOut stockOut = new StockOut();
        stockOut.setOutNo(generateNo("OUT"));
        stockOut.setOutType("PROCESS");
        stockOut.setSourceId(processId);
        stockOut.setWarehouseId(order.getWarehouseId());
        stockOut.setStatus("CONFIRMED");

        // 先出库存（不插入明细），收集待插入的明细
        List<StockOutItem> pendingOutItems = new ArrayList<>();
        BigDecimal outTotal = fifoDeduct(order.getRawSkuId(), order.getRawQuantity(),
                order.getWarehouseId(), pendingOutItems);
        stockOut.setTotalQuantity(outTotal);
        stockOutMapper.insert(stockOut);

        // 插入出库明细（outId 已存在）
        for (StockOutItem outItem : pendingOutItems) {
            outItem.setOutId(stockOut.getId());
            stockOutItemMapper.insert(outItem);
        }

        // ---- 4. 成本分摊（按实际产出重量比例） ----
        BigDecimal totalActual = outputs.stream()
                .map(o -> o.getActualQuantity() != null ? o.getActualQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalActual.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("实际产出总量为 0，无法分摊成本");
        }

        BigDecimal costPerUnit = rawCost.divide(totalActual, 6, RoundingMode.HALF_UP);

        for (ProcessItemOut out : outputs) {
            BigDecimal actualQty = out.getActualQuantity() != null ? out.getActualQuantity() : BigDecimal.ZERO;
            BigDecimal expectedQty = out.getExpectedQuantity() != null ? out.getExpectedQuantity() : BigDecimal.ZERO;

            // 成本分摊
            BigDecimal itemCost = costPerUnit.multiply(actualQty).setScale(2, RoundingMode.HALF_UP);
            out.setCostPrice(itemCost);

            // 计算损耗
            BigDecimal waste = expectedQty.subtract(actualQty);
            out.setWasteQuantity(waste.compareTo(BigDecimal.ZERO) > 0 ? waste : BigDecimal.ZERO);

            // 持久化产出明细
            if (out.getId() != null) {
                processItemOutMapper.updateById(out);
            } else {
                processItemOutMapper.insert(out);
            }
        }

        // 校准总成本（最后一项补偿舍入误差）
        BigDecimal allocatedCost = outputs.stream()
                .map(o -> o.getCostPrice() != null ? o.getCostPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal roundingDiff = rawCost.subtract(allocatedCost);
        if (roundingDiff.compareTo(BigDecimal.ZERO) != 0 && !outputs.isEmpty()) {
            ProcessItemOut last = outputs.get(outputs.size() - 1);
            last.setCostPrice(last.getCostPrice().add(roundingDiff));
            processItemOutMapper.updateById(last);
        }

        // ---- 5. 产出品入库 ----
        StockIn stockIn = new StockIn();
        stockIn.setInNo(generateNo("IN"));
        stockIn.setInType("PROCESS");
        stockIn.setSourceId(processId);
        stockIn.setWarehouseId(order.getWarehouseId());
        stockIn.setStatus("CONFIRMED");
        stockIn.setConfirmBy(userId);
        stockIn.setConfirmTime(LocalDateTime.now());

        BigDecimal inTotal = BigDecimal.ZERO;
        List<StockInItem> inItems = new ArrayList<>();

        int batchSeq = 0;
        for (ProcessItemOut out : outputs) {
            BigDecimal actualQty = out.getActualQuantity() != null ? out.getActualQuantity() : BigDecimal.ZERO;
            if (actualQty.compareTo(BigDecimal.ZERO) <= 0) continue;

            // 为新产出品创建批次
            batchSeq++;
            Batch batch = new Batch();
            batch.setBatchNo(order.getProcessNo() + "-" + batchSeq);
            batch.setSkuId(out.getSkuId());
            batch.setProductDate(LocalDate.now());
            batch.setSourceType("PROCESS");
            batch.setSourceId(processId);
            batchMapper.insert(batch);

            // 更新产出明细的批次号
            out.setBatchNo(batch.getBatchNo());
            processItemOutMapper.updateById(out);

            // 入库明细
            StockInItem inItem = new StockInItem();
            inItem.setSkuId(out.getSkuId());
            inItem.setBatchId(batch.getId());
            inItem.setQuantity(actualQty);
            inItem.setCostPrice(out.getCostPrice());
            inItems.add(inItem);

            inTotal = inTotal.add(actualQty);

            // 更新/新增库存
            Stock stock = stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                    .eq(Stock::getSkuId, out.getSkuId())
                    .eq(Stock::getBatchId, batch.getId())
                    .eq(Stock::getWarehouseId, order.getWarehouseId()));
            if (stock != null) {
                stock.setQuantity(stock.getQuantity().add(actualQty));
                stockMapper.updateById(stock);
            } else {
                stock = new Stock();
                stock.setSkuId(out.getSkuId());
                stock.setBatchId(batch.getId());
                stock.setWarehouseId(order.getWarehouseId());
                stock.setQuantity(actualQty);
                stock.setLockedQuantity(BigDecimal.ZERO);
                stockMapper.insert(stock);
            }
        }

        stockIn.setTotalQuantity(inTotal);
        stockInMapper.insert(stockIn);

        // 关联 StockInItem
        for (StockInItem item : inItems) {
            item.setInId(stockIn.getId());
            stockInItemMapper.insert(item);
        }

        // ---- 6. 完成 ----
        order.setStatus("COMPLETED");
        updateById(order);

        log.info("加工完成: 单号[{}], 原料成本={}, 产出{}项, 总产出={}kg, 损耗合计={}kg",
                order.getProcessNo(), rawCost, outputs.size(), totalActual,
                outputs.stream().map(o -> o.getWasteQuantity() != null ? o.getWasteQuantity() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    // ========== 辅助方法 ==========

    /**
     * FIFO 库存扣减
     */
    private BigDecimal fifoDeduct(Long skuId, BigDecimal quantity, Long warehouseId,
                                   List<StockOutItem> pendingOutItems) {
        List<Stock> stocks = stockMapper.selectList(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getSkuId, skuId)
                .eq(Stock::getWarehouseId, warehouseId)
                .gt(Stock::getQuantity, BigDecimal.ZERO));
        if (stocks.isEmpty()) throw new BusinessException("原料库存不足: SKU[" + skuId + "] 无可用库存");

        List<Batch> batches = batchMapper.selectBatchIds(
                stocks.stream().map(Stock::getBatchId).collect(Collectors.toList()));
        batches.sort(Comparator.comparing(Batch::getExpireDate,
                Comparator.nullsLast(Comparator.naturalOrder())));

        BigDecimal remaining = quantity;
        BigDecimal totalDeducted = BigDecimal.ZERO;

        for (Batch batch : batches) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            Stock stock = stocks.stream()
                    .filter(s -> s.getBatchId().equals(batch.getId()))
                    .findFirst().orElse(null);
            if (stock == null) continue;

            BigDecimal deduct = stock.getQuantity().min(remaining);
            stock.setQuantity(stock.getQuantity().subtract(deduct));
            stockMapper.updateById(stock);
            remaining = remaining.subtract(deduct);
            totalDeducted = totalDeducted.add(deduct);

            StockOutItem outItem = new StockOutItem();
            outItem.setSkuId(skuId);
            outItem.setBatchId(batch.getId());
            outItem.setQuantity(deduct);
            pendingOutItems.add(outItem);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("原料库存不足，还缺 " + remaining + " kg");
        }
        return totalDeducted;
    }

    /**
     * 从入库记录查找原料成本
     */
    private BigDecimal lookupRawCost(Long skuId, Long batchId) {
        StockInItem item = stockInItemMapper.selectOne(new LambdaQueryWrapper<StockInItem>()
                .eq(StockInItem::getSkuId, skuId)
                .eq(StockInItem::getBatchId, batchId)
                .orderByDesc(StockInItem::getCreateTime)
                .last("LIMIT 1"));
        return item != null ? item.getCostPrice() : null;
    }

    /**
     * 生成单号
     */
    private String generateNo(String prefix) {
        return prefix + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%06d", System.nanoTime() % 1000000);
    }
}
