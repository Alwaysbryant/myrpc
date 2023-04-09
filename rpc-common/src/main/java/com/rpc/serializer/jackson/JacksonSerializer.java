package com.rpc.serializer.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rpc.serializer.Serializer;

/**
 * 使用jackson进行序列化和反序列化
 */
public class JacksonSerializer extends Serializer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 如果为空则不输出
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        // 对于空的对象转json的时候不抛出错误
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 禁用序列化日期为timestamps
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 禁用遇到未知属性抛出异常
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) throws Exception{
        if (bytes == null || bytes.length == 0) {
            throw new NullPointerException("deserializer error, the byte array is empty");
        }
        return OBJECT_MAPPER.readValue(bytes, clazz);
    }

    @Override
    public <T> byte[] serialize(T o) throws Exception {
        if (o == null) {
            throw new NullPointerException("object is null that is serialized");
        }
        return OBJECT_MAPPER.writeValueAsBytes(o);

    }
}
