package com.meat.sale.entity;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class MemberLevel {
    private Long id; private String levelName; private String levelCode;
    private BigDecimal minConsumption; private BigDecimal discountRate;
    private BigDecimal pointsRate; private Integer status;
}
