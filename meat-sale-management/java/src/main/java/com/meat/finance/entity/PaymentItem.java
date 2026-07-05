package com.meat.finance.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class PaymentItem {
    private Long id; private Long paymentId; private String paymentMethod;
    private BigDecimal amount; private String referenceNo; private LocalDateTime createTime;
}
