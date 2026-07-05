package com.meat.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.meat.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrder extends BaseEntity {
    private String orderNo;
    private Long supplierId;
    private BigDecimal totalAmount;
    private LocalDate expectArriveTime;
    private String status;
    private Long approveBy;
    private LocalDateTime approveTime;
    private String remark;
    private Long createBy;

    @TableField(exist = false)
    private String supplierName;
    @TableField(exist = false)
    private List<PurchaseOrderItem> items;
}
