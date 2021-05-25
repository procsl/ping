package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.model.AnnotationModel;
import cn.procsl.ping.processor.model.NamingModel;

import javax.lang.model.element.TypeElement;
import javax.ws.rs.Path;
import java.util.HashMap;

public class RequestMappingAnnotationBuilder extends AnnotationModel {

    public RequestMappingAnnotationBuilder(String prefix, TypeElement typeElement) {
        super(new NamingModel("org.springframework.web.bind.annotation", "RequestMapping"));

        prefix = prefix == null ? "" : prefix.trim();

        HashMap<String, String> map = new HashMap<>();
        Path path = typeElement.getAnnotation(Path.class);
        String api;
        if (path == null) {
            api = prefix + "/";
        } else {
            api = (prefix + path.value().trim());
        }
        api = api.replaceAll("/+", "/").trim();
        map.put("path", String.format("{%s}", api));
    }

}
