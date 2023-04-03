package com.rpc.test.service;

import com.rpc.annotation.RpcService;

@RpcService(value = HelloService.class, version = "1.1.1")
public class HelloServiceImpl implements HelloService{

    public HelloServiceImpl() {
    }

    @Override
    public String hello(String content) {
        System.out.println("hello," + content);
        return "hello" + content;
    }

    @Override
    public Dto remoteForDto(String name, Integer age, String job) {
        return new Dto(name, age, job);
    }
}
