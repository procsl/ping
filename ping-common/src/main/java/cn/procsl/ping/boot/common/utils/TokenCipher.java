package cn.procsl.ping.boot.common.utils;

import lombok.SneakyThrows;

import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class TokenCipher {

    final KeyGenerator generator;
    final SecretKey privateKey;


    public TokenCipher(String privateKey) throws NoSuchAlgorithmException {
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(privateKey.getBytes(StandardCharsets.UTF_8));
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256, secureRandom);
        this.privateKey = generator.generateKey();
        this.generator = generator;
    }

    public byte[] encrypt(byte[] content) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return cipher.doFinal(content);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] decrypt(byte[] content) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(content);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }


}
