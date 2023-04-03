package com.rpc.serializer;

public abstract class Serializer {

    public abstract <T> Object deserialize(byte[] bytes, Class<T> clazz) throws Exception;

    public abstract <T> byte[] serialize(T o) throws Exception;

}
