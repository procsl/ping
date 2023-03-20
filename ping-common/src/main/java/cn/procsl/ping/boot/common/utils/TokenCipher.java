package cn.procsl.ping.boot.common.utils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.*;

/**
 * TODO javax.crypto 不是线程安全的, 同时注意填充方式,  ISO10126Padding 使用随机数填充, 有可能会有性能问题
 */
public class TokenCipher {

    final Cipher encrypt;

    final Cipher decrypt;

    public TokenCipher(String seed, String parameter, int keySize) {

        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(seed.getBytes(StandardCharsets.UTF_8));

            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(keySize, secureRandom);

            SecretKey seedKey = generator.generateKey();

            byte[] bytes = this.md5(seed);
            IvParameterSpec iv = new IvParameterSpec(bytes);

            this.encrypt = Cipher.getInstance(parameter);
            this.encrypt.init(Cipher.ENCRYPT_MODE, seedKey, iv);


            this.decrypt = Cipher.getInstance(parameter);
            this.decrypt.init(Cipher.DECRYPT_MODE, seedKey, iv);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException e) {
            throw new TokenCipherException(e);
        }
    }

    public TokenCipher(String seedKey) {
        this(seedKey, false, 256);
    }

    public TokenCipher(String seedKey, boolean randPadding) {
        this(seedKey, randPadding, 256);
    }

    public TokenCipher(String seedKey, boolean randPadding, int keySize) {
        this(seedKey, randPadding ? "AES/CBC/ISO10126Padding" : "AES/CBC/PKCS5Padding", keySize);
    }

    public TokenCipher(String seedKey, int keySize) {
        this(seedKey, false, keySize);
    }

    public byte[] encrypt(byte[] content) {
        try {
            return encrypt.doFinal(content);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new TokenCipherException(e);
        }
    }

    public byte[] decrypt(byte[] content) {
        try {
            return decrypt.doFinal(content);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new TokenCipherException(e);
        }
    }

    private byte[] md5(String dataStr) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.update(dataStr.getBytes(StandardCharsets.UTF_8));
        return m.digest();
    }


}
