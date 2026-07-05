package com.meat.purchase.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class PurchaseReceipt {
    private Long id; private Long orderId; private String receiptNo;
    private BigDecimal actualWeight; private Integer qualityResult;
    private String quarantineNo; private String origin; private LocalDate slaughterDate;
    private BigDecimal diffRatio; private Integer diffHandled; private String status;
    private LocalDateTime createTime; private LocalDateTime updateTime;
    @TableField(exist=false) private List<PurchaseReceiptItem> items;
    @TableField(exist=false) private String supplierName;
}
