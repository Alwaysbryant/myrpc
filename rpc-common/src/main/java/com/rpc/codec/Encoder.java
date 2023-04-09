package com.rpc.codec;

import com.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * netty编码处理器
 */
public class Encoder extends MessageToByteEncoder<Object> {
    private static final Logger logger = LoggerFactory.getLogger(Encoder.class);

    private final Serializer serializer;
    private final Class<?> clazz;

    public Encoder(Class<?> clazz, Serializer serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) {
        // 非空判断
        if (o == null) {
            logger.error("encoder object is null");
            return;
        }
        // 对象类型校验
        if (this.clazz.isInstance(o)) {
            try {
                byte[] bytes = this.serializer.serialize(o);
                byteBuf.writeInt(bytes.length);
                byteBuf.writeBytes(bytes);
            } catch (Exception e) {
                logger.error("encode error, cause by {}", e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
