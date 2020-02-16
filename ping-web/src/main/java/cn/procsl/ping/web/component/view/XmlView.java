package cn.procsl.ping.web.component.view;

import cn.procsl.ping.web.serializable.PropertyFilterMixin;
import cn.procsl.ping.web.serializable.SerializableFilter;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    protected SimpleBeanPropertyFilter propertyFilter;

    @Override
    protected Object filterModel(Map<String, Object> model) {
        return Constant.filter(model);
    }

    public XmlView(String rootName) {
        super(new XmlMapper() {
            {
                this.setDefaultUseWrapper(false);
            }

            @Override
            public ObjectWriter writer() {
                return super.writer().withRootName(rootName);
            }
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        XmlMapper mapper = (XmlMapper) this.getObjectMapper();
        mapper.configure(INDENT_OUTPUT, this.format);
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter(SerializableFilter.FILTER_ID, propertyFilter);
        mapper.setFilterProvider(filterProvider);
        mapper.addMixIn(Object.class, PropertyFilterMixin.class);
    }


}
