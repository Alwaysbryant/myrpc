package com.rpc.serializer;

public abstract class Serializer {

    /**
     * 用于Netty-handler处理解码
     * @param bytes  二进制数据
     * @param clazz  解码对应class
     * @param <T>   泛型
     * @return      解码后对象
     * @throws Exception  二进制数据为空，会抛NPE
     */
    public abstract <T> Object deserialize(byte[] bytes, Class<T> clazz) throws Exception;

    /**
     * 用于Netty-handler处理编码
     * @param o    对象
     * @param <T>  泛型
     * @return      二进制数据
     * @throws Exception   对象为空会抛出NPE
     */
    public abstract <T> byte[] serialize(T o) throws Exception;

}
