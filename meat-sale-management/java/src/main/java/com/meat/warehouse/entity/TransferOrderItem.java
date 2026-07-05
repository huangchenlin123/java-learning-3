package com.meat.warehouse.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class TransferOrderItem {
    private Long id; private Long transferId; private Long skuId; private Long batchId;
    private BigDecimal quantity; private LocalDateTime createTime;
}
