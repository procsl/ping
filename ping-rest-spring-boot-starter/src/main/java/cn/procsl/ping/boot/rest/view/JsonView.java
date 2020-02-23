package cn.procsl.ping.boot.rest.view;

import cn.procsl.ping.boot.rest.config.RestWebProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Map;

/**
 * @author procsl
 * @date 2020/01/01
 */
public class JsonView extends MappingJackson2JsonView {

    private final RestWebProperties properties;

    public JsonView(ObjectMapper objectMapper, RestWebProperties properties) {
        super(objectMapper);
        this.properties = properties;
    }

    @Override
    protected Object filterModel(Map<String, Object> model) {
        return model.get(properties.getModelKey());
    }
}
