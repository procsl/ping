package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.ProcessorContext;
import cn.procsl.ping.processor.generator.AnnotationSpecBuilder;
import cn.procsl.ping.processor.generator.CodeType;
import cn.procsl.ping.processor.generator.TargetElement;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.ws.rs.*;
import java.util.Arrays;
import java.util.Collection;

@AutoService(AnnotationSpecBuilder.class)
public class RequestMappingAnnotationBuilder implements AnnotationSpecBuilder {


    protected final String requestMapping = "org.springframework.web.bind.annotation.RequestMapping";

    @Override
    public <T extends Element> AnnotationSpec build(ProcessorContext context, @Nullable T source, TargetElement type) {

        // 生成类的RequestMapping
        if (source instanceof TypeElement && type == TargetElement.TYPE) {
            AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.bestGuess(this.requestMapping));

            String prefix = context.getConfig("api.prefix");
            String api = getPath(prefix, source);

            builder.addMember("path", "$S", api);
            return builder.build();
        }

        // 生成方法的RequestMapping
        if (source instanceof ExecutableElement && type == TargetElement.METHOD) {
            AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.bestGuess(this.requestMapping));

            String api = getPath("", source);

            builder.addMember("path", "$S", api);

            String method = getMethod((ExecutableElement) source);

            builder.addMember("method", "$N", method);

            return builder.build();
        }

        context.getMessager().printMessage(Diagnostic.Kind.WARNING, "传入的类型错误， 无法生成 RequestMapping 注解!");
        return null;
    }

    @Override
    public Collection<TargetElement> supportTargetElements() {
        return Arrays.asList(TargetElement.METHOD, TargetElement.TYPE);
    }

    @Override
    public CodeType supportCodeType() {
        return CodeType.CONTROLLER;
    }

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


}
