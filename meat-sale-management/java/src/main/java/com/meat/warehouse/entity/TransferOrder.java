package com.meat.warehouse.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class TransferOrder {
    private Long id; private String transferNo; private Long fromWarehouseId;
    private Long toWarehouseId; private BigDecimal totalQuantity; private String reason;
    private String status; private Long approveBy; private LocalDateTime approveTime;
    private Long createBy; private LocalDateTime createTime; private LocalDateTime updateTime;
    @TableField(exist=false) private List<TransferOrderItem> items;
}
