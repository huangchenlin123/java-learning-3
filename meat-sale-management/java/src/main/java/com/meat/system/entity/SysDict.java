package com.meat.system.entity;

import com.meat.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysDict extends BaseEntity {
    private String dictName;
    private String dictCode;
    private Integer status;
}
