package cn.procsl.ping.boot.common.utils;

import java.io.IOException;

public interface BufferListener {

    default void onPreWrite(ByteArrayBuffer buffer, int len) throws IOException {

    }

    default void onAfterWrite(ByteArrayBuffer buffer, int len) throws IOException {

    }

    default void onPreRead(ByteArrayBuffer buffer, int len) throws IOException {

    }

    default void onAfterRead(ByteArrayBuffer buffer, int len, int actualLength) throws IOException {

    }

}
