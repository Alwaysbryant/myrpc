package com.rpc.manager;

import com.rpc.netty.ClientChannelInitializer;
import com.rpc.netty.ClientHandler;
import com.rpc.protocol.Protocol;
import com.rpc.protocol.ServiceInfo;
import com.rpc.utils.ServiceKeyUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * netty客户端
 */
public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    /**
     * 单例模式
     */
    private volatile static ConnectionManager instance;

    private volatile boolean running = true;

    private Map<Protocol, ClientHandler> connectedNodes = new ConcurrentHashMap<>();

    private CopyOnWriteArraySet<Protocol> protocolSet = new CopyOnWriteArraySet<>();

    private final EventLoopGroup workerGroup = new NioEventLoopGroup(4);

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 8, 600, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000));

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    private static final long timeout = 3000;

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        if (instance == null) {
            synchronized (ConnectionManager.class) {
                if (instance == null) {
                    instance = new ConnectionManager();
                }
            }
        }
        return instance;
    }

    /**
     * 更新当前发现的服务
     */
    public void updateService(List<Protocol> serviceList) {
        if (CollectionUtils.isEmpty(serviceList)) {
            // todo
            if (CollectionUtils.isNotEmpty(this.protocolSet)) {
                for (Protocol protocol : protocolSet) {
                    removeNode(protocol);
                }
            }
            return;
        }
        Set<Protocol> set = new HashSet<>(serviceList);
        for (Protocol protocol : set) {
            if (!protocolSet.contains(protocol)) {
                connectNode(protocol);
            }
        }
        // 删除当前所有连接中不存在的
        for (Protocol protocol : protocolSet) {
            if (!set.contains(protocol)) {
                removeNode(protocol);
            }
        }
    }

    public void updateService(Protocol protocol, PathChildrenCacheEvent.Type type) {
        if (Objects.nonNull(protocol)) {
            if (type == PathChildrenCacheEvent.Type.CHILD_ADDED && !protocolSet.contains(protocol)) {
                connectNode(protocol);
            } else if (type == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
                removeNode(protocol);
            } else if (type == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
                removeNode(protocol);
                connectNode(protocol);
            } else {
                throw new RuntimeException("zookeeper type is error");
            }
        }
    }

    public ClientHandler chooseHandler(String serviceKey) {
        int size = connectedNodes.size();
        while (running && size <= 0) {
            try {
                waitHandler();
                size = connectedNodes.size();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Map<String, List<Protocol>> serviceMap = getServiceMap();
        List<Protocol> protocols = serviceMap.get(serviceKey);
        if (CollectionUtils.isEmpty(protocols)) {
            throw new NoSuchElementException();
        }
        return connectedNodes.get(protocols.get(0));
    }

    private Map<String, List<Protocol>> getServiceMap() {
        Map<String, List<Protocol>> map = new HashMap<>();
        if (MapUtils.isNotEmpty(connectedNodes)) {
            for (Protocol protocol : connectedNodes.keySet()) {
                List<ServiceInfo> serviceList = protocol.getServiceList();
                for (ServiceInfo serviceInfo : serviceList) {
                    String key = ServiceKeyUtil.generateKey(serviceInfo.getServiceName(), serviceInfo.getVersion());
                    List<Protocol> protocols = map.get(key);
                    if (protocols == null) {
                        protocols = new ArrayList<>();
                    }
                    protocols.add(protocol);
                    map.putIfAbsent(key, protocols);
                }

            }
        }
        return map;
    }

    private void connectNode(Protocol protocol) {
        if (CollectionUtils.isEmpty(protocol.getServiceList())) {
            logger.error("services is empty");
            return;
        }
        this.protocolSet.add(protocol);
        String host = protocol.getHost();
        int port = protocol.getPort();
        protocol.getServiceList().forEach(item -> logger.info("service connecting..., serviceName:[{}], version[{}]", item.getServiceName(), item.getVersion()));
        final InetSocketAddress address = new InetSocketAddress(host, port);
        executor.submit(() -> {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(this.workerGroup)
                    .handler(new ClientChannelInitializer())
                    .channel(NioSocketChannel.class);

            ChannelFuture future = bootstrap.connect(address);
            future.addListener(channelFuture -> {
                if (channelFuture.isSuccess()) {
                    ClientHandler clientHandler = future.channel().pipeline().get(ClientHandler.class);
                    connectedNodes.put(protocol, clientHandler);
                    clientHandler.setProtocol(protocol);
                    // 唤醒线程
                    signalAllAvailableHandler();
                }
            });

        });


    }

    /**
     * 删除handler
     *
     * @param protocol
     */
    private void removeNode(Protocol protocol) {
        ClientHandler clientHandler = connectedNodes.get(protocol);
        if (clientHandler != null) {
            clientHandler.close();
        }
        connectedNodes.remove(protocol);
        protocolSet.remove(protocol);
    }

    public void removeHandler(Protocol protocol) {
        connectedNodes.remove(protocol);
        protocolSet.remove(protocol);
    }

    private void signalAllAvailableHandler() {
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean waitHandler() throws InterruptedException {
        lock.lock();
        try {
            return condition.await(timeout, TimeUnit.SECONDS);
        } finally {
            lock.unlock();
        }
    }


    public void stop() {
        this.running = false;
        //todo
        for (Protocol protocol : protocolSet) {
            removeNode(protocol);
        }
        signalAllAvailableHandler();
        executor.shutdown();
        workerGroup.shutdownGracefully();
    }


}