package cn.procsl.ping.boot.common.utils;

import java.io.IOException;

public interface ByteArrayBuffer {

    int read(byte[] b, int off, int len) throws IOException;

    int read() throws IOException;

    default int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    void write(byte[] b, int off, int len) throws IOException;

    void write(byte b) throws IOException;

    default void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    boolean isEmpty();

    int size();

}
