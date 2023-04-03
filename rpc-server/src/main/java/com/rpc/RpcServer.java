package com.rpc;

import com.rpc.annotation.RpcService;
import com.rpc.netty.NettyServer;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

public class RpcServer extends NettyServer implements ApplicationContextAware, InitializingBean, DisposableBean {


    public RpcServer(String serverAddress, String registerAddress) {
        super(serverAddress, registerAddress);
    }

    @Override
    public void destroy() throws Exception {
        super.stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        Map<String, Object> beanMap = context.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(beanMap)) {
            beanMap.forEach((key, value) -> {
                RpcService annotation = value.getClass().getAnnotation(RpcService.class);
                String serviceName = annotation.value().getName();
                String version = annotation.version();
                super.addService(serviceName, version, value);
            });
        }
    }
}
