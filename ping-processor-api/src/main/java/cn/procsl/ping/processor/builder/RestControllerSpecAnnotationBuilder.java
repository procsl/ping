package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.ProcessorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;

@AutoService(AnnotationSpecBuilder.class)
public class RestControllerSpecAnnotationBuilder extends AbstractAnnotationSpecBuilder<TypeSpec.Builder> {

    @Override
    protected boolean isType(String type) {
        return "CONTROLLER".equals(type);
    }

    @Override
    protected <E extends Element> void buildTargetAnnotation(ProcessorContext context, @Nullable E source, TypeSpec.Builder target) {
        AnnotationSpec tmp = AnnotationSpec
            .builder(ClassName.bestGuess("org.springframework.web.bind.annotation.RestController"))
            .build();
        target.addAnnotation(tmp);
    }

    @Override
    protected Class<TypeSpec.Builder> target() {
        return TypeSpec.Builder.class;
    }

}
