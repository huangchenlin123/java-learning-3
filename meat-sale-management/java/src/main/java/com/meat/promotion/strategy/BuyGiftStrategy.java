package com.meat.promotion.strategy;

import com.meat.sale.entity.Promotion;
import com.meat.sale.entity.PromotionRule;
import com.meat.sale.entity.SaleOrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 买赠策略 — 购买指定商品满 N 件，赠送 M 件赠品
 * <p>
 * 规则匹配逻辑：
 * <ul>
 *   <li>CONDITION 规则：skuId = 指定商品，threshold = 满 N 件</li>
 *   <li>ACTION 规则：giftSkuId = 赠品商品 ID，discount = 赠送数量</li>
 * </ul>
 * 本策略不产生金额优惠，而是通过向订单添加赠品行实现。
 * 调用方应在引擎计算结果中检查赠品列表并处理。
 * </p>
 */
@Component
public class BuyGiftStrategy implements PromotionStrategy {

    @Override
    public BigDecimal calculate(PromotionContext ctx, Promotion promotion) {
        if (promotion.getRules() == null || promotion.getRules().isEmpty()) {
            return BigDecimal.ZERO;
        }

        Long conditionSkuId = null;
        BigDecimal thresholdQty = null;  // 满 N 件
        Long giftSkuId = null;
        BigDecimal giftQty = null;       // 赠 M 件

        for (PromotionRule rule : promotion.getRules()) {
            if ("CONDITION".equals(rule.getRuleType())) {
                conditionSkuId = rule.getSkuId();
                thresholdQty = rule.getThreshold();
            } else if ("ACTION".equals(rule.getRuleType())) {
                giftSkuId = rule.getGiftSkuId();
                giftQty = rule.getDiscount();
            }
        }

        if (conditionSkuId == null || thresholdQty == null || giftSkuId == null || giftQty == null) {
            return BigDecimal.ZERO;
        }
        if (thresholdQty.compareTo(BigDecimal.ZERO) <= 0 || giftQty.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 统计购买数量
        BigDecimal purchasedQty = BigDecimal.ZERO;
        if (ctx.getItems() != null) {
            for (SaleOrderItem item : ctx.getItems()) {
                if (conditionSkuId.equals(item.getSkuId())) {
                    BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
                    purchasedQty = purchasedQty.add(qty);
                }
            }
        }

        // 判断是否满足条件
        if (purchasedQty.compareTo(thresholdQty) < 0) {
            return BigDecimal.ZERO;
        }

        // 计算可赠送份数（向下取整）
        int times = purchasedQty.divide(thresholdQty, 0, RoundingMode.DOWN).intValue();
        if (times <= 0) {
            return BigDecimal.ZERO;
        }

        // 赠品总数量（份数 × 每份赠品数）返回赠品 SKU 信息（使用负值作为标记，实际处理由引擎负责）
        // 注意：这里返回赠品 SKU ID 的负数编码，引擎识别后执行赠品添加逻辑
        return giftQty.multiply(BigDecimal.valueOf(times)).negate();
    }

    @Override
    public String getType() {
        return "BUY_GIFT";
    }
}
