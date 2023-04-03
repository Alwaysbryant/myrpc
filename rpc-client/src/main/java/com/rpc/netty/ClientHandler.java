package com.rpc.netty;

import com.rpc.beat.Beat;
import com.rpc.manager.ConnectionManager;
import com.rpc.message.RpcRequest;
import com.rpc.message.RpcResponse;
import com.rpc.protocol.Protocol;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private volatile Channel channel;
    private Protocol protocol;
    private SocketAddress remoteAddress;

    private ConcurrentHashMap<String, RpcFuture> pendingRPC = new ConcurrentHashMap<>();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remoteAddress = this.channel.remoteAddress();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            sendRequest(Beat.BEAT_PING);
            logger.info("netty client send heart beat");
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ConnectionManager.getInstance().removeHandler(protocol);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        String requestId = response.getRequestId();
        logger.info("netty client receive response id {}", requestId);
        RpcFuture future = pendingRPC.get(requestId);
        if (future == null) {
            logger.error("netty client receive response is null");
        } else {
            pendingRPC.remove(future);
            future.done(response);
        }
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public RpcFuture sendRequest(RpcRequest request) {
        RpcFuture future = new RpcFuture(request);
        // todo
        pendingRPC.put(request.getRequestId(), future);
        try {
            ChannelFuture sync = this.channel.writeAndFlush(request).sync();
            if (!sync.isSuccess()) {
                logger.error("write request to server error");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return future;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("netty client caught exception", cause.getCause());
        this.close();
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

}
