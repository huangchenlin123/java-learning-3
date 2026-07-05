package com.meat.sale.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class SaleOrder {
    private Long id; private String orderNo; private String orderType;
    private Long customerId; private Long memberId; private BigDecimal totalAmount;
    private BigDecimal discountAmount; private BigDecimal finalAmount;
    private String paymentMethod; private String status; private Long approveBy;
    private Long createBy; private String remark;
    private LocalDateTime createTime; private LocalDateTime updateTime;
    @TableField(exist=false) private List<SaleOrderItem> items;
    @TableField(exist=false) private String customerName;
}
