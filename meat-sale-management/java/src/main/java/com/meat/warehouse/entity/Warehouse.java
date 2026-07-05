package com.meat.warehouse.entity;

import com.meat.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Warehouse extends BaseEntity {
    private String warehouseName;
    private String warehouseCode;
    private String warehouseType;
    private String address;
    private String manager;
    private String phone;
    private Integer status;
}
