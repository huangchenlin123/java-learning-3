package com.meat.purchase.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class PurchaseReceiptItem {
    private Long id; private Long receiptId; private Long orderItemId;
    private Long skuId; private BigDecimal orderQuantity; private BigDecimal actualQuantity;
    private BigDecimal price; private Integer qualityStatus; private LocalDateTime createTime;
}
