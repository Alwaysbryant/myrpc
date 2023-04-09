package com.rpc.proxy;

import com.rpc.manager.ConnectionManager;
import com.rpc.message.RpcRequest;
import com.rpc.netty.ClientHandler;
import com.rpc.netty.RpcFuture;
import com.rpc.utils.ServiceKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class ObjectProxy<T, P> implements InvocationHandler, RpcService<T, P, Function<T>>  {
    private final static Logger logger = LoggerFactory.getLogger(ObjectProxy.class);

    private final Class<T> clazz;
    private final String version;

    public ObjectProxy(Class<T> clazz, String version) {
        this.clazz = clazz;
        this.version = version;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            String name = method.getName();
            switch (name) {
                case "equals":
                    return proxy == args[0];
                case "toString":
                    return proxy.getClass().getName() + "@" +
                            Integer.toHexString(System.identityHashCode(proxy)) +
                            ", with InvocationHandler " + this;
                case "hashCode":
                    return proxy.hashCode();
                default:
                    throw new IllegalStateException(String.valueOf(method));
            }
        }
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setMethodName(method.getName());
        request.setVersion(version);
        request.setClassName(method.getDeclaringClass().getName());
        request.setParameterizedTypes(method.getParameterTypes());
        request.setParams(args);
        logger.info("ready for send request to server, requestId is {}", request.getRequestId());
        ClientHandler clientHandler = ConnectionManager.getInstance().chooseHandler(ServiceKeyUtil.generateKey(method.getDeclaringClass().getName(), version));

        RpcFuture future = clientHandler.sendRequest(request);

        return future.get();
    }


    @Override
    public RpcFuture call(String methodName, Object... args) {
        String key = ServiceKeyUtil.generateKey(this.clazz.getSimpleName(), version);
        RpcRequest request = this.createRequest(this.clazz.getName(), methodName, args);
        ClientHandler clientHandler = ConnectionManager.getInstance().chooseHandler(key);
        return clientHandler.sendRequest(request);
    }

    @Override
    public RpcFuture call(Function<T> f, Object... args) throws Exception {
        String key = ServiceKeyUtil.generateKey(this.clazz.getName(), version);
        RpcRequest request = this.createRequest(this.clazz.getName(), f.getMethodName(), args);
        ClientHandler clientHandler = ConnectionManager.getInstance().chooseHandler(key);
        return clientHandler.sendRequest(request);
    }

    private RpcRequest createRequest(String className, String methodName, Object... args) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setParams(args);
        request.setVersion(this.version);
        request.setClassName(className);
        request.setMethodName(methodName);
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = this.getType(args[i]);
        }
        request.setParameterizedTypes(types);
        return request;
    }

    private Class<?> getType(Object o) {
        return o.getClass();
    }
}
