package cn.procsl.ping.boot.web.encrypt;

import cn.procsl.ping.boot.web.component.SpringContextHolder;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@Slf4j
final class DecryptProcessor extends JsonDeserializer<Long> {

    private EncryptDecryptService encryptDecryptService;

    private final Object lock = new Object();

    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        log.debug("反序列化");
        String text = jsonParser.getText();
        if (text == null || text.isEmpty()) {
            return null;
        }
        if (encryptDecryptService == null) {
            ApplicationContext context = SpringContextHolder.getContext();
            synchronized (lock) {
                this.encryptDecryptService = context.getBean(EncryptDecryptService.class);
            }
        }
        return encryptDecryptService.decryptByContext(text);
    }
}
