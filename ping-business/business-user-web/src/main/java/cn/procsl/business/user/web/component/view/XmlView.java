package cn.procsl.business.user.web.component.view;

import cn.procsl.business.user.web.component.Constant;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.bohnman.squiggly.filter.SquigglyPropertyFilter;
import com.github.bohnman.squiggly.filter.SquigglyPropertyFilterMixin;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.xml.MappingJackson2XmlView;

import java.util.Map;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

/**
 * @author procsl
 * @date 2020/01/01
 */
public class XmlView extends MappingJackson2XmlView implements InitializingBean {
    @Setter
    @Value("${ping.business.view.output.format:true}")
    protected boolean format;

    @Override
    protected Object filterModel(Map<String, Object> model) {
        return Constant.filter(model);
    }

    public XmlView(String rootName) {
        super(new XmlMapper() {
            @Override
            public ObjectWriter writer() {
                return super.writer().withRootName(rootName);
            }
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ObjectMapper mapper = this.getObjectMapper();
        mapper.configure(INDENT_OUTPUT, this.format);
        SimpleBeanPropertyFilter propertyFilter = new SimpleBeanPropertyFilter() {
            @Override
            protected boolean include(BeanPropertyWriter writer) {
                return super.include(writer);
            }

            @Override
            protected boolean include(PropertyWriter writer) {
                return super.include(writer);
            }

            @Override
            protected boolean includeElement(Object elementValue) {
                return super.includeElement(elementValue);
            }

            @Override
            public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
                super.serializeAsField(pojo, jgen, provider, writer);
            }

            @Override
            public void serializeAsElement(Object elementValue, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
                super.serializeAsElement(elementValue, jgen, provider, writer);
            }
        };
        SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter(SquigglyPropertyFilter.FILTER_ID, propertyFilter);
        mapper.setFilterProvider(filterProvider);
        mapper.addMixIn(Object.class, SquigglyPropertyFilterMixin.class);
    }
}
