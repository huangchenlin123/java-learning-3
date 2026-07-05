package com.meat.warehouse.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class ScrapOrder {
    private Long id; private String scrapNo; private Long warehouseId;
    private BigDecimal totalQuantity; private BigDecimal totalAmount; private String scrapReason;
    private String photoUrl; private String status; private Long approveBy;
    private LocalDateTime approveTime; private Long createBy;
    private LocalDateTime createTime; private LocalDateTime updateTime;
    @TableField(exist=false) private List<ScrapOrderItem> items;
}
