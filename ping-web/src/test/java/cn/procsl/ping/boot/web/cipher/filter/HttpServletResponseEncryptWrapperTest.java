package cn.procsl.ping.boot.web.cipher.filter;

import com.github.jsonzou.jmockdata.JMockData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.slf4j.event.Level;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class HttpServletResponseEncryptWrapperTest {


    @RepeatedTest(20)
    public void test() throws IOException {
        Base64.Encoder coder = Base64.getMimeEncoder(10, new byte[0]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        OutputStream wrap = coder.wrap(out);
        OutputStream wrap3 = Base64.getEncoder().wrap(out3);
        Integer times = JMockData.mock(Integer.class);
        for (int i = times; i > 0; i--) {
            byte[] bty = JMockData.mock(byte[].class);
            wrap.write(bty);
            out2.write(bty);
            wrap3.write(bty);
        }
        wrap.close();
        out2.close();
        wrap3.close();
        out.close();
        out3.close();

        wrap.close();
        byte[] res1 = out.toByteArray();
//        log.info("魔改: [{}]", new String(res1, StandardCharsets.UTF_8));
//        log.info("改改: [{}]", new String(coder.encode(out2.toByteArray()), StandardCharsets.UTF_8));


        byte[] res2 = Base64.getEncoder().encode(out2.toByteArray());
//        log.info("标准: [{}]", new String(res2, StandardCharsets.UTF_8));

        wrap3.close();
        byte[] array = out3.toByteArray();
        String res3 = new String(array, StandardCharsets.UTF_8);
//        log.info("默认: [{}]", res3);
        Assertions.assertArrayEquals(array, res2);
        Assertions.assertArrayEquals(res1, res2);
    }
}