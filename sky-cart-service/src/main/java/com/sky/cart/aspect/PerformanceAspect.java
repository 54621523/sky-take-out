package com.sky.cart.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PerformanceAspect {

    private static final String TARGET_CLASS_KEY = "targetClass";
    private static final String TARGET_METHOD_KEY = "targetMethod";

    @Around("execution(* com.sky.cart.service.impl..*(..))")
    public Object logPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        MDC.put(TARGET_CLASS_KEY, className);
        MDC.put(TARGET_METHOD_KEY, methodName);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.info("耗时: {}ms", duration);

            if (duration > 300) {
                log.warn("⚠️ 慢方法 | 耗时: {}ms", duration);
            }

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("!!! 方法异常 | 耗时: {}ms | 错误: {}",
                    duration, e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove(TARGET_CLASS_KEY);
            MDC.remove(TARGET_METHOD_KEY);
        }
    }
}
