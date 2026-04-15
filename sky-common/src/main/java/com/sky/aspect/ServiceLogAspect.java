package com.sky.aspect;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.sky.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Aspect
@Component
public class ServiceLogAspect {

    private static final String TRACE_ID_KEY = "traceId";

    private static final int MAX_LOG_LENGTH = 2048;

    @Around("execution(* com.sky.*.service.impl.*.*(..))")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        //直接获取traceId，无论上游是谁
        String traceId = MDC.get(TRACE_ID_KEY);

        if (StrUtil.isBlank(traceId)) {
            //单元测试
            traceId = "test";
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();


        String requestParams = "[]";
        try {
            Object[] args = joinPoint.getArgs();
            List<Object> safeArgs = new ArrayList<>();
            for (Object arg : args) {
                if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                    safeArgs.add("{" + arg.getClass().getSimpleName() + " 已过滤}");
                } else {
                    safeArgs.add(arg);
                }
            }
            String jsonStr = JSON.toJSONString(safeArgs);
            requestParams = truncate(jsonStr);
        } catch (Exception e) {
            requestParams = "解析入参失败: " + e.getMessage();
        }

        log.info("[入口参数] [{}] 请求入参 -> {}", methodName, requestParams);

        Object result = null;
        try {
            result = joinPoint.proceed();
            String responseParams = "null";
            try {
                String jsonStr = JSON.toJSONString(result);
                responseParams = truncate(jsonStr);
            } catch (Exception e) {
                responseParams = "解析出参失败: " + e.getMessage();
            }

            log.info("[返回结果] [{}] 响应结果 -> {}", methodName, responseParams);

            long costTime = System.currentTimeMillis() - startTime;
            if (costTime > 2000) {
                log.warn("[性能监控] [{}] 接口响应缓慢，耗时: {} ms", methodName, costTime);
            } else {
                log.info("[性能监控] [{}] 接口执行完成，耗时: {} ms", methodName, costTime);
            }

            return result;

        } catch (Throwable e) {
            if (e instanceof BaseException) {
                log.warn("[业务异常] {} {}", joinPoint.getSignature(), e.getMessage());
            } else {
                log.error("[系统异常] {}", joinPoint.getSignature(), e);
            }
            throw e;
        }
    }

    /**
     * 截断超长字符串
     */
    private String truncate(String str) {
        if (str == null) return "null";
        if (str.length() > MAX_LOG_LENGTH) {
            return str.substring(0, MAX_LOG_LENGTH) + "... (已截断)";
        }
        return str;
    }
}