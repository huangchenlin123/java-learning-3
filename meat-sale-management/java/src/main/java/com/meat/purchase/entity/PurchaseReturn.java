package com.meat.purchase.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class PurchaseReturn {
    private Long id; private String returnNo; private Long receiptId; private Long supplierId;
    private BigDecimal totalAmount; private String returnReason; private String status;
    private LocalDateTime createTime; private LocalDateTime updateTime;
    @TableField(exist=false) private List<PurchaseReturnItem> items;
    @TableField(exist=false) private String supplierName;
}
