package com.meat.promotion.strategy;

import com.meat.sale.entity.SaleOrder;
import com.meat.sale.entity.SaleOrderItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 促销计算上下文 — 封装订单信息供各策略使用
 */
@Data
@Builder
public class PromotionContext {

    /** 原始销售订单 */
    private SaleOrder order;

    /** 订单明细 */
    private List<SaleOrderItem> items;

    /** 订单原始总金额（策略计算前） */
    private BigDecimal originalTotal;

    /** 当前已累计的优惠金额（链式叠加时使用） */
    private BigDecimal accumulatedDiscount;

    /** 会员等级折扣率（null 表示非会员） */
    private BigDecimal memberDiscountRate;

    /**
     * 当前有效金额（扣减已累计优惠后）
     */
    public BigDecimal getEffectiveTotal() {
        BigDecimal acc = accumulatedDiscount != null ? accumulatedDiscount : BigDecimal.ZERO;
        BigDecimal orig = originalTotal != null ? originalTotal : BigDecimal.ZERO;
        return orig.subtract(acc).max(BigDecimal.ZERO);
    }
}
