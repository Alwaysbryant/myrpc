package com.rpc.test.service;

import com.rpc.annotation.RpcReference;

public class ConsumerServiceImpl implements ConsumerService{
    @RpcReference(version = "1.1.1")
    private HelloService service;

    @Override
    public String reply(String content) {
        return service.hello(content);
    }

    @Override
    public Dto getDto(String name, Integer age, String job) {
        return service.remoteForDto(name, age, job);
    }
}
