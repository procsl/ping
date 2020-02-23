package cn.procsl.ping.web.component;

import cn.procsl.ping.web.serializable.PropertyFilterMixin;
import cn.procsl.ping.web.serializable.SerializableFilter;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author procsl
 * @date 2020/02/16
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Bean
    public XmlMapper xmlMapper(@Value("${ping.xmlMapper.rootName:root}") String rootName,
                               @Value("${ping.xmlMapper.defaultUseWrapper:true}") boolean defaultUseWrapper,
                               @Autowired PropertyNamingStrategy propertyNamingStrategy,
                               @Autowired SimpleBeanPropertyFilter propertyFilter) {

        XmlMapper mapper = new XmlMapper() {
            {
                this.setDefaultUseWrapper(defaultUseWrapper);
            }

            @Override
            public ObjectWriter writer() {
                return super.writer().withRootName(rootName);
            }
        };
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        mapper.setPropertyNamingStrategy(propertyNamingStrategy);
        SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter(SerializableFilter.FILTER_ID, propertyFilter);
        mapper.setFilterProvider(filterProvider);
        mapper.addMixIn(Object.class, PropertyFilterMixin.class);

        return mapper;
    }

    @Bean
    @Primary
    public JsonMapper jsonMapper(@Autowired SimpleBeanPropertyFilter propertyFilter,
                                 @Autowired PropertyNamingStrategy propertyNamingStrategy) {
        JsonMapper mapper = JsonMapper.builder().build();
        SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter(SerializableFilter.FILTER_ID, propertyFilter);
        mapper.setFilterProvider(filterProvider);
        mapper.setPropertyNamingStrategy(propertyNamingStrategy);
        mapper.addMixIn(Object.class, PropertyFilterMixin.class);
        return mapper;
    }

}
