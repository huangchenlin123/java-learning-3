package com.meat.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.meat.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public R<Void> handleNotLoginException(NotLoginException e) {
        return R.fail(401, "未登录，请先登录");
    }

    @ExceptionHandler(NotPermissionException.class)
    public R<Void> handleNotPermissionException(NotPermissionException e) {
        return R.fail(403, "权限不足");
    }

    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return R.fail(400, msg);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public R<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("数据重复: ", e);
        String msg = e.getMessage();
        if (msg != null && msg.contains("Duplicate entry")) {
            return R.fail("数据已存在，请勿重复提交");
        }
        return R.fail("数据重复: " + (msg != null ? msg.substring(0, Math.min(100, msg.length())) : ""));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public R<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("数据完整性异常: ", e);
        String msg = e.getMessage();
        if (msg != null) {
            if (msg.contains("cannot be null") || msg.contains("Column") && msg.contains("null")) {
                return R.fail("必填字段未填写，请检查输入");
            }
        }
        return R.fail("数据异常: " + (msg != null ? msg.substring(0, Math.min(100, msg.length())) : ""));
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        // 返回具体错误信息（方便调试）
        String detail = e.getMessage();
        if (detail == null) detail = e.getClass().getSimpleName();
        return R.fail("系统异常: " + detail);
    }
}
