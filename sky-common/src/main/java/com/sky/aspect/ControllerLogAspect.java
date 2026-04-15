package com.sky.aspect;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Aspect
@Component
public class ControllerLogAspect {

    private static final String TRACE_ID_KEY = "traceId";


    private static final int MAX_LOG_LENGTH = 2048;

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put("traceId", traceId);

        HttpServletRequest request = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                request = attributes.getRequest();
            }
        } catch (Exception e) {
            // 忽略
        }

        long startTime = System.currentTimeMillis();
        String uri = request != null ? request.getRequestURI() : "unknown";
        String httpMethod = request != null ? request.getMethod() : "unknown";


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

        log.info("[HTTP请求] {} {} | 入参 -> {}", httpMethod, uri, requestParams);

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

            log.info("[HTTP响应] {} {} | 出参 -> {}", httpMethod, uri, responseParams);

            long costTime = System.currentTimeMillis() - startTime;
            log.info("[性能监控] {} {} | 耗时: {} ms", httpMethod, uri, costTime);

            return result;

        } catch (Throwable e) {
            if (e instanceof BaseException) {
                log.warn("[业务异常] {} {} | 耗时: {} ms", uri, e.getMessage(), System.currentTimeMillis() - startTime);
                return Result.error(e.getMessage());
            } else {
                log.error("[系统异常] {} {}", uri, joinPoint.getSignature(), e);
                throw e;
            }
        } finally {
            MDC.clear();
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