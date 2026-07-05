package com.meat.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.meat.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseEntity {
    private String roleName;
    private String roleCode;
    private String description;
    private Integer dataScope;
    private Integer status;

    @TableField(exist = false)
    private Long[] menuIds;
}
