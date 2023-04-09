package com.rpc.netty;

import com.rpc.registery.ServiceRegister;
import com.rpc.threadpool.CustomThreadPool;
import com.rpc.utils.ServiceKeyUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class NettyServer extends Server {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    /**
     * 当前连接的线程
     */
    private Thread thread;
    /**
     * 连接address
     */
    private final String address;
    /**
     * 服务注册
     */
    private ServiceRegister register;
    /**
     * 保存所有含有{@link com.rpc.annotation.RpcService} 的服务
     */
    private final Map<String, Object> serviceMap;

    public NettyServer(String serverAddress, String registerAddress) {
        this.address = serverAddress;
        register = new ServiceRegister(registerAddress);
        serviceMap = new HashMap<>();
    }

    public void addService(String name, String version, Object bean) {
        String key = ServiceKeyUtil.generateKey(name, version);
        serviceMap.put(key, bean);
    }

    @Override
    public void start() {
        this.thread = new Thread(new Runnable() {
            // 创建线程池，用于服务端异步处理请求
            final ThreadPoolExecutor executor
                    = CustomThreadPool.createPool(this.getClass().getSimpleName(), 32, 16);

            @Override
            public void run() {
                NioEventLoopGroup bossGroup = new NioEventLoopGroup();
                NioEventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                            .childHandler(new ServerChannelInitializer(serviceMap, executor));
                    String[] addressArr = address.split(":");
                    String host = addressArr[0];
                    int port = Integer.parseInt(addressArr[1]);
                    ChannelFuture future = bootstrap.bind(host, port).sync();
                    logger.info("server start successful in {}", address);
                    // 进行服务注册
                    if (register != null) {
                        register.registerService(host, port, serviceMap);
                    }
                    future.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // 删除服务
                    assert register != null;
                    register.unregisterService();
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                }
            }
        });
        // 开启线程
        this.thread.start();
    }

    @Override
    public void stop() {
        if (thread != null || thread.isAlive()) {
            this.thread.interrupt();
        }
    }
}
