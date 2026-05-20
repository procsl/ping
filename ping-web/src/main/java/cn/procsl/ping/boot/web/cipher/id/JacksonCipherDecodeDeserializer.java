package cn.procsl.ping.boot.web.cipher.id;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import cn.procsl.ping.boot.web.cipher.CipherException;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

@RequiredArgsConstructor
final class JacksonCipherDecodeDeserializer extends ValueDeserializer<Long> {

    private final SecurityId securityId;

    private final SecurityIdCipherService service;

    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        String text = jsonParser.getValueAsString();
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
