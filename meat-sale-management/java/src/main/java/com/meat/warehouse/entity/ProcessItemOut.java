package com.meat.warehouse.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class ProcessItemOut {
    private Long id; private Long processId; private Long skuId; private String batchNo;
    private BigDecimal expectedQuantity; private BigDecimal actualQuantity;
    private BigDecimal costPrice; private BigDecimal wasteQuantity; private LocalDateTime createTime;
}
