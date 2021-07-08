package cn.procsl.ping.processor.api.builder;

import cn.procsl.ping.processor.api.AbstractAnnotationSpecBuilder;
import cn.procsl.ping.processor.api.AnnotationSpecBuilder;
import cn.procsl.ping.processor.api.ProcessorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;

@AutoService(AnnotationSpecBuilder.class)
public class DTOAnnotationSpecBuilder extends AbstractAnnotationSpecBuilder<TypeSpec.Builder> {

    final String validate = "org.springframework.validation.annotation.Validated";

    @Override
    protected boolean isType(String type) {
        return "DTO".equals(type);
    }


    @Override
    protected <E extends Element> void buildTargetAnnotation(ProcessorContext context, E source, TypeSpec.Builder target) {
        ClassName getter = ClassName.bestGuess("lombok.Getter");
        ClassName setter = ClassName.bestGuess("lombok.Setter");
        AnnotationSpec getterBuilder = AnnotationSpec.builder(getter).build();
        AnnotationSpec setterBuilder = AnnotationSpec.builder(setter).build();
        target.addAnnotation(getterBuilder);
        target.addAnnotation(setterBuilder);
        AnnotationSpec validateAnnotation = AnnotationSpec.builder(ClassName.bestGuess(validate)).build();
        target.addAnnotation(validateAnnotation);
    }

    @Override
    protected Class<TypeSpec.Builder> target() {
        return TypeSpec.Builder.class;
    }
}
