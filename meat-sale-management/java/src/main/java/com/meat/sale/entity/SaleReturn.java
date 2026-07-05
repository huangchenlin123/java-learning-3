package com.meat.sale.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class SaleReturn {
    private Long id; private String returnNo; private Long orderId; private Long customerId;
    private BigDecimal totalAmount; private String returnReason; private Integer qualityResult;
    private String status; private Long createBy;
    private LocalDateTime createTime; private LocalDateTime updateTime;
    @TableField(exist=false) private List<SaleReturnItem> items;
}
