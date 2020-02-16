package cn.procsl.ping.web.component.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Map;

/**
 * @author procsl
 * @date 2020/01/01
 */
public class JsonView extends MappingJackson2JsonView {

    @Autowired
    SimpleBeanPropertyFilter propertyFilter;

    @Override
    protected Object filterModel(Map<String, Object> model) {
        return Constant.filter(model);
    }

    public JsonView(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}
