package com.meat.purchase.entity;

import com.meat.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Supplier extends BaseEntity {
    private String supplierName;
    private String supplierCode;
    private String contactPerson;
    private String phone;
    private String address;
    private String licenseNo;
    private String quarantineCert;
    private Integer rating;
    private Integer status;
}
