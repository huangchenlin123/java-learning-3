package com.meat.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseOrderItem implements Serializable {
    private Long id;
    private Long orderId;
    private Long skuId;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal amount;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String skuName;
}
