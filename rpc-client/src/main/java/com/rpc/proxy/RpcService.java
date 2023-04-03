package com.rpc.proxy;

import com.rpc.netty.RpcFuture;

public interface RpcService<T, P, FN extends Function<T>>{

    RpcFuture call(String methodName, Object... args) throws Exception;

    RpcFuture call(FN f, Object... args) throws Exception;
}
