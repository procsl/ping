package cn.procsl.business.user.web.component.view;

import cn.procsl.business.user.web.component.Constant;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.github.bohnman.squiggly.filter.SquigglyPropertyFilter;
import com.github.bohnman.squiggly.filter.SquigglyPropertyFilterMixin;
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

    public JsonView() {
        super(new JsonMapper());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        JsonMapper mapper = (JsonMapper) this.getObjectMapper();
        mapper.configure(INDENT_OUTPUT, this.format);
        SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter(SquigglyPropertyFilter.FILTER_ID, propertyFilter);
        mapper.setFilterProvider(filterProvider);
        mapper.addMixIn(Object.class, SquigglyPropertyFilterMixin.class);
    }

}
