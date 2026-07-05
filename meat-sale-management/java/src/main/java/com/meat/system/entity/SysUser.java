package com.meat.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.meat.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer status;
    private Long warehouseId;

    @TableField(exist = false)
    private String roleNames;
    @TableField(exist = false)
    private Long[] roleIds;
}
