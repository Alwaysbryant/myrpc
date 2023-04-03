package com.rpc;

import com.rpc.annotation.RpcReference;
import com.rpc.discovery.ServiceDiscovery;
import com.rpc.manager.ConnectionManager;
import com.rpc.proxy.ObjectProxy;
import com.rpc.threadpool.CustomThreadPool;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.ThreadPoolExecutor;

public class RpcClient implements ApplicationContextAware, DisposableBean {
    private ServiceDiscovery discovery;

    private static ThreadPoolExecutor executor = CustomThreadPool.createPool(RpcClient.class.getSimpleName(), 16, 8);

    public RpcClient(String address) {
        this.discovery = new ServiceDiscovery(address);
    }

    public static <T, P> T createService(Class<T> tClass, String version) {
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass}, new ObjectProxy<T, P>(tClass, version));
    }

    @Override
    public void destroy() throws Exception {
        this.stop();
    }

    public static void submit(Runnable r) {
        executor.submit(r);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = context.getBean(beanName);
            Field[] fields = bean.getClass().getFields();
            try {
                for (Field field : fields) {
                    RpcReference annotation = field.getAnnotation(RpcReference.class);
                    if (annotation != null) {
                        field.setAccessible(Boolean.TRUE);
                        String version = annotation.version();
                        field.set(bean, createService(field.getType(), version));
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        executor.shutdown();
        discovery.stop();
        ConnectionManager.getInstance().stop();
    }
}
