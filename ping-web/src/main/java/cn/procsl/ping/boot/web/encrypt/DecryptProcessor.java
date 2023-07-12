package cn.procsl.ping.boot.web.encrypt;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public final class DecryptProcessor extends JsonDeserializer<Long> {

    private final SecurityId securityId;

    private final EncryptDecryptService encryptDecryptService;

    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        log.debug("反序列化");
        String text = jsonParser.getText();
        if (text == null || text.isEmpty()) {
            return null;
        }
        return encryptDecryptService.decryptByContext(text, securityId.scope());
    }
}
