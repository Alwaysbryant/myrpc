package com.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记服务为rpc服务的注解
 * 被标记该注解的类将会成为rpc调用的被调用方，即服务的提供者。
 * 需要将含有该注解的类/服务注册到注册中心中
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {
    /**
     * 版本
     * @return 服务版本号
     */
    String version() default "1.0";

    /**
     * 类
     * @return 返回类
     */
    Class<?> value();

}
