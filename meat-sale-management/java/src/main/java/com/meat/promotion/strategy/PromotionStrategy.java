package com.meat.promotion.strategy;

import com.meat.sale.entity.Promotion;

import java.math.BigDecimal;

/**
 * 促销策略接口 — 策略模式核心契约
 * <p>
 * 每种促销类型（满减/折扣/特价/买赠）实现此接口，
 * 由 PromotionEngine 根据 promoType 分发调用。
 * </p>
 */
public interface PromotionStrategy {

    /**
     * 计算本策略产生的优惠金额
     *
     * @param ctx      促销上下文
     * @param promotion 当前促销活动
     * @return 优惠金额（单位：元），0 表示不满足条件
     */
    BigDecimal calculate(PromotionContext ctx, Promotion promotion);

    /**
     * 本策略对应的促销类型标识
     */
    String getType();
}
