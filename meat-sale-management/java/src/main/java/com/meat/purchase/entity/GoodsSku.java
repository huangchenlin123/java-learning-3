package com.meat.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.meat.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsSku extends BaseEntity {
    private Long categoryId;
    private String skuCode;
    private String skuName;
    private String unit;
    private String barcode;
    private BigDecimal defaultPrice;
    private BigDecimal safetyStock;
    private String description;
    private Integer status;

    @TableField(exist = false)
    private String categoryName;
}
