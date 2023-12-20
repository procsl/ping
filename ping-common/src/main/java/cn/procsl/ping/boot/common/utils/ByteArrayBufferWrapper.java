package cn.procsl.ping.boot.common.utils;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public final class ByteArrayBufferWrapper implements ByteArrayBuffer {

    private final ByteArrayBuffer buffer;
    private final BufferListener listener;

    public static ByteArrayBuffer createByteArrayBuffer(int size) {
        return new SimpleByteArrayBuffer(size);
    }

    public static ByteArrayBuffer createByteArrayBuffer(int size, BufferListener listener) {
        return new ByteArrayBufferWrapper(createByteArrayBuffer(size), listener);
    }


    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        this.listener.onPreRead(this.buffer, len);
        int i = this.buffer.read(b, off, len);
        this.listener.onAfterRead(this.buffer, len, i);
        return i;
    }

    @Override
    public int read() throws IOException {
        this.listener.onPreRead(this.buffer, 1);
        int i = this.buffer.read();
        this.listener.onAfterRead(this.buffer, 1, i == -1 ? 0 : 1);
        return i;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.listener.onPreWrite(this.buffer, len);
        this.buffer.write(b, off, len);
        this.listener.onAfterWrite(this.buffer, len);
    }

    @Override
    public void write(byte b) throws IOException {
        this.listener.onPreWrite(this.buffer, 1);
        this.buffer.write(b);
        this.listener.onAfterWrite(this.buffer, 1);
    }

    @Override
    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    @Override
    public int size() {
        return buffer.size();
    }
}
