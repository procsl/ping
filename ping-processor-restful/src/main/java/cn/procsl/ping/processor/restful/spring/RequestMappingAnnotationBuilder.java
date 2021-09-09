package cn.procsl.ping.processor.restful.spring;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.ws.rs.*;

class RequestMappingAnnotationBuilder {


    protected final String requestMapping = "org.springframework.web.bind.annotation.RequestMapping";

    protected String getPath(String prefix, Element typeElement) {
        prefix = prefix == null ? "" : prefix.trim();
        Path path = typeElement.getAnnotation(Path.class);
        String api;
        if (path == null) {
            api = prefix + "/";
        } else {
            api = (prefix + "/" + path.value().trim());
        }
        api = api.replaceAll("/+", "/").trim();
        return String.format("%s", api);
    }

    protected String getMethod(ExecutableElement item) {
        GET get = item.getAnnotation(GET.class);
        if (get != null) {
            return "org.springframework.web.bind.annotation.RequestMethod.GET";
        }

        POST post = item.getAnnotation(POST.class);
        if (post != null) {
            return "org.springframework.web.bind.annotation.RequestMethod.POST";
        }

        DELETE delete = item.getAnnotation(DELETE.class);
        if (delete != null) {
            return "org.springframework.web.bind.annotation.RequestMethod.DELETE";
        }

        PATCH PATCH = item.getAnnotation(PATCH.class);
        if (PATCH != null) {
            return "org.springframework.web.bind.annotation.RequestMethod.PATCH";
        }

        PUT put = item.getAnnotation(PUT.class);
        if (put != null) {
            return "org.springframework.web.bind.annotation.RequestMethod.PUT";
        }

        HEAD head = item.getAnnotation(HEAD.class);
        if (head != null) {
            return "org.springframework.web.bind.annotation.RequestMethod.HEAD";
        }

        OPTIONS options = item.getAnnotation(OPTIONS.class);
        if (options != null) {
            return "org.springframework.web.bind.annotation.RequestMethod.OPTIONS}";
        }

        return "org.springframework.web.bind.annotation.RequestMethod.GET";
    }


    public AnnotationSpec builder(String prefix, Element element) {

        AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.bestGuess(this.requestMapping));

        String api = getPath(prefix, element);

        builder.addMember("path", "$S", api.replaceAll("/$", ""));

        return builder.build();
    }

    public AnnotationSpec builder(Element element) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.bestGuess(this.requestMapping));

        String api = getPath("", element);

        builder.addMember("path", "$S", api.replaceAll("/$", ""));

        String method = getMethod((ExecutableElement) element);

        builder.addMember("method", "$N", method);

        return builder.build();
    }

}
