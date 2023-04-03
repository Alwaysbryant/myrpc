package com.rpc.test.service;

public interface ConsumerService {

    String reply(String content);

    Dto getDto(String name, Integer age, String job);
}
