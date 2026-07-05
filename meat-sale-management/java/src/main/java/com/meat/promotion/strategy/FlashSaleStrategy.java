package com.meat.promotion.strategy;

import com.meat.sale.entity.Promotion;
import com.meat.sale.entity.PromotionRule;
import com.meat.sale.entity.SaleOrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 限时特价策略 — 指定商品以特价出售，优惠金额 = (原价 - 特价) × 数量
 * <p>
 * 规则匹配逻辑：
 * <ul>
 *   <li>CONDITION 规则：skuId = 指定商品（必填）</li>
 *   <li>ACTION 规则：discount = 特价单价</li>
 * </ul>
 * 示例：猪肉原价 30 元/斤，特价 20 元/斤，买 2 斤 → 优惠 = (30-20) × 2 = 20 元
 * </p>
 */
@Component
public class FlashSaleStrategy implements PromotionStrategy {

    @Override
    public BigDecimal calculate(PromotionContext ctx, Promotion promotion) {
        if (promotion.getRules() == null || promotion.getRules().isEmpty()) {
            return BigDecimal.ZERO;
        }

        Long targetSkuId = null;
        BigDecimal specialPrice = null;

        for (PromotionRule rule : promotion.getRules()) {
            if ("CONDITION".equals(rule.getRuleType()) && rule.getSkuId() != null) {
                targetSkuId = rule.getSkuId();
            } else if ("ACTION".equals(rule.getRuleType()) && rule.getDiscount() != null) {
                specialPrice = rule.getDiscount();
            }
        }

        if (targetSkuId == null || specialPrice == null) {
            return BigDecimal.ZERO;
        }

        // 遍历订单明细，找到匹配商品并计算差价
        BigDecimal totalDiscount = BigDecimal.ZERO;
        if (ctx.getItems() != null) {
            for (SaleOrderItem item : ctx.getItems()) {
                if (targetSkuId.equals(item.getSkuId())) {
                    BigDecimal originalPrice = item.getOriginalPrice() != null ? item.getOriginalPrice() : BigDecimal.ZERO;
                    if (originalPrice.compareTo(specialPrice) > 0) {
                        BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
                        // 差价 × 数量
                        BigDecimal itemDiscount = originalPrice.subtract(specialPrice)
                                .multiply(qty)
                                .setScale(2, RoundingMode.HALF_UP);
                        totalDiscount = totalDiscount.add(itemDiscount);
                    }
                }
            }
        }

        return totalDiscount.min(ctx.getEffectiveTotal());
    }

    @Override
    public String getType() {
        return "FLASH_SALE";
    }
}
