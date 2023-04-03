package com.rpc.beat;

import com.rpc.message.RpcRequest;

/**
 * 心跳检测
 */
public final class Beat {
    /**
     * 客户端全部等待空闲时间，30秒
     */
    public static final long CLIENT_IDLE_TIME = 30;
    /**
     *  服务端全部等待空闲时间，三次
     */
    public static final long SERVER_IDLE_TIME = CLIENT_IDLE_TIME * 3;
    /**
     * 心跳检测的请求ID固定
     */
    public static final String REQUEST_ID = "heart-beat-ping";

    /**
     * 心跳检测发送的请求
     */
    public static RpcRequest BEAT_PING;

    static {
        BEAT_PING = new RpcRequest();
        BEAT_PING.setRequestId(REQUEST_ID);
    }
}
