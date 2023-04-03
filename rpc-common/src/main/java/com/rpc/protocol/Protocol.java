package com.rpc.protocol;

import java.io.Serializable;
import java.util.List;

/**
 * 服务
 */
public class Protocol implements Serializable {
    private static final long serialVersionUID = 7647934262786660541L;

    private String host;
    private int port;
    private List<ServiceInfo> serviceList;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<ServiceInfo> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<ServiceInfo> serviceList) {
        this.serviceList = serviceList;
    }
}
