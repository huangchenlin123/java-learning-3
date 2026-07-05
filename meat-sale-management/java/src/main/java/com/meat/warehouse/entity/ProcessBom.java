package com.meat.warehouse.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class ProcessBom {
    private Long id; private String bomName; private Long rawSkuId; private String description;
    private Integer status; private LocalDateTime createTime; private LocalDateTime updateTime;
    @TableField(exist=false) private List<ProcessBomItem> items;
}
