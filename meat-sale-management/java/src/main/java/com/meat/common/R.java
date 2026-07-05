package com.meat.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果封装
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private Integer code;

    /** 提示信息 */
    private String msg;

    /** 数据 */
    private T data;

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.msg = "操作成功";
        r.data = data;
        return r;
    }

    public static <T> R<T> ok(String msg, T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.msg = msg;
        r.data = data;
        return r;
    }

    public static <T> R<T> fail() {
        return fail("操作失败");
    }

    public static <T> R<T> fail(String msg) {
        return fail(500, msg);
    }

    public static <T> R<T> fail(Integer code, String msg) {
        R<T> r = new R<>();
        r.code = code;
        r.msg = msg;
        r.data = null;
        return r;
    }

    public static <T> R<T> fail(Integer code, String msg, T data) {
        R<T> r = new R<>();
        r.code = code;
        r.msg = msg;
        r.data = data;
        return r;
    }
}
