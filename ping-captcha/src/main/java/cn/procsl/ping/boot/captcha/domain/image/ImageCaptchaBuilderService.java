package cn.procsl.ping.boot.captcha.domain.image;

import cn.procsl.ping.boot.common.utils.TokenCipher;
import cn.procsl.ping.boot.common.utils.TokenCipherWrapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ImageCaptchaBuilderService {

    final JsonMapper jsonMapper = new JsonMapper();


    @SneakyThrows
    public String buildToken(String key, ImageCaptcha captcha) {
        String json = jsonMapper.writeValueAsString(captcha);
        TokenCipherWrapper cipher = new TokenCipherWrapper(new TokenCipher(key));
        return cipher.encrypt(json);
    }

    @SneakyThrows
    public ImageCaptcha buildForToken(String key, String token) {
        TokenCipherWrapper cipher = new TokenCipherWrapper(new TokenCipher(key));
        String json = cipher.decrypt(token);
        return jsonMapper.readValue(json, ImageCaptcha.class);
    }


}
