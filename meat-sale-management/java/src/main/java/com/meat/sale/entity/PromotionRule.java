package com.meat.sale.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class PromotionRule {
    private Long id; private Long promoId; private String ruleType;
    private Long skuId; private Long categoryId; private BigDecimal threshold;
    private BigDecimal discount; private Long giftSkuId; private LocalDateTime createTime;
}
