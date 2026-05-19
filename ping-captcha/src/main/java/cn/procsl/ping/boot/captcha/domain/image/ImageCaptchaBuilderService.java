package cn.procsl.ping.boot.captcha.domain.image;

import cn.procsl.ping.boot.common.utils.TokenCipher;
import cn.procsl.ping.boot.common.utils.TokenCipherWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class ImageCaptchaBuilderService {

    final JsonMapper jsonMapper = new JsonMapper();


    public String serializeSecureToken(String key, ImageCaptcha captcha) {
//        try {
        byte[] json = jsonMapper.writeValueAsBytes(captcha);
        TokenCipher cipher = new TokenCipher(key, true, 256);
        TokenCipherWrapper wrapper = new TokenCipherWrapper(cipher);
        return wrapper.encrypt(json);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
    }

    public ImageCaptcha buildForToken(String key, String token) throws IOException {
        TokenCipherWrapper cipher;
        cipher = new TokenCipherWrapper(new TokenCipher(key, true, 256));
        byte[] json = cipher.decrypt(token);
        return jsonMapper.readValue(json, ImageCaptcha.class);
    }


}
