package com.rpc.test.service;

public interface HelloService {
    String hello(String content);

    Dto remoteForDto(String name, Integer age, String job);
}
