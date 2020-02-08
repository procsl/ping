package cn.procsl.business.user.web.component.view;

import cn.procsl.business.user.web.component.Constant;
import cn.procsl.business.user.web.component.FieldsPropertyFilter;
import cn.procsl.business.user.web.component.PropertyFilterMixin;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.json.AbstractJackson2View;

import java.util.Map;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

/**
 * @author procsl
 * @date 2020/01/14
 */
public class YamlView extends AbstractJackson2View implements InitializingBean {


    @Setter
    @Value("${ping.business.view.output.format:true}")
    protected boolean format;


    @Setter
    @Value("${ping.business.view.xml.elementRootName:root}")
    protected String elementRootName;

    @Autowired
    @Setter
    SimpleBeanPropertyFilter propertyFilter;

    public YamlView(String contentType) {
        super(new YAMLMapper(), contentType);
    }

    @Override
    public void setModelKey(String modelKey) {
    }

    @Override
    protected Object filterModel(Map<String, Object> model) {
        return Constant.filter(model);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ObjectMapper mapper = this.getObjectMapper();
        mapper.configure(INDENT_OUTPUT, this.format);
        SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter(FieldsPropertyFilter.FILTER_ID, propertyFilter);
        mapper.setFilterProvider(filterProvider);
        mapper.addMixIn(Object.class, PropertyFilterMixin.class);
    }
}
