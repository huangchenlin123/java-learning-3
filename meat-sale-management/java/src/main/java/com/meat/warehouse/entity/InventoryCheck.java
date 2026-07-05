package com.meat.warehouse.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class InventoryCheck {
    private Long id; private String checkNo; private Long warehouseId; private Integer freezeStock;
    private String checkRange; private String status; private Long createBy; private LocalDateTime submitTime;
    private Long approveBy; private LocalDateTime approveTime;
    private LocalDateTime createTime; private LocalDateTime updateTime;
    @TableField(exist=false) private List<InventoryCheckItem> items;
}
