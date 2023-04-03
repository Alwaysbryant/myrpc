package com.rpc.threadpool;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CustomThreadPool {

    public static ThreadPoolExecutor createPool(String name, int maxPoolSize, int corePoolSize) {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>()
                , r -> new Thread(r, "rpc" + name)
                , new ThreadPoolExecutor.AbortPolicy());
    }
}
