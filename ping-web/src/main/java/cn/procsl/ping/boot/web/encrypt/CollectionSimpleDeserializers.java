package cn.procsl.ping.boot.web.encrypt;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollectionSimpleDeserializers extends SimpleDeserializers {

    final CollectionDecryptProcessor collectionDecryptProcessor;


    public CollectionSimpleDeserializers(EncryptDecryptService server) {
        this.collectionDecryptProcessor = new CollectionDecryptProcessor(server);
    }

    // TODO 暂时只处理 collection 的子类型
    @Override
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type,
                                                          DeserializationConfig config,
                                                          BeanDescription beanDesc,
                                                          TypeDeserializer elementTypeDeserializer,
                                                          JsonDeserializer<?> elementDeserializer)
            throws JsonMappingException {
        log.debug("查找 Collection 容器解析器: {}", type);
        int count = type.containedTypeCount();
        if (count != 1) {
            log.warn("使用默认的解析器解析");
            return super.findCollectionDeserializer(type,
                    config,
                    beanDesc,
                    elementTypeDeserializer,
                    elementDeserializer);
        }

        JavaType contentType = type.containedType(0);
        if (contentType.getRawClass() != Long.class) {
            log.debug("容器类型不匹配, 将使用默认解析器解析: {}", contentType.getRawClass());
            return super.findCollectionDeserializer(type,
                    config,
                    beanDesc,
                    elementTypeDeserializer,
                    elementDeserializer);
        }

        return collectionDecryptProcessor;
    }

}
