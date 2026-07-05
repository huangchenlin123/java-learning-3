package com.meat.warehouse.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class ProcessBomItem {
    private Long id; private Long bomId; private Long outputSkuId;
    private BigDecimal outputRatio; private Integer sort; private LocalDateTime createTime;
}
