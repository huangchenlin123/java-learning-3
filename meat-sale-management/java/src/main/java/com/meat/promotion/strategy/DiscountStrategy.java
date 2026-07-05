package com.meat.promotion.strategy;

import com.meat.sale.entity.Promotion;
import com.meat.sale.entity.PromotionRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 折扣策略 — 订单有效金额达到门槛后按折扣率打折
 * <p>
 * 规则匹配逻辑：
 * <ul>
 *   <li>CONDITION 规则：threshold = 门槛金额（0 表示无门槛）</li>
 *   <li>ACTION 规则：discount = 折扣率（0.85 表示 85 折）</li>
 * </ul>
 * 优惠金额 = effectiveTotal × (1 - discount)
 * </p>
 */
@Component
public class DiscountStrategy implements PromotionStrategy {

    @Override
    public BigDecimal calculate(PromotionContext ctx, Promotion promotion) {
        if (promotion.getRules() == null || promotion.getRules().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal effectiveTotal = ctx.getEffectiveTotal();
        if (effectiveTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal threshold = BigDecimal.ZERO;
        BigDecimal discountRate = null;

        for (PromotionRule rule : promotion.getRules()) {
            if ("CONDITION".equals(rule.getRuleType()) && rule.getThreshold() != null) {
                threshold = rule.getThreshold();
            } else if ("ACTION".equals(rule.getRuleType()) && rule.getDiscount() != null) {
                discountRate = rule.getDiscount();
            }
        }

        if (discountRate == null) {
            return BigDecimal.ZERO;
        }

        // 折扣率合法性校验（0~1 之间）
        if (discountRate.compareTo(BigDecimal.ZERO) <= 0 || discountRate.compareTo(BigDecimal.ONE) > 0) {
            return BigDecimal.ZERO;
        }

        // 判断门槛
        if (threshold.compareTo(BigDecimal.ZERO) > 0 && effectiveTotal.compareTo(threshold) < 0) {
            return BigDecimal.ZERO;
        }

        // 优惠金额 = 有效金额 × (1 - 折扣率)
        BigDecimal discountFactor = BigDecimal.ONE.subtract(discountRate);
        return effectiveTotal.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getType() {
        return "DISCOUNT";
    }
}
