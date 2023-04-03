package com.rpc.registery;

import com.rpc.protocol.Protocol;
import com.rpc.protocol.ServiceInfo;
import com.rpc.serializer.jackson.JacksonSerializer;
import com.rpc.utils.ServiceKeyUtil;
import com.rpc.zookeeper.Constant;
import com.rpc.zookeeper.CuratorClient;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;

public class ServiceRegister {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegister.class);

    private CuratorClient client;
    private List<String> paths = new ArrayList<>();

    public ServiceRegister(String address) {
        this.client = new CuratorClient(address);
    }

    /**
     * 服务注册
     * @param host  ip
     * @param port  端口
     * @param map   需要注册的服务map
     */
    public void registerService(String host, int port, Map<String, Object> map) {
        Assert.notNull(host, "host can not be null");
        // 拆分Key, 保存服务信息
        Set<String> keys = map.keySet();
        List<ServiceInfo> list = new ArrayList<>();
        for (String key : keys) {
            String[] generateKey = key.split(ServiceKeyUtil.CHAR);
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setServiceName(generateKey[0]);
            if (generateKey.length == 2) {
                serviceInfo.setVersion(generateKey[1]);
            }
            list.add(serviceInfo);
        }
        // 生成protocol创建zookeeper节点
        try {
            Protocol protocol = new Protocol();
            protocol.setHost(host);
            protocol.setPort(port);
            protocol.setServiceList(list);
            byte[] serialize = new JacksonSerializer().serialize(protocol);
            // 保证唯一
            String path = Constant.DATA_PATH + "-" + protocol.hashCode();
            path = this.client.createData(path, serialize);
            logger.info("register service successful, path is [{}]", path);
            this.paths.add(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 重新连接注册服务
        client.addStateChangedListener((curator, state) -> {
            if (ConnectionState.RECONNECTED == state) {
                registerService(host, port, map);
            }
        });

    }

    /**
     * 服务注销
     */
    public void unregisterService() {
        try {
            for (String path : paths) {
                this.client.deletePath(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
