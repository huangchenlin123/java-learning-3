package com.meat.common.aspect;

import com.meat.common.annotation.OperLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 操作日志 AOP 切面
 */
@Slf4j
@Aspect
@Component
public class OperLogAspect {

    @Around("@annotation(com.meat.common.annotation.OperLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperLog operLog = method.getAnnotation(OperLog.class);

        log.info("[操作日志] 模块:{} | 类型:{} | 描述:{} | 方法:{}",
                operLog.module(), operLog.type(), operLog.desc(), method.getName());

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();

        log.info("[操作日志] 模块:{} | 耗时:{}ms", operLog.module(), (endTime - startTime));
        return result;
    }
}
