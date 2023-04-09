package com.rpc.proxy;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

/**
 * 用来异步获取结果
 * 继承Serializable 默认方法实现获取当前方法名
 * @param <T>
 */
@SuppressWarnings("unused")
public interface Function<T> extends Serializable {
    default String getMethodName() throws Exception{
        Method method = this.getClass().getMethod("writeReplace");
        method.setAccessible(true);
        SerializedLambda invoke = (SerializedLambda) method.invoke(this);
        return invoke.getImplMethodName();
    }

}
