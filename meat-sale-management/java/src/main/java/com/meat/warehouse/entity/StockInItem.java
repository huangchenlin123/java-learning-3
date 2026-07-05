package com.meat.warehouse.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class StockInItem {
    private Long id; private Long inId; private Long skuId; private Long batchId;
    private BigDecimal quantity; private BigDecimal costPrice; private Long shelfId;
    private LocalDateTime createTime;
}
