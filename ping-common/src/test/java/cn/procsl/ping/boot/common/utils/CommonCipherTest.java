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
                    .iv("1234567812345678".getBytes(StandardCharsets.UTF_8))
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
                    .iv("1234567812345678".getBytes(StandardCharsets.UTF_8))
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
                    .iv("1234567812345678".getBytes(StandardCharsets.UTF_8))
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
                    .iv("1234567812345678".getBytes(StandardCharsets.UTF_8))
                    .build();

            byte[] output = cipher.doFinal(Base64.getDecoder().decode("NKxqB3bLEaBJvtpge5b9in3FewCgZrtNj+aJ3NFmK0/t3bLULm3HST9ThR9fTvbYpCnXK6Pfdi24ZLoJL+Ln4A=="));
            log.info("解密: {}", new String(output));
        }

        {
            log.info("加密密");
            CommonCipher cipher = CommonCipher.init()
                    .algorithm("AES")
                    .cipherMode(CommonCipher.CipherMode.ENCRYPT)
                    .mode("CBC")
                    .padding("ISO10126Padding")
                    .privateKey("1234567812345678".getBytes(StandardCharsets.UTF_8))
                    .iv("1234567812345678".getBytes(StandardCharsets.UTF_8))
                    .build();

//            Base64.getDecoder().decode("NKxqB3bLEaBJvtpge5b9in3FewCgZrtNj+aJ3NFmK0/t3bLULm3HST9ThR9fTvbYpCnXK6Pfdi24ZLoJL+Ln4A==")
//            Base64.getEncoder().encode(aa.getBytes(StandardCharsets.UTF_8))
            String aa = "你好啊";
            byte[] output = cipher.doFinal(aa.getBytes(StandardCharsets.UTF_8));
            log.info("加密: {}", Base64.getEncoder().encodeToString(output));
        }

    }

    @Test
    public void getKey() throws NoSuchAlgorithmException {
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed("AAAA".getBytes(StandardCharsets.UTF_8));

        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128, secureRandom);

        SecretKey secretKey = generator.generateKey();
        byte[] key = secretKey.getEncoded();
        log.info("key: [{}]", Base64.getEncoder().encodeToString(key));
    }

    @Test
    public void test3(){
//        byte[] key = Base64.getDecoder().decode("MTIzNDU2Nzg5MTIz");
        byte[] key = "MTIzNDU2Nzg5MTIz".getBytes(StandardCharsets.UTF_8);

        String out;
        {
            log.info("加密");
            CommonCipher cipher = CommonCipher.init()
                    .algorithm("AES")
                    .cipherMode(CommonCipher.CipherMode.ENCRYPT)
                    .mode("CBC")
                    .padding("ISO10126Padding")
                    .privateKey(key)
                    .iv(key)
                    .build();

            String aa = "你好啊";
            byte[] output = cipher.doFinal(aa.getBytes(StandardCharsets.UTF_8));
            out = Base64.getEncoder().encodeToString(output);
            log.info("加密: {}", out);
        }

        {
            log.info("解密");
            CommonCipher cipher = CommonCipher.init()
                    .algorithm("AES")
                    .cipherMode(CommonCipher.CipherMode.DECRYPT)
                    .mode("CBC")
                    .padding("ISO10126Padding")
                    .privateKey(key)
                    .iv(key)
                    .build();

            byte[] bb = Base64.getDecoder().decode("dFd37pKtna+HsedmeBaAVw==");
            byte[] output = cipher.doFinal(bb);
            log.info("解密: {}", new String(output));
        }

        log.info("再加密: {}", out);
        {
            log.info("解密");
            CommonCipher cipher = CommonCipher.init()
                    .algorithm("AES")
                    .cipherMode(CommonCipher.CipherMode.DECRYPT)
                    .mode("CBC")
                    .padding("ISO10126Padding")
                    .privateKey(key)
                    .iv(key)
                    .build();

            byte[] bb = Base64.getDecoder().decode(out);
            byte[] output = cipher.doFinal(bb);
            log.info("解密: {}", new String(output));
        }
    }

    @Test
    public void base64(){
        byte[] res = Base64.getDecoder().decode("MTIzNDU2Nzg5MA==");
        log.info("res: {}", new String(res));
    }


}