package com.rpc.discovery;

import com.rpc.manager.ConnectionManager;
import com.rpc.protocol.Protocol;
import com.rpc.utils.Json;
import com.rpc.zookeeper.Constant;
import com.rpc.zookeeper.CuratorClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 发现服务
 */
public class ServiceDiscovery {
    private final static Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CuratorClient client;

    public ServiceDiscovery(String address) {
        this.client = new CuratorClient(address);
        this.discovery();
    }

    private void discovery() {
        // 初始化
        this.init();
        try {
            this.client.watchChildrenNode(Constant.ROOT_PATH, (client, event) -> {
                PathChildrenCacheEvent.Type type = event.getType();
                ChildData data = event.getData();
                switch (type) {
                    case CONNECTION_RECONNECTED:
                        this.init();
                        break;
                    case CHILD_ADDED:
                    case CHILD_UPDATED:
                    case CHILD_REMOVED:
                        this.updateServer(data, type);
                        break;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        try {
            List<String> childrenList = this.client.getChildrenList(Constant.ROOT_PATH);
            if (CollectionUtils.isEmpty(childrenList)) {
                logger.warn("services is empty that is register");
                return;
            }
            List<Protocol> data = new ArrayList<>();
            for (String path : childrenList) {
                // 获取节点数据
                byte[] bytes = this.client.getData(Constant.ROOT_PATH + "/" + path);
                Protocol protocol = Json.parseObject(bytes, Protocol.class);
                data.add(protocol);
            }
            updateServer(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateServer(ChildData data, PathChildrenCacheEvent.Type type) {
        String path = data.getPath();
        byte[] bytes = data.getData();
        logger.info("update node, path is {}", path);
        Protocol protocol = Json.parseObject(bytes, Protocol.class);
        this.updateServer(protocol, type);
    }

    private void updateServer(Protocol protocol, PathChildrenCacheEvent.Type type) {
        ConnectionManager.getInstance().updateService(protocol, type);
    }

    private void updateServer(List<Protocol> list) {
        ConnectionManager.getInstance().updateService(list);
    }

    public void stop() {
        client.stop();
    }

}
