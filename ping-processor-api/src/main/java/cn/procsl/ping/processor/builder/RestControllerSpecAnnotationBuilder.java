package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.ProcessorContext;
import cn.procsl.ping.processor.generator.TypeAnnotationSpecBuilder;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;

@AutoService(TypeAnnotationSpecBuilder.class)
public class RestControllerSpecAnnotationBuilder implements TypeAnnotationSpecBuilder {

    @Override
    public AnnotationSpec build(ProcessorContext context, TypeElement source) {
        return AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.RestController")).build();
    }

}
