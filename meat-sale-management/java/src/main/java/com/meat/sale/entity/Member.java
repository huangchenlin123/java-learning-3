package com.meat.sale.entity;
import com.meat.common.BaseEntity;
import lombok.Data; import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
@Data @EqualsAndHashCode(callSuper=true)
public class Member extends BaseEntity {
    private String cardNo; private String memberName; private String phone;
    private String idCard; private Long levelId; private Integer totalPoints;
    private Integer availablePoints; private BigDecimal totalConsumption; private Integer status;
}
