package com.meat.finance.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class Payment {
    private Long id; private String paymentNo; private Long orderId; private String orderType;
    private String paymentMethod; private BigDecimal amount; private Long createBy;
    private LocalDateTime createTime;
    @TableField(exist=false) private List<PaymentItem> items;
}
