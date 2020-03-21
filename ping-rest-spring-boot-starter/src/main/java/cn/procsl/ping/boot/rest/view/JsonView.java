package cn.procsl.ping.boot.rest.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Map;

/**
 * @author procsl
 * @date 2020/01/01
 */
public class JsonView extends MappingJackson2JsonView {

    private final String key;

    public JsonView(ObjectMapper objectMapper, String key) {
        super(objectMapper);
        this.key = key;
    }

    @Override
    protected Object filterModel(Map<String, Object> model) {
        return model.get(key);
    }
}
