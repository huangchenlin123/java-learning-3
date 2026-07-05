package com.meat.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解 — 用于标记需要数据权限过滤的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /** 表别名（SQL中的表别名，用于拼接 WHERE 条件） */
    String tableAlias() default "";

    /** 表中仓库ID字段名 */
    String columnName() default "warehouse_id";
}
