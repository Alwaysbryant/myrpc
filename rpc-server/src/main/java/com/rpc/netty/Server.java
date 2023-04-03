package com.rpc.netty;

public abstract class Server {
    /**
     * 开启服务
     */
    public abstract void start() throws Exception;

    /**
     * 停止服务
     */
    public abstract void stop() throws Exception;
}
