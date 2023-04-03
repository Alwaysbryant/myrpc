package com.rpc.proxy;

@FunctionalInterface
public interface MyFunction<T, P> extends Function<T>{

    Object apply(T t, P p);
}
