package cn.procsl.ping.boot.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class TokenCipherTest {

    TokenCipher cipher;

    {
        cipher = new TokenCipher("12345678");
    }

    @Test
    public void encrypt() {
        {
            byte[] res = cipher.encrypt("hello world".getBytes(StandardCharsets.UTF_8));
            log.info(Base64.getEncoder().encodeToString(res));
        }

        {
            byte[] res = cipher.encrypt("helloworld".getBytes(StandardCharsets.UTF_8));
            log.info(Base64.getEncoder().encodeToString(res));
        }

        {
            byte[] res = cipher.encrypt("helloorld".getBytes(StandardCharsets.UTF_8));
            log.info(Base64.getEncoder().encodeToString(res));
        }
    }

    @Test
    public void decrypt() {
        String text = "TcP6zkOOF47Q67iC3uVcGA==";
        byte[] res = cipher.decrypt(Base64.getDecoder().decode(text.getBytes(StandardCharsets.UTF_8)));
        log.info(new String(res));
    }

}
