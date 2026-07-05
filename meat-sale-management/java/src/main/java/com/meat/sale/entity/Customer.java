package com.meat.sale.entity;
import com.meat.common.BaseEntity;
import lombok.Data; import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
@Data @EqualsAndHashCode(callSuper=true)
public class Customer extends BaseEntity {
    private String customerName; private String customerType; private String contactPerson;
    private String phone; private String address; private String taxNo;
    private BigDecimal creditLimit; private Integer status;
}
