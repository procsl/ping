package cn.procsl.business.user.web;

import cn.procsl.business.user.web.router.IndexController;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.bohnman.squiggly.filter.SquigglyPropertyFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

/**
 * @author procsl
 * @date 2020/01/15
 */
@Slf4j
public class XMLTest {

    private PropertyNamingStrategy strategy;

    private IndexController index;

    private SimpleBeanPropertyFilter propertyFilter;

    private SimpleFilterProvider filterProvider;

    @Before
    public void before() {
        strategy = new PropertyNamingStrategy() {
            @Override
            public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
                return super.nameForField(config, field, defaultName);
            }

            @Override
            public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
                return super.nameForGetterMethod(config, method, defaultName);
            }

            @Override
            public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
                return super.nameForSetterMethod(config, method, defaultName);
            }

            @Override
            public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam, String defaultName) {
                return super.nameForConstructorParameter(config, ctorParam, defaultName);
            }
        };

        propertyFilter = new SimpleBeanPropertyFilter() {
            @Override
            protected boolean include(BeanPropertyWriter writer) {
                return true;
            }

            @Override
            protected boolean include(PropertyWriter writer) {
                return true;
            }

            @Override
            protected boolean includeElement(Object elementValue) {
                return super.includeElement(elementValue);
            }

            @Override
            public void serializeAsField(Object pojo, JsonGenerator general, SerializerProvider provider, PropertyWriter writer) throws Exception {
                log.debug("s-当前节点的path:{}", general.getOutputContext().pathAsPointer().toString());
                log.debug("name:{}", writer.getName());
                super.serializeAsField(pojo, general, provider, writer);
                log.debug("e-当前节点的path:{}\n\n\n", general.getOutputContext().pathAsPointer().toString());
            }

            @Override
            public void serializeAsElement(Object elementValue, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
                super.serializeAsElement(elementValue, jgen, provider, writer);
            }
        };

        filterProvider = new SimpleFilterProvider().addFilter(SquigglyPropertyFilter.FILTER_ID, propertyFilter);

        index = new IndexController();
    }

    @Test
    public void run() throws IOException {
        ObjectMapper mapper = new XmlMapper();
        mapper.setPropertyNamingStrategy(strategy);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mapper.configure(INDENT_OUTPUT, true);

        mapper.setFilterProvider(filterProvider);

        mapper.writer().writeValue(out, index.api());

        log.debug("\n--------------xml-----------\n{}\n-----------------xml------------", out.toString());
    }


}
