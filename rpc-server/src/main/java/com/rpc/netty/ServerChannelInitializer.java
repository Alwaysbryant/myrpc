package com.rpc.netty;

import com.rpc.beat.Beat;
import com.rpc.codec.Decoder;
import com.rpc.codec.Encoder;
import com.rpc.message.RpcRequest;
import com.rpc.message.RpcResponse;
import com.rpc.serializer.jackson.JacksonSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * NettyChannel初始化，添加处理器。
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private Map<String, Object> serviceMap;
    private ThreadPoolExecutor executor;

    public ServerChannelInitializer(Map<String, Object> serviceMap, ThreadPoolExecutor executor) {
        this.serviceMap = serviceMap;
        this.executor = executor;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        JacksonSerializer serializer = JacksonSerializer.class.newInstance();
        ChannelPipeline pipeline = channel.pipeline();
        // 心跳处理器
        pipeline.addLast(new IdleStateHandler(0, 0, Beat.SERVER_IDLE_TIME, TimeUnit.SECONDS));
        // 由于在解码编码过程中，数据由长度和具体的数据内容组成。使用该处理器预先处理
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        pipeline.addLast(new Decoder(RpcRequest.class, serializer));
        pipeline.addLast(new Encoder(RpcResponse.class, serializer));
        pipeline.addLast(new ServerHandler(serviceMap, executor));
    }
}
