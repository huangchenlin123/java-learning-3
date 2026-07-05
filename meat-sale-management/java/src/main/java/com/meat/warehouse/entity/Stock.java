package com.meat.warehouse.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class Stock {
    private Long id; private Long skuId; private Long batchId; private Long warehouseId;
    private Long shelfId; private BigDecimal quantity; private BigDecimal lockedQuantity;
    private Integer version;
    private LocalDateTime createTime; private LocalDateTime updateTime;
}
