package cn.procsl.ping.boot.web.encrypt;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
class CollectionDecryptProcessor extends JsonDeserializer<Collection<Long>> {

    final EncryptDecryptService encryptDecryptService;

    @Override
    public Collection<Long> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        log.info("进入序列化");

        CollectionType collectionType = ctxt.getTypeFactory().constructCollectionType(Collection.class, String.class);
        JsonDeserializer<?> deserialize = ctxt.getFactory().createCollectionDeserializer(ctxt, collectionType, null);
        Object result = deserialize.deserialize(p, ctxt);
        if (!(result instanceof Collection<?>)) {
            throw new IllegalArgumentException("请求解析错误");
        }

        return ((Collection<?>) result).stream().map(item -> {
            if (item == null) {
                return null;
            }
            if (item instanceof String) {
                return encryptDecryptService.decryptByContext((String) item);
            }
            throw new IllegalArgumentException("参数解析失败" + item);
        }).collect(Collectors.toList());
    }


}
