package cn.procsl.ping.boot.web.encrypt;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

@Slf4j
public class CollectionDecryptProcessor extends JsonDeserializer<Collection<Long>> {

    @Override
    public Collection<Long> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        log.info("进入序列化");
        return Collections.emptyList();
    }

    public SimpleDeserializers rebuild() {
        return new CollectionSimpleDeserializers();
    }

//    @Override
//    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
//        return null;
//    }

    private class CollectionSimpleDeserializers extends SimpleDeserializers {

        public CollectionSimpleDeserializers() {
        }

        // TODO 暂时只处理 collection 的子类型
        @Override
        public JsonDeserializer<?> findCollectionDeserializer(CollectionType type,
                                                              DeserializationConfig config,
                                                              BeanDescription beanDesc,
                                                              TypeDeserializer elementTypeDeserializer,
                                                              JsonDeserializer<?> elementDeserializer)
                throws JsonMappingException {
            return getJsonDeserializer(type, config, beanDesc, elementTypeDeserializer, elementDeserializer);
        }

        private JsonDeserializer<?> getJsonDeserializer(CollectionType type,
                                                        DeserializationConfig config,
                                                        BeanDescription beanDesc,
                                                        TypeDeserializer elementTypeDeserializer,
                                                        JsonDeserializer<?> elementDeserializer)
                throws JsonMappingException {
            log.info("查找 Collection 容器解析器: {}", type);
            int count = type.containedTypeCount();
            if (count != 1) {
                log.warn("使用默认的解析器解析");
                return super.findCollectionDeserializer(type, config, beanDesc, elementTypeDeserializer, elementDeserializer);
            }

            JavaType contentType = type.containedType(0);
            if (contentType.getRawClass() != Long.class) {
                log.info("容器类型不匹配, 将使用默认解析器解析: {}", contentType.getRawClass());
                return super.findCollectionDeserializer(type, config, beanDesc, elementTypeDeserializer, elementDeserializer);
            }

            return CollectionDecryptProcessor.this;
        }

    }

}
