package cn.procsl.ping.web.component.view;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.web.servlet.view.xml.MappingJackson2XmlView;

import java.util.Map;

/**
 * @author procsl
 * @date 2020/01/01
 */
public class XmlView extends MappingJackson2XmlView {

    @Override
    protected Object filterModel(Map<String, Object> model) {
        return Constant.filter(model);
    }

    public XmlView(XmlMapper xmlMapper) {
        super(xmlMapper);
    }

}
