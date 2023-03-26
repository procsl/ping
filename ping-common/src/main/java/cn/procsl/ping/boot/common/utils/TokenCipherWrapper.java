package cn.procsl.ping.boot.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TokenCipherWrapper {

    final TokenCipher tokenCipher;

    final Encoder encoder;

    final Decoder decoder;

    public TokenCipherWrapper(TokenCipher tokenCipher) {
        this(tokenCipher, false);
    }

    public TokenCipherWrapper(TokenCipher tokenCipher, boolean isBase62) {
        this.tokenCipher = tokenCipher;
        if (isBase62) {
            Base62 base62 = Base62.createInstance();
            encoder = base62::encodeToString;
            decoder = base62::decode;
        } else {
            encoder = Base64.getEncoder()::encodeToString;
            decoder = Base64.getDecoder()::decode;
        }
    }


    public String encrypt(byte[] content) {
        byte[] res = tokenCipher.encrypt(content);
        return encoder.encodeToString(res);
    }

    public String encrypt(Long content) {
        byte[] bytes = Scale62.longToBytesBig(content);
        return this.encrypt(bytes);
    }

    public byte[] decrypt(String content) {
        byte[] res = decoder.decode(content.getBytes(StandardCharsets.UTF_8));
        return tokenCipher.decrypt(res);
    }

    public Long decryptToLong(String content) {
        byte[] res = this.decrypt(content);
        long l = Scale62.bytesToLongBig(res);
        return l;
    }

    public interface Encoder {
        String encodeToString(byte[] input);
    }

    public interface Decoder {
        byte[] decode(byte[] bytes);
    }

}
