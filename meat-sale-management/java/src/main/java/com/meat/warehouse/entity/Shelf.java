package com.meat.warehouse.entity;

import com.meat.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Shelf extends BaseEntity {
    private Long warehouseId;
    private String shelfCode;
    private String shelfName;
    private Integer status;
}
