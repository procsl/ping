package cn.procsl.ping.boot.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

@Slf4j
public class Base62Test {

    private final Base62 instance = Base62.createInstance();

    private final TokenCipher tokenCipher = new TokenCipher("123", 128);


    @Test
    public void encode() {

        {

            byte[] max = Scale62.longToBytesBig(Long.MAX_VALUE);
            byte[] res = tokenCipher.encrypt(max);
            byte[] base62 = instance.encode(res);
            String str = new String(base62);
            log.info("str:[{}], len={}", str, str.length());

            byte[] decode = instance.decode(str.getBytes(StandardCharsets.UTF_8));
            byte[] decrypt = tokenCipher.decrypt(decode);
            long num = Scale62.bytesToLongBig(decrypt);
            log.info("值为:{} actual={}", num, Long.MAX_VALUE);

        }

        {
            byte[] max = Scale62.longToBytesBig(Long.MIN_VALUE);
            byte[] res = tokenCipher.encrypt(max);
            byte[] base62 = instance.encode(res);
            String str = new String(base62);
            log.info("str:[{}], len={}", str, str.length());

            byte[] decode = instance.decode(str.getBytes(StandardCharsets.UTF_8));
            byte[] decrypt = tokenCipher.decrypt(decode);
            long num = Scale62.bytesToLongBig(decrypt);
            log.info("值为:{} actual={}", num, Long.MIN_VALUE);
        }

        log.info("---------------");
        {
            byte[] max = Scale62.intToByteBig(Integer.MAX_VALUE);
            byte[] res = tokenCipher.encrypt(max);
            byte[] base62 = instance.encode(res);
            String str = new String(base62);
            log.info("str:[{}], len={}", str, str.length());

            byte[] decode = instance.decode(str.getBytes(StandardCharsets.UTF_8));
            byte[] decrypt = tokenCipher.decrypt(decode);
            long num = Scale62.bytes2IntBig(decrypt);
            log.info("值为:{} actual={}", num, Integer.MAX_VALUE);
        }

    }

    @Test
    public void decode() {


    }
}