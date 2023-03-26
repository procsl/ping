package cn.procsl.ping.boot.web.encrypt;

import cn.procsl.ping.boot.web.SpringContextHolder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@Slf4j
final class EncryptProcessor extends JsonSerializer<Long> {

    private EncryptDecryptService encryptDecryptService;

    private final Object lock = new Object();

    @Override
    public void serialize(Long number, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (number == null) {
            return;
        }

        if (encryptDecryptService == null) {
            ApplicationContext context = SpringContextHolder.getContext();
            synchronized (lock) {
                this.encryptDecryptService = context.getBean(EncryptDecryptService.class);
            }
        }

        String str = encryptDecryptService.encryptByContext(number);
        jsonGenerator.writeString(str);
    }

}
