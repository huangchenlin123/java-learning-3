package com.meat.promotion.strategy;

import com.meat.sale.entity.Promotion;
import com.meat.sale.entity.PromotionRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 满减策略 — 订单有效金额达到门槛后直接扣减固定金额
 * <p>
 * 规则匹配逻辑：
 * <ul>
 *   <li>CONDITION 规则：threshold = 满减门槛金额</li>
 *   <li>ACTION 规则：discount = 立减金额</li>
 * </ul>
 * 示例：满 100 减 20 → CONDITION(threshold=100) + ACTION(discount=20)
 * </p>
 */
@Component
public class FullReductionStrategy implements PromotionStrategy {

    @Override
    public BigDecimal calculate(PromotionContext ctx, Promotion promotion) {
        if (promotion.getRules() == null || promotion.getRules().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal effectiveTotal = ctx.getEffectiveTotal();
        if (effectiveTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 查找 CONDITION 规则（门槛金额）
        BigDecimal threshold = BigDecimal.ZERO;
        BigDecimal reduction = BigDecimal.ZERO;

        for (PromotionRule rule : promotion.getRules()) {
            if ("CONDITION".equals(rule.getRuleType()) && rule.getThreshold() != null) {
                threshold = rule.getThreshold();
            } else if ("ACTION".equals(rule.getRuleType()) && rule.getDiscount() != null) {
                reduction = rule.getDiscount();
            }
        }

        // 判断是否满足门槛
        if (threshold.compareTo(BigDecimal.ZERO) <= 0 || reduction.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (effectiveTotal.compareTo(threshold) < 0) {
            return BigDecimal.ZERO;
        }

        // 满减金额不能超过有效金额
        return reduction.min(effectiveTotal);
    }

    @Override
    public String getType() {
        return "FULL_REDUCTION";
    }
}
