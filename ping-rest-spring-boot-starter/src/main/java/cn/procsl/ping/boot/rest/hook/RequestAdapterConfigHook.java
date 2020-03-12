package cn.procsl.ping.boot.rest.hook;

import cn.procsl.ping.boot.rest.config.RestWebProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.procsl.ping.boot.rest.config.RestWebProperties.MetaMediaType.json;
import static cn.procsl.ping.boot.rest.config.RestWebProperties.MetaMediaType.xml;

/**
 * @author procsl
 * @date 2020/03/12
 */
@RequiredArgsConstructor
public class RequestAdapterConfigHook implements RequestAdapterHook {

    final Map<RestWebProperties.MetaMediaType, List<MediaType>> mediaTypes;

    final HashMap<String, ObjectMapper> objectMappers;

    final ApplicationContext applicationContext;

    @Override
    public void httpMessageConverter(List<HttpMessageConverter<?>> httpMessageConverters) {
        MappingJackson2HttpMessageConverter jsonConvert = null;
        MappingJackson2XmlHttpMessageConverter xmlConvert = null;

        for (HttpMessageConverter<?> converter : httpMessageConverters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                jsonConvert = (MappingJackson2HttpMessageConverter) converter;
            }

            if (converter instanceof MappingJackson2XmlHttpMessageConverter) {
                xmlConvert = (MappingJackson2XmlHttpMessageConverter) converter;
            }
        }

        if (mediaTypes.containsKey(json.name())) {
            if (jsonConvert == null) {
                jsonConvert = this.createJackson2HttpMessageConverter();
                httpMessageConverters.add(1, jsonConvert);
            } else {
                ObjectMapper jsonMapper = this.objectMappers.get(json);
                Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
                builder.applicationContext(this.applicationContext);
                builder.configure(jsonMapper);
                jsonConvert.setObjectMapper(jsonMapper);
            }
        }

        if (mediaTypes.containsKey(xml)) {
            if (xmlConvert == null) {
                xmlConvert = this.createJackson2XmlHttpMessageConverter();
                httpMessageConverters.add(2, xmlConvert);
            } else {
                ObjectMapper xmlMapper = this.objectMappers.get(xml);
                Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
                builder.applicationContext(this.applicationContext);
                builder.configure(xmlMapper);
                xmlConvert.setObjectMapper(xmlMapper);
            }
        }
    }

    protected MappingJackson2HttpMessageConverter createJackson2HttpMessageConverter() {
        ObjectMapper jsonMapper = this.objectMappers.get(json);
        MappingJackson2HttpMessageConverter jsonConverter;
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        builder.applicationContext(this.applicationContext);
        builder.configure(jsonMapper);
        jsonConverter = new MappingJackson2HttpMessageConverter(jsonMapper);
        jsonConverter.setObjectMapper(jsonMapper);
        jsonConverter.setSupportedMediaTypes(this.mediaTypes.get(json));
        return jsonConverter;
    }

    protected MappingJackson2XmlHttpMessageConverter createJackson2XmlHttpMessageConverter() {
        ObjectMapper xmlMapper = this.objectMappers.get(xml);
        MappingJackson2XmlHttpMessageConverter xmlConverter;
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.xml();
        builder.applicationContext(this.applicationContext);
        builder.configure(xmlMapper);
        xmlConverter = new MappingJackson2XmlHttpMessageConverter(xmlMapper);
        xmlConverter.setObjectMapper(xmlMapper);
        xmlConverter.setSupportedMediaTypes(this.mediaTypes.get(xml));
        return xmlConverter;
    }


}
