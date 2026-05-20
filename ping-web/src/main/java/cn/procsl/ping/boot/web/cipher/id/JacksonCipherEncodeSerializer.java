package cn.procsl.ping.boot.web.cipher.id;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import cn.procsl.ping.boot.web.cipher.CipherException;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

@RequiredArgsConstructor
final class JacksonCipherEncodeSerializer extends ValueSerializer<Long> {

    final SecurityId securityId;

    final SecurityIdCipherService cipherService;


    @Override
    public void serialize(Long value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        if (value == null) {
            return;
        }

        String str;
        try {
            str = cipherService.encrypt(value, securityId);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new CipherException("加密失败", e);
        }
        gen.writeString(str);
    }
}
