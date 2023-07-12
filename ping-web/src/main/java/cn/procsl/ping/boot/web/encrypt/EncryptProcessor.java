package cn.procsl.ping.boot.web.encrypt;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import cn.procsl.ping.boot.web.component.SpringContextHolder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public final class EncryptProcessor extends JsonSerializer<Long> {

    final SecurityId securityId;

    final EncryptDecryptService encryptDecryptService;

    @Override
    public void serialize(Long number, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (number == null) {
            return;
        }

        String str = encryptDecryptService.encryptByContext(number, securityId.scope());
        jsonGenerator.writeString(str);
    }

}
