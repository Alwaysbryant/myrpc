package com.rpc.netty;

public interface AsyncRpcCallback {
    void fail(Exception e);

    void success(Object result);
}
