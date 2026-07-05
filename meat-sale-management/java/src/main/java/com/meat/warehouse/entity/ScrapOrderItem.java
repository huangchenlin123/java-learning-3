package com.meat.warehouse.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class ScrapOrderItem {
    private Long id; private Long scrapId; private Long skuId; private Long batchId;
    private BigDecimal quantity; private BigDecimal costPrice; private LocalDateTime createTime;
}
