package cn.procsl.ping.boot.web.cipher;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
final class JacksonCipherDecodeDeserializer extends JsonDeserializer<Long> {

    private final SecurityId securityId;

    private final CipherSecurityService cipherSecurityService;

    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String text = jsonParser.getText();
        if (text == null || text.isEmpty()) {
            return null;
        }
        return cipherSecurityService.decrypt(text.trim(), securityId);
    }
}
