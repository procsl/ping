package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.ProcessorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.ws.rs.*;

@AutoService(AnnotationSpecBuilder.class)
public class RequestMappingAnnotationBuilder implements AnnotationSpecBuilder {


    protected final String requestMapping = "org.springframework.web.bind.annotation.RequestMapping";

    protected String getPath(String prefix, Element typeElement) {
        prefix = prefix == null ? "" : prefix.trim();
        Path path = typeElement.getAnnotation(Path.class);
        String api;
        if (path == null) {
            api = prefix + "/";
        } else {
            api = (prefix + path.value().trim());
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


    @Override
    public <T extends Element> void build(ProcessorContext context, @Nullable T source, Object target, String type) {

        if (target instanceof TypeSpec.Builder) {
            AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.bestGuess(this.requestMapping));

            String prefix = context.getConfig("api.prefix");
            String api = getPath(prefix, source);

            builder.addMember("path", "$S", api);
            ((TypeSpec.Builder) target).addAnnotation(builder.build());
        }

        if (target instanceof MethodSpec.Builder) {

            AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.bestGuess(this.requestMapping));

            String api = getPath("", source);

            builder.addMember("path", "$S", api);

            String method = getMethod((ExecutableElement) source);

            builder.addMember("method", "$N", method);

            AnnotationSpec tmp = builder.build();
            ((MethodSpec.Builder) target).addAnnotation(tmp);
        }
    }
}
