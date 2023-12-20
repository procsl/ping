package cn.procsl.ping.boot.web.cipher;

import cn.procsl.ping.boot.common.utils.BufferListener;
import cn.procsl.ping.boot.common.utils.ByteArrayBuffer;
import cn.procsl.ping.boot.common.utils.ByteArrayBufferWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class TransformInputStream extends InputStream implements BufferListener {

    private final ByteArrayBuffer buffer;

    private final byte[] tmp = new byte[128];

    public TransformInputStream(InputStream inputStream, int size) {
        this.buffer = ByteArrayBufferWrapper.createByteArrayBuffer(254, this);
    }

    @Override
    public void onPreRead(ByteArrayBuffer buffer, int len) throws IOException {
        // 判断缓冲区是否有数据, 如果有数据,且数据量大于需要读取的数据量, 则返回, 否则触发写入
//        do {
//            if (buffer.size() > len) {
//                return;
//            }
//            int res = this.inputStream.read(tmp);
//            if (res <= 0) {
//                return;
//            }
//            // TODO 处理数据
//            this.buffer.write(tmp, 0, res);
//        } while (true);
    }


    @Override
    public int read() throws IOException {
        return buffer.read();
    }


    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return buffer.read(b, off, len);
    }


}
