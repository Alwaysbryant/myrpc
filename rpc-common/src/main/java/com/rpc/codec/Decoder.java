package com.rpc.codec;


import com.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * netty数据传输，解码处理器
 */
public class Decoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(Decoder.class);

    private final Class<?> clazz;
    private final Serializer serializer;

    public Decoder(Class<?> clazz, Serializer serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int bytes = byteBuf.readableBytes();
        if (bytes < 4) {
            logger.info("message is empty");
            return;
        }
        byteBuf.markReaderIndex();
        int dataLen = byteBuf.readInt();
        if (bytes < dataLen) {
            byteBuf.resetReaderIndex();
            return;
        }
        // 反序列化
        byte[] data = new byte[dataLen];
        byteBuf.readBytes(data);
        Object o = null;
        try {
            o = serializer.deserialize(data, this.clazz);
        } catch (IOException e) {
            logger.error("deserialize {} error, cause by {}", clazz, e.getCause());
            e.printStackTrace();
        }
        list.add(o);
    }
}
