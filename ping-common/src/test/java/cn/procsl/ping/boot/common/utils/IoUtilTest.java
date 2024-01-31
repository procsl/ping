package cn.procsl.ping.boot.common.utils;

import com.github.jsonzou.jmockdata.JMockData;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.core.codec.ByteArrayDecoder;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class IoUtilTest {

    @RepeatedTest(200)
    public void test() throws IOException {
        DefaultDataBuffer buf = DefaultDataBufferFactory
                .sharedInstance.allocateBuffer(10);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Integer rand = JMockData.mock(Integer.class);
        rand = rand > 100 ? 100 : rand;
        for (Integer i = rand; i > 0; i--) {
            out.write(JMockData.mock(byte[].class));
        }
        byte[] bytes = out.toByteArray();

//        for (int a = 100; a > 0; a--) {
//            for (int i = 100; i > 0; i--) {
//                buf.write("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA".getBytes(StandardCharsets.UTF_8));
//            }
//            for (int i = 100; i > 0; i--) {
//                byte[] red = new byte[1000];
//                int pos = Math.min(buf.readPosition(), 1000);
//                buf.read(red, 0, pos);
//            }
//            for (int i = 100; i > 0; i--) {
//                buf.write("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA".getBytes(StandardCharsets.UTF_8));
//            }
//            for (int i = 100; i > 0; i--) {
//                byte[] red = new byte[1000];
//                int pos = Math.min(buf.readPosition(), 1000);
//                buf.read(red, 0, pos);
//            }
//        }
    }

}
