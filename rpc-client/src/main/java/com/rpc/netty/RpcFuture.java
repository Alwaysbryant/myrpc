package com.rpc.netty;

import com.rpc.RpcClient;
import com.rpc.message.RpcRequest;
import com.rpc.message.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 通过反射{@link com.rpc.proxy.ObjectProxy} 调用方法时，
 * 向服务端发送请求后，需要等待服务端响应，客户端处理器将response保存起来后，tryRelease将锁状态改为1
 * 而在{@link com.rpc.proxy.ObjectProxy#invoke(Object, Method, Object[])}从future获取数据时，需要等上一步骤释放锁
 */
public class RpcFuture implements Future<Object> {
    private static final Logger logger = LoggerFactory.getLogger(RpcFuture.class);

    private Sync sync;
    private RpcRequest request;
    private RpcResponse response;
    private long startTime;
    private long responseTimeThreshold = 5000;
    private List<AsyncRpcCallback> callbacks = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();

    public RpcFuture(RpcRequest request) {
        this.sync = new Sync();
        this.request = request;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(1);
        if (response != null) {
            return response.getResult();
        }
        return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean acquired = sync.tryAcquireNanos(1, unit.toNanos(timeout));
        if (acquired) {
            if (response != null) {
                return response.getResult();
            } else {
                return null;
            }
        }
        throw new RuntimeException("get result timeout, requestId: " + request.getRequestId());
    }

    public void done(RpcResponse response) {
        this.response = response;
        sync.release(1);
        invokeCallbacks();
        long spent = System.currentTimeMillis() - startTime;
        if (spent > responseTimeThreshold) {
            logger.warn("service response is slow, cost {}, requestId: {}", spent,response.getRequestId());
        }
    }

    public void invokeCallbacks() {
        lock.lock();
        try {
            for (final AsyncRpcCallback callback : callbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }

    public RpcFuture addCallback(AsyncRpcCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                callbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    private void runCallback(final AsyncRpcCallback callback) {
        final RpcResponse res = response;
        RpcClient.submit(() -> {
            if (res.isSuccess()) {
                callback.success(res.getResult());
            } else {
                callback.fail(new RuntimeException("response error", new Throwable(res.getErrMsg())));
            }
        });
    }

    static class Sync extends AbstractQueuedSynchronizer {
        //future status
        private final int done = 1;
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == done;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == pending) {
                if (compareAndSetState(pending, done)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        protected boolean isDone() {
            return getState() == done;
        }
    }
}
