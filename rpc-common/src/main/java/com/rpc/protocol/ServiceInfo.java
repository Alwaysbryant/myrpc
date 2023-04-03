package com.rpc.protocol;

import java.io.Serializable;

/**
 * 服务注册-服务信息
 */
public class ServiceInfo implements Serializable {
    private static final long serialVersionUID = 1756660456785900860L;

    private String serviceName;
    private String version;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
