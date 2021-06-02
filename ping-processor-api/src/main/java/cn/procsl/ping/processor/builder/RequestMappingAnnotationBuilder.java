package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.model.AnnotationModel;
import cn.procsl.ping.processor.model.NamingModel;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.ws.rs.*;
import java.util.HashMap;

public class RequestMappingAnnotationBuilder extends AnnotationModel {

    public RequestMappingAnnotationBuilder(String prefix, TypeElement typeElement) {
        this("RequestMapping");
        HashMap<String, String> map = new HashMap<>();
        this.setValueMap(map);
        String api = getPath(prefix, typeElement);
        map.put("path", api);
    }

    private String getPath(String prefix, Element typeElement) {
        prefix = prefix == null ? "" : prefix.trim();
        Path path = typeElement.getAnnotation(Path.class);
        String api;
        if (path == null) {
            api = prefix + "/";
        } else {
            api = (prefix + path.value().trim());
        }
        api = api.replaceAll("/+", "/").trim();
        return String.format("{%s}", api);
    }

    public RequestMappingAnnotationBuilder(ExecutableElement item) {
        this("RequestMapping");
        HashMap<String, String> map = new HashMap<>();
        this.setValueMap(map);
        String api = getPath("", item);
        map.put("path", api);
        String method = getMethod(item);
        map.put("method", method);
    }

    private String getMethod(ExecutableElement item) {
        GET get = item.getAnnotation(GET.class);
        if (get != null) {
            return "{org.springframework.web.bind.annotation.RequestMethod.GET}";
        }

        POST post = item.getAnnotation(POST.class);
        if (post != null) {
            return "{org.springframework.web.bind.annotation.RequestMethod.POST}";
        }

        DELETE delete = item.getAnnotation(DELETE.class);
        if (delete != null) {
            return "{org.springframework.web.bind.annotation.RequestMethod.DELETE}";
        }

        PATCH PATCH = item.getAnnotation(PATCH.class);
        if (PATCH != null) {
            return "{org.springframework.web.bind.annotation.RequestMethod.PATCH}";
        }

        PUT put = item.getAnnotation(PUT.class);
        if (put != null) {
            return "{org.springframework.web.bind.annotation.RequestMethod.PUT}";
        }

        HEAD head = item.getAnnotation(HEAD.class);
        if (head != null) {
            return "{org.springframework.web.bind.annotation.RequestMethod.HEAD}";
        }

        OPTIONS options = item.getAnnotation(OPTIONS.class);
        if (options != null) {
            return "{org.springframework.web.bind.annotation.RequestMethod.OPTIONS}";
        }

        return "{org.springframework.web.bind.annotation.RequestMethod.GET}";
    }

    private RequestMappingAnnotationBuilder(String name) {
        super(new NamingModel("org.springframework.web.bind.annotation", name));
    }

    public boolean isSimpleRequest() {
        return false;
    }
}
