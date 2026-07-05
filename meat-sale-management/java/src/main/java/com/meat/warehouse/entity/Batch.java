package com.meat.warehouse.entity;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
public class Batch {
    private Long id; private String batchNo; private Long skuId;
    private LocalDate productDate; private Integer shelfLife; private LocalDate expireDate;
    private String sourceType; private Long sourceId;
    private String quarantineNo; private String origin; private LocalDateTime createTime;
}
