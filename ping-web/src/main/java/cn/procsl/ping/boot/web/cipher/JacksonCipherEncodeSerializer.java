package cn.procsl.ping.boot.web.cipher;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
final class JacksonCipherEncodeSerializer extends JsonSerializer<Long> {

    final SecurityId securityId;

    final CipherSecurityService encryptDecryptService;

    @Override
    public void serialize(Long number, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (number == null) {
            return;
        }

        String str = encryptDecryptService.encrypt(number, securityId);
        jsonGenerator.writeString(str);
    }

}
