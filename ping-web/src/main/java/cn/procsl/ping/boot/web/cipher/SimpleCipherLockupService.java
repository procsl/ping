package cn.procsl.ping.boot.web.cipher;

import cn.procsl.ping.boot.common.utils.CipherFactory;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;

public class SimpleCipherLockupService implements CipherLockupService {

    final CipherFactory.CipherFactoryBuilder encryptBuilder = CipherFactory.init()
            .algorithm("AES")
            .cipherMode(CipherFactory.CipherMode.ENCRYPT)
            .mode("ECB")
            .padding("PKCS5Padding")
            .privateKey("12345678".getBytes(StandardCharsets.UTF_8));

    final CipherFactory.CipherFactoryBuilder decryptBuilder = CipherFactory.init()
            .algorithm("AES")
            .cipherMode(CipherFactory.CipherMode.DECRYPT)
            .mode("ECB")
            .padding("PKCS5Padding")
            .privateKey("12345678".getBytes(StandardCharsets.UTF_8));

    @Override
    public Cipher lockupEncryptCipher(CipherScope scope) {
        return encryptBuilder.build().getCipher();
    }

    @Override
    public Cipher lockupDecryptCipher(CipherScope scope) {
        return decryptBuilder.build().getCipher();
    }

    @Override
    public void release(CipherScope scope, Cipher cipher) {

    }
}
