package cn.procsl.ping.boot.common.utils;

import cn.procsl.ping.boot.common.error.BusinessException;
import lombok.Builder;
import lombok.NonNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CommonCipher {

    private final Cipher cipher;
    private final CipherMode m;

    @Builder(builderMethodName = "init")
    private CommonCipher(@NonNull byte[] privateKey,
                         @NonNull CipherMode cipherMode,
                         @NonNull String algorithm,
                         @NonNull String mode,
                         @NonNull String padding,
                         byte[] iv) {

        if (!algorithm.equals("AES")) {
            throw new IllegalArgumentException("不支持的算法");
        }

        try {
            this.cipher = Cipher.getInstance("%s/%s/%s".formatted(algorithm, mode, padding));
        } catch (NoSuchPaddingException e) {
            throw new IllegalArgumentException("创建%s组件失败, 参数错误".formatted(cipherMode.toString()), e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("创建%s组件失败, 找不到指定的算法".formatted(cipherMode.toString()), e);
        }

        SecretKeySpec spec = new SecretKeySpec(privateKey, "AES");

        try {
            if (iv != null) {
                IvParameterSpec ivp = new IvParameterSpec(iv);
                this.cipher.init(cipherMode.mode, spec, ivp);
            } else {
                this.cipher.init(cipherMode.mode, spec);
            }
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("初始化%s组件失败, 错误的密钥".formatted(cipherMode.toString()), e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException("初始化%s组件失败, 初始化向量错误".formatted(cipherMode.toString()), e);
        }
        this.m = cipherMode;
    }

    private CommonCipher(@NonNull String privateKey,
                         @NonNull CipherMode cipherMode,
                         @NonNull String algorithm,
                         @NonNull String mode,
                         @NonNull String padding,
                         byte[] iv) {

        this(privateKey.getBytes(StandardCharsets.UTF_8), cipherMode, algorithm, mode, padding, iv);
    }

    public byte[] doFinal(byte[] input) {
        try {
            return this.cipher.doFinal(input);
        } catch (IllegalBlockSizeException e) {
            throw new BusinessException("数据%s失败, 错误的数据".formatted(m.toString()), e);
        } catch (BadPaddingException e) {
            throw new BusinessException("数据%s失败".formatted(m.toString()), e);
        }
    }

    enum CipherMode {
        ENCRYPT(Cipher.ENCRYPT_MODE), DECRYPT(Cipher.DECRYPT_MODE);

        final int mode;

        CipherMode(int decryptMode) {
            this.mode = decryptMode;
        }

        @Override
        public String toString() {
            return mode == CipherMode.ENCRYPT.mode ? "加密" : "解密";
        }
    }


}