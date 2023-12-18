package cn.procsl.ping.boot.web.cipher;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

@Slf4j
public class TransformInputStreamTest {

    @Test
    public void testTransformServlet() throws IOException, URISyntaxException {

        FileInputStream is = new FileInputStream("C:\\Users\\procsl\\Desktop\\微信图片_20231007220929.png");
        Transform transform = new Transform() {

            TransformWriter writer;


            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int currentOffset = 0;

            @Override
            public void init(TransformWriter writer) {
                this.writer = writer;
            }

            @Override
            public void update(byte[] buffer, int offset, int length) throws IOException {
                // 将转换后的数据写回
                log.info("{}, {}, {}", buffer, offset, length);
                outputStream.write(buffer, offset, length);
//                ByteArrayInputStream in = new ByteArrayInputStream();
//                for (; ; ) {
//                    boolean bool = this.writer.write(outputStream.);
//                    if (!bool) {
//                        break;
//                    }
//
//                }
//                this.outputStream.
            }

        };


        TransformInputStream trans = new TransformInputStream(is, transform);

        int read = trans.read();
        if (read == -1) {
            log.info("{}", read);
        }

    }

    public static class Buffered {
        byte[] buffer;
        int offset;

        public boolean isOver() {
            return !(offset < buffer.length);
        }

    }

}