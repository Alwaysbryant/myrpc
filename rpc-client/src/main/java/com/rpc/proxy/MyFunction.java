package com.rpc.proxy;

@FunctionalInterface
@SuppressWarnings("unused")
public interface MyFunction<T, P> extends Function<T>{

    Object apply(T t, P p);
}
