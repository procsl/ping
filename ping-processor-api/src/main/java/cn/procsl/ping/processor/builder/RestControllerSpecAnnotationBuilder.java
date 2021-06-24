package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.GeneratorContext;
import cn.procsl.ping.processor.generator.TypeAnnotationSpecBuilder;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;

public class RestControllerSpecAnnotationBuilder implements TypeAnnotationSpecBuilder {

    @Override
    public AnnotationSpec build(GeneratorContext context, TypeElement source) {
        return AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.RestController")).build();
    }

}
