package cn.procsl.ping.boot.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class SimpleByteArrayChannelTest {

    @Test
    public void append() throws IOException {


        SimpleByteArrayChannel simple = new SimpleByteArrayChannel(10);

        simple.write(ByteBuffer.wrap("hello".getBytes()));
        simple.write(ByteBuffer.wrap("hello".getBytes()));
        simple.write(ByteBuffer.wrap("hello".getBytes()));
        simple.write(ByteBuffer.wrap("hello".getBytes()));
        simple.write(ByteBuffer.wrap("hello".getBytes()));


        log.info("{}", simple);
    }

    @Test
    public void dequeue() {
    }
}