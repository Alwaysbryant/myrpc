package com.rpc.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

/**
 * zookeeper服务
 */
@SuppressWarnings("unused")
public class CuratorClient {
    private CuratorFramework curator;

    public CuratorClient(String connectString, int sessionTimeout, int connectionTimeout) {
        curator = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(new ExponentialBackoffRetry(1000, 10))
                .sessionTimeoutMs(sessionTimeout)
                .connectionTimeoutMs(connectionTimeout)
                .build();
        // 启动
        curator.start();
    }

    public CuratorClient(String connectString) {
        this(connectString, Constant.SESSION_TIMEOUT, Constant.CONNECTION_TIMEOUT);
    }

    public CuratorClient(String connectString, int timeout) {
        this(connectString, timeout, timeout);
    }

    public CuratorFramework getClient() {
        return this.curator;
    }

    /**
     * 监听节点状态改变
     * @param listener 监听器
     */
    public void addStateChangedListener(ConnectionStateListener listener) {
        this.curator.getConnectionStateListenable().addListener(listener);
    }

    /**
     * 创建节点
     * 创建临时节点，即断开后就删除
     * @param path      路径
     * @param serialize znode数据
     */
    public String createData(String path, byte[] serialize) throws Exception{
        return this.curator.create()
                .creatingParentContainersIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path, serialize);
    }

    /**
     * 删除znode
     * @param path 路径
     */
    public void deletePath(String path) throws Exception{
        this.curator.delete().forPath(path);
    }

    /**
     * 添加子节点监听
     * @param path 路径
     * @param listener 监听器对象
     */
    public void watchChildrenNode(String path, PathChildrenCacheListener listener) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curator, path, true);
        //BUILD_INITIAL_CACHE 代表使用同步的方式进行缓存初始化。
        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        pathChildrenCache.getListenable().addListener(listener);
    }

    /**
     * 获取节点数据
     * @param path 路径
     * @return 数据字节流
     */
    public byte[] getData(String path) throws Exception{
        return this.curator.getData().forPath(path);
    }

    /**
     * 获取子节点路径集合
     * @param rootPath  根路径
     */
    public List<String> getChildrenList(String rootPath) throws Exception{
        return this.curator.getChildren().forPath(rootPath);
    }

    public void stop() {
        this.curator.close();
    }
}
