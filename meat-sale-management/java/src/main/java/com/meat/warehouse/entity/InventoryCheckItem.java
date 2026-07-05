package com.meat.warehouse.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class InventoryCheckItem {
    private Long id; private Long checkId; private Long skuId; private Long batchId;
    private BigDecimal bookQuantity; private BigDecimal actualQuantity;
    private BigDecimal diffQuantity; private String diffReason; private LocalDateTime createTime;
}
