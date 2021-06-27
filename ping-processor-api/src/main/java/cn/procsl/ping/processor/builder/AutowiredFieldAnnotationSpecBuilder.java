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
import java.util.Collection;
import java.util.Collections;

@AutoService(value = AnnotationSpecBuilder.class)
public class AutowiredFieldAnnotationSpecBuilder implements AnnotationSpecBuilder {

    @Override
    public <T extends Element> AnnotationSpec build(ProcessorContext context, @Nullable T source, TargetElement type) {
        ClassName clazz = ClassName.bestGuess("org.springframework.beans.factory.annotation.Autowired");
        return AnnotationSpec.builder(clazz).addMember("required", "true").build();
    }

    @Override
    public Collection<TargetElement> supportTargetElements() {
        return Collections.singleton(TargetElement.FIELD);
    }

    @Override
    public CodeType supportCodeType() {
        return CodeType.CONTROLLER;
    }
}
