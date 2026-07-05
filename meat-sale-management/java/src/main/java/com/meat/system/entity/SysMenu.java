package com.meat.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.meat.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysMenu extends BaseEntity {
    private Long parentId;
    private String menuName;
    private Integer menuType;
    private String path;
    private String component;
    private String perms;
    private String icon;
    private Integer sort;
    private Integer status;

    @TableField(exist = false)
    private List<SysMenu> children;
}
