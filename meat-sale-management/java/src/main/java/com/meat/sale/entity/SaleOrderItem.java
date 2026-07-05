package com.meat.sale.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class SaleOrderItem {
    private Long id; private Long orderId; private Long skuId; private Long batchId;
    private BigDecimal quantity; private BigDecimal originalPrice;
    private BigDecimal finalPrice; private BigDecimal amount; private LocalDateTime createTime;
}
