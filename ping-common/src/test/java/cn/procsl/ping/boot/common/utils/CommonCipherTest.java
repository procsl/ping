package cn.procsl.ping.boot.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class CommonCipherTest {

    @Test
    public void doFinal() throws NoSuchAlgorithmException {

        {
            CommonCipher cipher = CommonCipher.init()
                    .algorithm("AES")
                    .cipherMode(CommonCipher.CipherMode.ENCRYPT)
                    .mode("CBC")
                    .padding("PKCS5Padding")
                    .privateKey("1234567812345678".getBytes(StandardCharsets.UTF_8))
                    .iv("1234567812345678")
                    .build();

            byte[] input = "hello world".getBytes(StandardCharsets.UTF_8);
            byte[] output = cipher.doFinal(input);

            String result = Base62.createInstance().encodeToString(output);
            log.info(result);
        }
        {
            CommonCipher cipher = CommonCipher.init()
                    .algorithm("AES")
                    .cipherMode(CommonCipher.CipherMode.ENCRYPT)
                    .mode("ECB")
                    .padding("PKCS5Padding")
                    .privateKey("1234567812345678".getBytes(StandardCharsets.UTF_8))
                    .build();

            byte[] input = "hello world".getBytes(StandardCharsets.UTF_8);
            byte[] output = cipher.doFinal(input);

            String result = Base62.createInstance().encodeToString(output);
            log.info(result);
        }

        {
            CommonCipher cipher = CommonCipher.init()
                    .algorithm("AES")
                    .cipherMode(CommonCipher.CipherMode.ENCRYPT)
                    .mode("ECB")
                    .padding("ISO10126Padding")
                    .privateKey("1234567812345678".getBytes(StandardCharsets.UTF_8))
                    .build();

            byte[] input = "hello world".getBytes(StandardCharsets.UTF_8);
            byte[] output = cipher.doFinal(input);

            String result = Base62.createInstance().encodeToString(output);
            log.info(result);
        }

        {
            CommonCipher cipher = CommonCipher.init()
                    .algorithm("AES")
                    .cipherMode(CommonCipher.CipherMode.ENCRYPT)
                    .mode("ECB")
                    .padding("ISO10126Padding")
                    .privateKey("1234567812345678".getBytes(StandardCharsets.UTF_8))
                    .build();

            byte[] input = "hello world".getBytes(StandardCharsets.UTF_8);
            byte[] output = cipher.doFinal(input);

            String result = Base62.createInstance().encodeToString(output);
            log.info(result);
        }

        {

            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed("AAAA".getBytes(StandardCharsets.UTF_8));

            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(128, secureRandom);

            SecretKey secretKey = generator.generateKey();
            byte[] key = secretKey.getEncoded();

            CommonCipher cipher = CommonCipher.init()
                    .algorithm("AES")
                    .cipherMode(CommonCipher.CipherMode.ENCRYPT)
                    .mode("ECB")
                    .padding("PKCS5Padding")
                    .privateKey(key)
                    .build();

            byte[] output = cipher.doFinal(Scale62.longToBytesLittle(Long.MAX_VALUE));
            String keystr = Base62.createInstance().encodeToString(key);
            log.info("key: {}", keystr);

            String result = Base62.createInstance().encodeToString(output);
            log.info(result);
        }

        {

            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed("AAAA".getBytes(StandardCharsets.UTF_8));

            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(128, secureRandom);

            SecretKey secretKey = generator.generateKey();
            byte[] key = secretKey.getEncoded();

            CommonCipher cipher = CommonCipher.init()
                    .algorithm("AES")
                    .cipherMode(CommonCipher.CipherMode.ENCRYPT)
                    .mode("ECB")
                    .padding("PKCS5Padding")
                    .privateKey(key)
                    .build();

            byte[] output = cipher.doFinal("恭喜你解密成功!!!".getBytes(StandardCharsets.UTF_8));
            String keyStr = Base64.getEncoder().encodeToString(key);
            log.info("key: {}", keyStr);

            String result = Base64.getEncoder().encodeToString(output);
            log.info(result);
        }

        {
            log.info("解密结果");
            CommonCipher cipher = CommonCipher.init()
                    .algorithm("AES")
                    .cipherMode(CommonCipher.CipherMode.DECRYPT)
                    .mode("CBC")
                    .padding("ISO10126Padding")
                    .privateKey("1234567812345678".getBytes(StandardCharsets.UTF_8))
                    .iv("1234567812345678")
                    .build();

            byte[] code = Base64.getDecoder().decode("U2FsdGVkX1/i/Noc9PXUoXyHcKhqllmbHc5ZDTGhcyRQjc11CT//ohz1IXjVCx5zth0cSbDwzOLMYfyG5SkhOoD3mz8JATCTSL0B/UdRYIg=");
            byte[] output = cipher.doFinal(code);
            log.info(new String(output));
        }


    }

    @Test
    public void decrypt() {
        {
            CommonCipher cipher = CommonCipher.init()
                    .algorithm("AES")
                    .cipherMode(CommonCipher.CipherMode.ENCRYPT)
                    .mode("CBC")
                    .padding("ISO10126Padding")
                    .privateKey("1234567812345678".getBytes(StandardCharsets.UTF_8))
                    .iv("1234567812345678")
                    .build();

            byte[] input = "你好啊".getBytes(StandardCharsets.UTF_8);
            byte[] output = cipher.doFinal(input);

            String result = Base64.getEncoder().encodeToString(output);
            log.info("加密: {}", result);
        }

        {
            log.info("解密");
            CommonCipher cipher = CommonCipher.init()
                    .algorithm("AES")
                    .cipherMode(CommonCipher.CipherMode.DECRYPT)
                    .mode("CBC")
                    .padding("ISO10126Padding")
                    .privateKey("1234567812345678".getBytes(StandardCharsets.UTF_8))
                    .iv("1234567812345678")
                    .build();

            byte[] output = cipher.doFinal(Base64.getDecoder().decode("NKxqB3bLEaBJvtpge5b9in3FewCgZrtNj+aJ3NFmK0/t3bLULm3HST9ThR9fTvbYpCnXK6Pfdi24ZLoJL+Ln4A=="));
            log.info("解密: {}", new String(output));
        }

    }

}