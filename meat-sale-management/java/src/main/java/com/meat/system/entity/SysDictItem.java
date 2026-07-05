package com.meat.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.meat.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictItem extends BaseEntity {
    private Long dictId;
    private String itemLabel;
    private String itemValue;
    private Integer sort;
    private Integer status;

    @TableField(exist = false)
    private String dictCode;
}
