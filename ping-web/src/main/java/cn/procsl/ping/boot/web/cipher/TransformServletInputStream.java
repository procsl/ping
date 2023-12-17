package cn.procsl.ping.boot.web.cipher;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class TransformServletInputStream extends FilterInputStream implements Transform.TransformWriter {


    private final byte[] readBuffer = new byte[1024];
    private final byte[] originBuffer = new byte[this.readBuffer.length];
    private final Transform transform;
    private int readOffset = 0;

    private int writeOffset = 0;

    protected TransformServletInputStream(InputStream in, Transform transform) {
        super(in);
        transform.init(this);
        this.transform = transform;
    }

    @Override
    public int read() throws IOException {
        // 如果已经读取完成, 则直接返回
        if (readOffset == -1) {
            return -1;
        }

        this.fill();

        // 如果已经读取完成, 则直接返回
        if (readOffset == -1) {
            return -1;
        }

        // 如果buffer中还有数据，则直接返回
        if (this.readOffset < this.writeOffset) {
            int tmp = this.readBuffer[this.readOffset] & 0xFF;
            this.readOffset++;
            return tmp;
        } else {
            // 填充之后, 还是等于 写指针大小, 这种情况应该是错误的
            throw new IllegalStateException("buffer size 错误");
        }

    }

    private void fill() throws IOException {
        if (this.readOffset == -1) {
            return;
        }

        if (this.readOffset < this.writeOffset) {
            return;
        }

        // 先读取指定长度数据
        int len = super.read(originBuffer, 0, this.readBuffer.length);

        // 每次读取之前重置写指针
        this.writeOffset = 0;

        // 调用update方法填充数据
        this.transform.update(originBuffer, 0, len);

        // 更新数据之后, 判断当前的写指针长度,如果写指针长度为0, 则代表未读取到数据, 此时数据已经读取完成
        if (this.writeOffset == 0) {
            this.readOffset = -1;
            return;
        }

        // 如果写指针长度大于0, 则代表已经读取到数据, 此时需要将读指针重置为0
        this.readOffset = 0;
    }


    @Override
    public int read(@Nonnull byte[] b, int off, int len) throws IOException {

        if (off < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException("byte数组溢出");
        }

        if (len == 0 || len < 0) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < len; i++) {
            int res = this.read();
            // 结束
            if (res == -1) {
                break;
            }
            b[i + off] = (byte) res;
            count = i + 1;
        }

        return count;
    }

    @Override
    public long skip(long n) throws IOException {
        throw new UnsupportedOperationException("不支持的操作");
    }

    @Override
    public int available() throws IOException {
        throw new UnsupportedOperationException("不支持的操作");
    }

    @Override
    public void close() throws IOException {
        super.close();
    }


    @Override
    public boolean write(int b) throws IOException {
        // 首先判断缓冲区是否写满
        if (this.writeOffset < this.readBuffer.length) {
            this.readBuffer[this.writeOffset] = (byte) b;
            this.writeOffset++;
            return true;
        }

        return false;
    }


}
