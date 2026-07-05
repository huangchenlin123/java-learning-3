package com.meat.warehouse.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class StockIn {
    private Long id; private String inNo; private String inType; private Long sourceId;
    private Long warehouseId; private BigDecimal totalQuantity; private String status;
    private Long confirmBy; private LocalDateTime confirmTime;
    private LocalDateTime createTime; private LocalDateTime updateTime;
    @TableField(exist=false) private List<StockInItem> items;
}
