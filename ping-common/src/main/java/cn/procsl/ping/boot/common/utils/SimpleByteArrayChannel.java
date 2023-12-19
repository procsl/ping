package cn.procsl.ping.boot.common.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.util.Arrays;

public class SimpleByteArrayChannel implements ByteChannel {

    private byte[] buffer;

    private int currentPosition;

    private int last;
    private boolean closed;

    SimpleByteArrayChannel(int sz) {
        this.buffer = new byte[sz];
        this.currentPosition = this.last = 0;
    }

    SimpleByteArrayChannel(byte[] buf) {
        this.buffer = buf;
        this.currentPosition = 0;
        this.last = buf.length;
    }

    @Override
    public boolean isOpen() {
        return !closed;
    }

    public String toString() {
        return "{" +
                "buffer=" + Arrays.toString(buffer) +
                ", currentPosition=" + currentPosition +
                ", last=" + last +
                '}';
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        ensureOpen();
        if (currentPosition == last) {
            return -1;
        }
        int n = Math.min(dst.remaining(), last - currentPosition);
        dst.put(buffer, currentPosition, n);
        currentPosition += n;
        return n;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        ensureOpen();
        int n = src.remaining();
        ensureCapacity(currentPosition + n);
        src.get(buffer, currentPosition, n);
        currentPosition += n;
        if (currentPosition > last) {
            last = currentPosition;
        }
        return n;
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        buffer = null;
        currentPosition = 0;
        last = 0;
    }


    private void ensureOpen() throws IOException {
        if (closed) throw new ClosedChannelException();
    }

    private void ensureCapacity(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - buffer.length > 0) {
            grow(minCapacity);
        }
    }

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = buffer.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0) newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0) newCapacity = hugeCapacity(minCapacity);
        buffer = Arrays.copyOf(buffer, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError("Required length exceeds implementation limit");
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }
}
