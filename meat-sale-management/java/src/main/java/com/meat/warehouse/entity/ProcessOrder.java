package com.meat.warehouse.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class ProcessOrder {
    private Long id; private String processNo; private Long bomId; private Long warehouseId;
    private Long rawSkuId; private Long rawBatchId; private BigDecimal rawQuantity;
    private BigDecimal rawCost; private String status; private Long createBy;
    private LocalDateTime createTime; private LocalDateTime updateTime;
    @TableField(exist=false) private List<ProcessItemOut> outputs;
}
