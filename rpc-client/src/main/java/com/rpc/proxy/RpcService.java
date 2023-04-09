package com.rpc.proxy;

import com.rpc.netty.RpcFuture;

@SuppressWarnings("unused")
public interface RpcService<T, P, FN extends Function<T>>{
    /**
     * 回调方法
     * @param methodName  回调方法名
     * @param args  参数
     * @return  响应结果
     */
    RpcFuture call(String methodName, Object... args) throws Exception;

    /**
     * 回调方法
     * @param f   函数式接口，
     * @param args 参数
     * @return   响应结果
     */
    RpcFuture call(FN f, Object... args) throws Exception;
}
