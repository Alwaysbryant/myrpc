package com.rpc.netty;

public abstract class Server {
    /**
     * 开启服务
     * 使用线程池来创建连接。创建一个Netty连接，作为服务器启动
     */
    public abstract void start() throws Exception;

    /**
     * 停止服务
     * 维持Netty连接的线程不存在/Thread.isAlive==Boolean.False，则中断线程
     */
    public abstract void stop() throws Exception;
}
