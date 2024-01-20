package cn.procsl.ping.boot.web.cipher.id;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import cn.procsl.ping.boot.web.cipher.CipherException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.RequiredArgsConstructor;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;

@RequiredArgsConstructor
final class JacksonCipherEncodeSerializer extends JsonSerializer<Long> {

    final SecurityId securityId;

    final SecurityIdCipherService cipherService;

    @Override
    public void serialize(Long number, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (number == null) {
            return;
        }

        String str;
        try {
            str = cipherService.encrypt(number, securityId);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new CipherException("加密失败", e);
        }
        jsonGenerator.writeString(str);
    }

}
