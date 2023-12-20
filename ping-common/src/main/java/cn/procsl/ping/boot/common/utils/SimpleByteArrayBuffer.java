package cn.procsl.ping.boot.common.utils;

import java.io.IOException;

final class SimpleByteArrayBuffer implements ByteArrayBuffer {

    private byte[] buffer;
    private int writeOffset;
    private int readOffset;

    public SimpleByteArrayBuffer(int sz) {
        this.buffer = new byte[sz];
        this.readOffset = 0;
        this.writeOffset = 0;
    }

    /**
     * 从缓冲区中读取字节到指定的数组。
     *
     * @param b   目标字节数组
     * @param off 写入的起始索引
     * @param len 要读取的字节数
     * @return 实际读取的字节数
     * @throws IllegalArgumentException 如果提供的数组长度不足以容纳读取请求
     */
    public int read(byte[] b, int off, int len) {

        isValidReadRequest(b, off, len);

        // 检查是否有足够的可用数据
        if (isEmpty()) return 0;

        int actualLen = Math.min(len, this.size()); // 读取的实际长度，为len和available的较小值

        // 将数据从缓冲区复制到目标数组
        System.arraycopy(this.buffer, this.readOffset, b, off, actualLen);

        // 更新读取指针
        this.readOffset += actualLen;
        return actualLen;
    }

    @Override
    public int read() throws IOException {
        if (isEmpty()) return -1;
        byte tmp = buffer[this.readOffset];
        this.readOffset++;
        return (tmp & 0xFF);
    }


    private static void isValidReadRequest(byte[] b, int off, int len) {
        if (b.length < off + len) {
            throw new IllegalArgumentException("The buffer length is not enough to accommodate the read request");
        }
    }

    /**
     * 写入字节数组到内部缓冲区。
     *
     * @param b   要写入的字节数组
     * @param off 写入的起始索引
     * @param len 要写入的字节数
     * @throws IllegalArgumentException 如果写入请求无效
     */
    public void write(byte[] b, int off, int len) {
        isValidReadRequest(b, off, len);
        this.ensureCapacity(len);
        System.arraycopy(b, off, this.buffer, this.writeOffset, len);
        this.writeOffset += len;
    }

    @Override
    public void write(byte b) throws IOException {
        this.ensureCapacity(1);
        this.buffer[this.writeOffset] = b;
        this.writeOffset += 1;
    }

    private void ensureCapacity(int len) {

        // 如果已经读取完成, 直接重置指针
        if (isEmpty()) {
            this.readOffset = 0;
            this.writeOffset = 0;
        }

        // 如果当前剩余空间足够, 直接返回
        if (this.buffer.length - this.writeOffset >= len) {
            return;
        }

        // 至少扩容16个字节
        if (len <= 16) {
            len = 16;
        }

        // 空间不够
        byte[] tmp = new byte[this.buffer.length + len];
        System.arraycopy(this.buffer, this.readOffset, tmp, 0, this.writeOffset - this.readOffset);
        this.readOffset = 0;
        this.buffer = tmp;
    }

    public boolean isEmpty() {
        return this.size() <= 0;
    }

    public int size() {
        return this.writeOffset - this.readOffset;
    }

}
