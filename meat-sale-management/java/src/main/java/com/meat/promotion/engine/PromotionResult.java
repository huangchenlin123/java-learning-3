package com.meat.promotion.engine;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 促销计算结果
 */
@Data
@Builder
public class PromotionResult {

    /** 总优惠金额 */
    @Builder.Default
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    /** 赠品明细 */
    @Builder.Default
    private List<GiftEntry> gifts = new ArrayList<>();

    /** 命中的促销活动名称列表 */
    @Builder.Default
    private List<String> appliedPromotions = new ArrayList<>();

    /**
     * 赠品条目
     */
    @Data
    @Builder
    public static class GiftEntry {
        /** 赠品 SKU ID */
        private Long skuId;
        /** 赠送数量 */
        private BigDecimal quantity;
        /** 来源促销活动名 */
        private String promoName;
    }

    public void addDiscount(BigDecimal amount, String promoName) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.totalDiscount = this.totalDiscount.add(amount);
            this.appliedPromotions.add(promoName);
        }
    }

    public void addGift(Long skuId, BigDecimal quantity, String promoName) {
        if (skuId != null && quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0) {
            this.gifts.add(GiftEntry.builder()
                    .skuId(skuId)
                    .quantity(quantity)
                    .promoName(promoName)
                    .build());
            if (!this.appliedPromotions.contains(promoName)) {
                this.appliedPromotions.add(promoName);
            }
        }
    }
}
