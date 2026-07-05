package com.meat.warehouse.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 批次效期预警记录
 */
@Data
public class BatchExpiryWarning {
    private Long id;
    private Long batchId;
    private Long skuId;
    private Long warehouseId;
    private BigDecimal quantity;
    private LocalDate expireDate;
    private Integer daysRemaining;
    private String warnLevel;
    private Integer isHandled;
    private Long handledBy;
    private LocalDateTime handledTime;
    private LocalDateTime createTime;
}
