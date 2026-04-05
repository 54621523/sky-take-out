package com.sky.filter;

import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

@Slf4j
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
public class DubboInfoFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (RpcContext.getServiceContext().isProviderSide()) {
            String currentId = RpcContext.getServiceContext().getAttachment("currentId");
            if (currentId != null) {
                BaseContext.setCurrentId(Long.valueOf(currentId));
                log.debug("Dubbo服务端设置用户上下文 - currentId: {}", currentId);
            }
        } else {
            Long currentId = BaseContext.getCurrentId();
            if (currentId != null) {
                RpcContext.getServiceContext().setAttachment("currentId", String.valueOf(currentId));
                log.debug("Dubbo客户端传递用户上下文 - currentId: {}", currentId);
            }
        }

        try {
            return invoker.invoke(invocation);
        } finally {
            if (RpcContext.getServiceContext().isProviderSide()) {
                BaseContext.removeCurrentId();
            }
        }
    }
}
