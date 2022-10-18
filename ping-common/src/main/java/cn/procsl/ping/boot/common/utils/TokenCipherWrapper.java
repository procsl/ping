package cn.procsl.ping.boot.common.utils;

import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RequiredArgsConstructor
public class TokenCipherWrapper {

    final TokenCipher tokenCipher;

    final Base64.Encoder encoder = Base64.getEncoder();

    final Base64.Decoder decoder = Base64.getDecoder();


    public String encrypt(String content) {
        byte[] res = tokenCipher.encrypt(content.getBytes(StandardCharsets.UTF_8));
        return encoder.encodeToString(res);
    }

    public String decrypt(String content) {
        byte[] res = decoder.decode(content.getBytes(StandardCharsets.UTF_8));
        byte[] decode = tokenCipher.decrypt(res);
        return new String(decode);
    }

}
