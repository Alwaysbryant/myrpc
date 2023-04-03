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

import java.util.concurrent.TimeUnit;

public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        JacksonSerializer serializer = JacksonSerializer.class.newInstance();
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(0, 0, Beat.CLIENT_IDLE_TIME, TimeUnit.SECONDS));
        pipeline.addLast(new Encoder(RpcRequest.class, serializer));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        pipeline.addLast(new Decoder(RpcResponse.class, serializer));
        pipeline.addLast(new ClientHandler());
    }
}
