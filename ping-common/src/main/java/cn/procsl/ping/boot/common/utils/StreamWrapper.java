package cn.procsl.ping.boot.common.utils;

import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import java.io.InputStream;
import java.io.OutputStream;

public class StreamWrapper {

    final InputStream inputStream;
    final OutputStream outputStream;

    public StreamWrapper(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        DefaultDataBufferFactory.sharedInstance.allocateBuffer(10);
    }
}
