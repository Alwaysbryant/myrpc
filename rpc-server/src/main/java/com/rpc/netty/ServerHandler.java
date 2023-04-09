package com.rpc.netty;

import com.rpc.beat.Beat;
import com.rpc.message.RpcRequest;
import com.rpc.message.RpcResponse;
import com.rpc.utils.ServiceKeyUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Netty服务端自定义处理器
 */
public class ServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private final Map<String, Object> serviceMap;
    private final ThreadPoolExecutor executor;

    public ServerHandler(Map<String, Object> serviceMap, ThreadPoolExecutor executor) {
        this.serviceMap = serviceMap;
        this.executor = executor;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcRequest request) {
        // 如果是心跳检测，直接返回
        if (Beat.REQUEST_ID.equalsIgnoreCase(request.getRequestId())) {
            logger.info("channel is heart beat");
            return;
        }
        this.executor.execute(() -> {
            logger.info("current requestId is [{}]", request.getRequestId());
            RpcResponse response = new RpcResponse();
            response.setRequestId(request.getRequestId());
            reference(response, request);

            // 写入
            ctx.writeAndFlush(response).addListener(
                    (ChannelFutureListener) future ->
                            logger.info("server send response to client, requestId [{}]", request.getRequestId()));
        });
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
            logger.debug("the channel idle {} seconds", Beat.SERVER_IDLE_TIME);
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("the channel is error, cause by {}", cause.getMessage());
        super.exceptionCaught(ctx, cause);
    }

    /**
     * 发起调用
     * @param response 封装的响应
     * @param request  需要解析的请求
     */
    private void reference(RpcResponse response, RpcRequest request) {
        String className = request.getClassName();
        Object[] params = request.getParams();
        Class<?>[] types = request.getParameterizedTypes();
        String version = request.getVersion();
        // 按照规则生成key
        String serviceKey = ServiceKeyUtil.generateKey(className, version);
        Object bean = serviceMap.get(serviceKey);
        if (bean == null) {
            logger.error("service is not exists");
            throw new NullPointerException("bean is null");
        }
        String methodName = request.getMethodName();
        Class<?> aClass = bean.getClass();
        try {
            Method method = aClass.getMethod(methodName, types);
            method.setAccessible(Boolean.TRUE);
            Object result = method.invoke(bean, params);
            logger.info("invoke method successful, result is {}", result);
            response.setResult(result);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("invoke method error, message {}", e.getMessage());
            response.setErrMsg(e.getMessage());
            e.printStackTrace();
        }
    }
}
