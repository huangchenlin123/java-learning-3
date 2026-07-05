package com.meat.sale.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meat.promotion.engine.PromotionEngine;
import com.meat.promotion.engine.PromotionResult;
import com.meat.promotion.strategy.PromotionContext;
import com.meat.sale.entity.Promotion;
import com.meat.sale.entity.SaleOrder;
import com.meat.sale.entity.SaleOrderItem;
import com.meat.sale.mapper.PromotionMapper;
import com.meat.sale.mapper.SaleOrderItemMapper;
import com.meat.sale.service.PromotionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 促销服务实现 — 委托 PromotionEngine 执行策略计算
 */
@Slf4j
@Service
public class PromotionServiceImpl extends ServiceImpl<PromotionMapper, Promotion> implements PromotionService {

    @Resource
    private PromotionEngine promotionEngine;

    @Resource
    private SaleOrderItemMapper saleOrderItemMapper;

    @Override
    public BigDecimal calculateDiscount(SaleOrder order) {
        PromotionResult result = calculateFull(order);
        return result != null ? result.getTotalDiscount() : BigDecimal.ZERO;
    }

    /**
     * 完整的促销计算（含赠品信息）
     *
     * @param order 销售订单
     * @return 完整促销结果
     */
    public PromotionResult calculateFull(SaleOrder order) {
        if (order == null) {
            return PromotionResult.builder().build();
        }

        // 查询订单明细
        List<SaleOrderItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            items = saleOrderItemMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SaleOrderItem>()
                            .eq(SaleOrderItem::getOrderId, order.getId()));
        }

        // 计算原始总金额
        BigDecimal originalTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        if (originalTotal.compareTo(BigDecimal.ZERO) <= 0 && items != null) {
            originalTotal = items.stream()
                    .map(i -> {
                        BigDecimal price = i.getFinalPrice() != null ? i.getFinalPrice()
                                : (i.getOriginalPrice() != null ? i.getOriginalPrice() : BigDecimal.ZERO);
                        BigDecimal qty = i.getQuantity() != null ? i.getQuantity() : BigDecimal.ZERO;
                        return price.multiply(qty);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // 构建上下文
        PromotionContext ctx = PromotionContext.builder()
                .order(order)
                .items(items)
                .originalTotal(originalTotal)
                .accumulatedDiscount(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO)
                .build();

        return promotionEngine.calculate(ctx);
    }
}
