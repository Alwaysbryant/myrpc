package com.rpc.zookeeper;

/**
 * 定义zookeeper的路径
 */
public interface Constant {
    int SESSION_TIMEOUT = 50000;
    int CONNECTION_TIMEOUT = 50000;

    String ROOT_PATH = "/register";

    String DATA_PATH = ROOT_PATH + "/data";



}
