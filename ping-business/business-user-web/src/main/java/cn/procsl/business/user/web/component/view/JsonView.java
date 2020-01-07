package cn.procsl.business.user.web.component.view;

import cn.procsl.business.user.web.component.Constant;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Map;

/**
 * @author procsl
 * @date 2020/01/01
 */
public class JsonView extends MappingJackson2JsonView {

    @Override
    protected Object filterModel(Map<String, Object> model) {
        return Constant.filter(model);
    }

}
