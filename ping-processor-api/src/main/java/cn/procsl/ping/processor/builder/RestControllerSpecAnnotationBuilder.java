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
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.Collections;

@AutoService(AnnotationSpecBuilder.class)
public class RestControllerSpecAnnotationBuilder implements AnnotationSpecBuilder {

    @Override
    public <T extends Element> AnnotationSpec build(ProcessorContext context, @Nullable T source, TargetElement type) {
        return AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.RestController")).build();
    }

    @Override
    public Collection<TargetElement> supportTargetElements() {
        return Collections.singleton(TargetElement.TYPE);
    }

    @Override
    public CodeType supportCodeType() {
        return CodeType.CONTROLLER;
    }
}
