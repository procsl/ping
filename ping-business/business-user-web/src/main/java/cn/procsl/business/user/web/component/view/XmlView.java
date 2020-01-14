package cn.procsl.business.user.web.component.view;

import cn.procsl.business.user.web.component.Constant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.bohnman.squiggly.filter.SquigglyPropertyFilter;
import com.github.bohnman.squiggly.filter.SquigglyPropertyFilterMixin;
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


    @Setter
    @Value("${ping.business.view.xml.elementRootName:root}")
    protected String elementRootName;

    @Autowired
    protected SimpleBeanPropertyFilter propertyFilter;

    @Override
    protected Object filterModel(Map<String, Object> model) {
        return Constant.filter(model);
    }

    public XmlView() {
        super(new XmlMapper() {
            @Override
            public ObjectWriter writer() {
                return super.writer().withRootName("root");
            }
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ObjectMapper mapper = this.getObjectMapper();
        mapper.configure(INDENT_OUTPUT, this.format);
        SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter(SquigglyPropertyFilter.FILTER_ID, propertyFilter);
        mapper.setFilterProvider(filterProvider);
        mapper.addMixIn(Object.class, SquigglyPropertyFilterMixin.class);
    }
}
