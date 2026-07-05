package com.meat.sale.service.impl;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.common.exception.BusinessException;
import com.meat.promotion.engine.PromotionResult;
import com.meat.promotion.strategy.PromotionContext;
import com.meat.purchase.entity.GoodsSku;
import com.meat.purchase.mapper.GoodsSkuMapper;
import com.meat.sale.entity.*;
import com.meat.sale.mapper.*;
import com.meat.sale.service.SaleOrderService;
import com.meat.warehouse.entity.*;
import com.meat.warehouse.mapper.*;
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

@Slf4j
@Service
public class SaleOrderServiceImpl extends ServiceImpl<SaleOrderMapper, SaleOrder> implements SaleOrderService {

    @Resource private SaleOrderItemMapper itemMapper;
    @Resource private StockMapper stockMapper;
    @Resource private BatchMapper batchMapper;
    @Resource private StockOutMapper stockOutMapper;
    @Resource private StockOutItemMapper stockOutItemMapper;
    @Resource private GoodsSkuMapper goodsSkuMapper;
    @Resource private PromotionServiceImpl promotionServiceImpl;

    @Override
    @Transactional
    public void posSale(SaleOrder order) {
        // ---- 1. 基础信息设置 ----
        order.setOrderType("RETAIL");
        order.setStatus("COMPLETED");
        order.setCreateBy(StpUtil.getLoginIdAsLong());
        if (order.getOrderNo() == null) {
            order.setOrderNo("SO" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + String.format("%06d", System.nanoTime() % 1000000));
        }
        super.save(order);

        // ---- 2. 创建出库单 ----
        StockOut stockOut = new StockOut();
        stockOut.setOutNo("OUT" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%06d", System.nanoTime() % 1000000));
        stockOut.setOutType("SALE");
        stockOut.setSourceId(order.getId());
        stockOut.setWarehouseId(1L);

        // ---- 3. 处理订单明细 + FIFO 出库 ----
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<StockOutItem> pendingOutItems = new ArrayList<>();

        for (SaleOrderItem item : order.getItems()) {
            item.setOrderId(order.getId());
            if (item.getAmount() == null && item.getFinalPrice() != null) {
                item.setAmount(item.getFinalPrice().multiply(item.getQuantity()));
            }
            totalAmount = totalAmount.add(item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO);

            // FIFO 出库
            fifoDeduct(item.getSkuId(), item.getQuantity(), 1L, item.getSkuId(), pendingOutItems);
            itemMapper.insert(item);
        }

        order.setTotalAmount(totalAmount);

        // ---- 4. 促销计算 ----
        PromotionResult promoResult = promotionServiceImpl.calculateFull(order);
        BigDecimal discountAmount = promoResult.getTotalDiscount();
        order.setDiscountAmount(discountAmount);

        // ---- 5. 处理赠品（买赠活动） ----
        for (PromotionResult.GiftEntry gift : promoResult.getGifts()) {
            GoodsSku giftSku = goodsSkuMapper.selectById(gift.getSkuId());
            if (giftSku == null) {
                log.warn("赠品 SKU[{}] 不存在，跳过", gift.getSkuId());
                continue;
            }
            // 创建赠品行（价格 = 0）
            SaleOrderItem giftItem = new SaleOrderItem();
            giftItem.setOrderId(order.getId());
            giftItem.setSkuId(gift.getSkuId());
            giftItem.setQuantity(gift.getQuantity());
            giftItem.setOriginalPrice(giftSku.getDefaultPrice() != null ? giftSku.getDefaultPrice() : BigDecimal.ZERO);
            giftItem.setFinalPrice(BigDecimal.ZERO);
            giftItem.setAmount(BigDecimal.ZERO);
            itemMapper.insert(giftItem);

            // 赠品也需从库存中扣减（FIFO）
            try {
                fifoDeduct(gift.getSkuId(), gift.getQuantity(), 1L, gift.getSkuId(), pendingOutItems);
            } catch (BusinessException e) {
                log.warn("赠品 [{}] 库存不足，跳过库存扣减: {}", giftSku.getSkuName(), e.getMessage());
            }
        }

        // ---- 6. 保存出库单并关联出库明细 ----
        BigDecimal outTotal = pendingOutItems.stream()
                .map(i -> i.getQuantity() != null ? i.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stockOut.setTotalQuantity(outTotal);
        stockOutMapper.insert(stockOut);

        for (StockOutItem outItem : pendingOutItems) {
            outItem.setOutId(stockOut.getId());
            stockOutItemMapper.insert(outItem);
        }

        // ---- 7. 更新订单最终金额 ----
        order.setFinalAmount(totalAmount.subtract(discountAmount).max(BigDecimal.ZERO));
        super.updateById(order);

        log.info("POS 收银完成: 订单[{}], 原价={}, 优惠={}, 实付={}, 赠品={}件",
                order.getOrderNo(), totalAmount, discountAmount, order.getFinalAmount(),
                promoResult.getGifts().size());
    }

    /**
     * FIFO 先进先出库存扣减（按批次有效期排序）
     *
     * @param skuId          商品 ID
     * @param quantity       扣减数量
     * @param warehouseId    仓库 ID
     * @param stockOutSkuId  出库明细记录的 skuId（可能与扣减 skuId 不同，如赠品）
     * @param pendingOutItems 出库明细收集列表
     */
    private void fifoDeduct(Long skuId, BigDecimal quantity, Long warehouseId,
                            Long stockOutSkuId, List<StockOutItem> pendingOutItems) {
        List<Stock> stocks = stockMapper.selectList(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getSkuId, skuId)
                .eq(Stock::getWarehouseId, warehouseId)
                .gt(Stock::getQuantity, BigDecimal.ZERO));
        if (stocks.isEmpty()) {
            throw new BusinessException("商品库存不足");
        }

        List<Batch> batches = batchMapper.selectBatchIds(
                stocks.stream().map(Stock::getBatchId).collect(Collectors.toList()));
        batches.sort(Comparator.comparing(Batch::getExpireDate,
                Comparator.nullsLast(Comparator.naturalOrder())));

        BigDecimal remaining = quantity;
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

            StockOutItem outItem = new StockOutItem();
            outItem.setSkuId(stockOutSkuId);
            outItem.setBatchId(batch.getId());
            outItem.setQuantity(deduct);
            pendingOutItems.add(outItem);
        }
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("商品库存不足");
        }
    }

    @Override
    @Transactional
    public void approveOrder(Long orderId, boolean approved) {
        SaleOrder order = getById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        order.setStatus(approved ? "APPROVED" : "CANCELLED");
        order.setApproveBy(StpUtil.getLoginIdAsLong());
        updateById(order);
    }
}
