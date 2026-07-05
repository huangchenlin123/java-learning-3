package com.meat.trace.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class ColdChainLog {
    private Long id; private Long warehouseId; private BigDecimal temperature;
    private BigDecimal humidity; private Integer isAbnormal; private String abnormalDesc;
    private Long recordBy; private LocalDateTime createTime;
}
