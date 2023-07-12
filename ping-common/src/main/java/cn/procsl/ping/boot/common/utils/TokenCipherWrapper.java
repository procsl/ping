package cn.procsl.ping.boot.common.utils;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

    public String encrypt(Long content, String scope) {
        byte[] bytes = Scale62.longToBytesBig(content);
        byte[] ss = scope.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length + ss.length);
        buffer.put(bytes);
        buffer.put(ss);
        return this.encrypt(buffer.array());
    }

    public byte[] decrypt(String content) {
        byte[] res = decoder.decode(content.getBytes(StandardCharsets.UTF_8));
        return tokenCipher.decrypt(res);
    }

    public Long decryptToLong(String content, String scope) {
        byte[] res = this.decrypt(content);

        byte[] ss = Arrays.copyOfRange(res, 8, res.length);
        String scopeOrg = new String(ss);
        if (ObjectUtils.nullSafeEquals(scope, scopeOrg)) {
            byte[] idArray = Arrays.copyOfRange(res, 0, 8);
            return Scale62.bytesToLongBig(idArray);
        }
        throw new IllegalArgumentException("scope 错误");
    }

    public interface Encoder {
        String encodeToString(byte[] input);
    }

    public interface Decoder {
        byte[] decode(byte[] bytes);
    }

}
