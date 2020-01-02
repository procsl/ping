package cn.procsl.business.user.web.components.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.Locale;

/**
 * @author procsl
 * @date 2020/01/01
 */
public class JsonViewResolver implements ViewResolver {

    @Qualifier("jsonView")
    @Autowired
    View view;

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        return view;
    }
}
