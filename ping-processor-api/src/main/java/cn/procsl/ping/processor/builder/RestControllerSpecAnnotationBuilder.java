package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.ProcessorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;

@AutoService(AnnotationSpecBuilder.class)
public class RestControllerSpecAnnotationBuilder implements AnnotationSpecBuilder {


    @Override
    public <T extends Element> void build(ProcessorContext context, @Nullable T source, Object target, String type) {
        AnnotationSpec tmp = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.RestController")).build();
        if (target instanceof TypeSpec.Builder) {
            ((TypeSpec.Builder) target).addAnnotation(tmp);
        }
    }
}
