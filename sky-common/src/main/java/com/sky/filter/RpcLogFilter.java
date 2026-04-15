package com.sky.filter;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.sky.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
public class RpcLogFilter implements Filter {

    private static final String TRACE_ID_KEY = "traceId";
    private static final int MAX_LOG_LENGTH = 1024;


    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String traceId;
        RpcContext rpcContext = RpcContext.getServiceContext();

        // --- 1. TraceID 处理
        if (rpcContext.isProviderSide()) {
            traceId = invocation.getAttachment(TRACE_ID_KEY);
            if (StrUtil.isBlank(traceId)) {
                traceId = "test";
            }
            MDC.put(TRACE_ID_KEY, traceId);
        } else {
            traceId = MDC.get(TRACE_ID_KEY);
            if (StrUtil.isBlank(traceId)) {
                traceId = "test";
            }
            invocation.setAttachment(TRACE_ID_KEY, traceId);
        }

        String serviceName = invoker.getInterface().getSimpleName();
        String methodName = invocation.getMethodName();

        try {
            //2.消费者打印入参
            if (!rpcContext.isProviderSide()) {
                try {
                    Object[] arguments = invocation.getArguments();
                    String argsJson = JSON.toJSONString(arguments);
                    log.info("[RPC调用] {}.{} 请求参数 -> {}", serviceName, methodName, truncate(argsJson));
                } catch (Exception e) {
                    log.warn("[RPC调用] {}.{} 参数序列化失败: {}", serviceName, methodName, e.getMessage());
                }
            }

            // --- 3. 执行调用 ---
            long startTime = System.currentTimeMillis();
            Result result = invoker.invoke(invocation);
            long costTime = System.currentTimeMillis() - startTime;

            // --- 4. 结果与异常处理 ---
                // 检查是否有异常 (Dubbo 会把异常封装在 Result 里，而不是直接抛出)
                if (result.hasException()) {
                    Throwable exception = result.getException();
                    //消费者打印错误日志
                    if (!rpcContext.isProviderSide()) {
                        if (exception instanceof BaseException) {
                            // 业务异常：打印消息，不打印堆栈
                            log.warn("[业务异常] {}.{} 调用异常 -> {} | 耗时: {}ms",
                                    serviceName, methodName, exception.getMessage(), costTime);
                        } else {
                            // 系统异常：打印堆栈
                            log.error("[系统异常] {}.{} 调用异常 -> {} | 耗时: {}ms",
                                    serviceName, methodName, exception.getMessage(), costTime, exception);
                        }
                    }

                } else {
                    // 没有异常，正常打印日志
                    try {
                        Object value = result.getValue();
                        String resultJson = JSON.toJSONString(value);
                        if (rpcContext.isProviderSide()) {
                            log.info("[RPC响应] {}.{} 响应结果 -> {} | 耗时: {}ms",
                                    serviceName, methodName, truncate(resultJson), costTime);
                        } else {
                            log.info("[RPC返回] {}.{} 返回结果 -> {} | 耗时: {}ms",
                                    serviceName, methodName, truncate(resultJson), costTime);
                        }
                    } catch (Exception e) {
                        log.warn("[RPC响应] {}.{} 结果序列化失败: {}", serviceName, methodName, e.getMessage());
                    }
                }

            return result;

        } catch (RpcException e) {
            //
            log.error("[RPC异常] {}.{} 调用异常 -> {}", serviceName, methodName, e.getMessage(), e);
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