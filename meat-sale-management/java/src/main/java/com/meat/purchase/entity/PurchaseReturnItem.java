package com.meat.purchase.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class PurchaseReturnItem {
    private Long id; private Long returnId; private Long skuId;
    private BigDecimal quantity; private BigDecimal price; private BigDecimal amount;
    private LocalDateTime createTime;
}
