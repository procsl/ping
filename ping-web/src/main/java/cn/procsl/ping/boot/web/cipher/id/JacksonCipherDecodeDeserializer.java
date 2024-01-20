package cn.procsl.ping.boot.web.cipher.id;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import cn.procsl.ping.boot.web.cipher.CipherException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.RequiredArgsConstructor;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;

@RequiredArgsConstructor
final class JacksonCipherDecodeDeserializer extends JsonDeserializer<Long> {

    private final SecurityId securityId;

    private final SecurityIdCipherService service;

    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String text = jsonParser.getText();
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return service.decrypt(text.trim(), securityId);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new CipherException("字段解密失败", e);
        }
    }
}
