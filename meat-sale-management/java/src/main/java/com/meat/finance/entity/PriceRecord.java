package com.meat.finance.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class PriceRecord {
    private Long id; private Long skuId; private String priceType;
    private BigDecimal price; private Long memberLevelId;
    private LocalDateTime effectiveTime; private Long createBy; private LocalDateTime createTime;
}
