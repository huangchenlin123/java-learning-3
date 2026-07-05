package com.meat.warehouse.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class StockOut {
    private Long id; private String outNo; private String outType; private Long sourceId;
    private Long warehouseId; private BigDecimal totalQuantity; private String status;
    private LocalDateTime createTime;
    @TableField(exist=false) private List<StockOutItem> items;
}
