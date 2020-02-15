package cn.procsl.ping.web.component.view;

import cn.procsl.ping.web.serializable.PropertyFilterMixin;
import cn.procsl.ping.web.serializable.SerializableFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Map;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

/**
 * @author procsl
 * @date 2020/01/01
 */
public class JsonView extends MappingJackson2JsonView implements InitializingBean {

    @Setter
    @Value("${ping.business.view.output.format:true}")
    protected boolean format;

    @Autowired
    SimpleBeanPropertyFilter propertyFilter;

    @Override
    protected Object filterModel(Map<String, Object> model) {
        return Constant.filter(model);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ObjectMapper mapper = this.getObjectMapper();
        mapper.configure(INDENT_OUTPUT, this.format);
        SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter(SerializableFilter.FILTER_ID, propertyFilter);
        mapper.setFilterProvider(filterProvider);
        mapper.addMixIn(Object.class, PropertyFilterMixin.class);
    }

}
