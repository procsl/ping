package cn.procsl.business.user.web.components.view;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.xml.MappingJackson2XmlView;

import java.util.Map;

/**
 * @author procsl
 * @date 2020/01/01
 */
public class XmlView extends MappingJackson2XmlView {


    @Setter
    @Value("${ping.business.web.errorKey:__error_key__}")
    protected String errorKey;

    @Setter
    @Value("${ping.business.web.returnKey:__return_key__}")
    protected String returnKey;

    @Override
    protected Object filterModel(Map<String, Object> model) {
        if (model.containsKey(this.returnKey)) {
            return model.get(this.returnKey);
        }

        if (model.containsKey(errorKey)) {
            return model.get(this.errorKey);
        }
        return super.filterModel(model);
    }
}
