package cn.procsl.ping.boot.web.encrypt;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringCollectionDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

@Slf4j
class CollectionDecryptProcessor extends JsonDeserializer<Collection<Long>> {


    public CollectionDecryptProcessor() {
        StringCollectionDeserializer desc = new StringCollectionDeserializer();
    }

    @Override
    public Collection<Long> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        log.info("进入序列化");
        return Collections.emptyList();
    }

}
