package com.meat.sale.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class Promotion {
    private Long id; private String promoName; private String promoType;
    private LocalDateTime startTime; private LocalDateTime endTime;
    private Integer priority; private Integer status; private String description;
    private LocalDateTime createTime; private LocalDateTime updateTime;
    @TableField(exist=false) private List<PromotionRule> rules;
}
