package com.meat.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.meat.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsCategory extends BaseEntity {
    private Long parentId;
    private String categoryName;
    private String categoryCode;
    private Integer sort;
    private Integer status;

    @TableField(exist = false)
    private List<GoodsCategory> children;
}
