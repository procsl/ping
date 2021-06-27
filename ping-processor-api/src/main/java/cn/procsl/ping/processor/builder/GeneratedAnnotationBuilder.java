package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.ProcessorContext;
import cn.procsl.ping.processor.generator.AnnotationSpecBuilder;
import cn.procsl.ping.processor.generator.CodeType;
import cn.procsl.ping.processor.generator.TargetElement;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;

import javax.annotation.Nullable;
import javax.annotation.processing.Generated;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;


@AutoService(AnnotationSpecBuilder.class)
public class GeneratedAnnotationBuilder implements AnnotationSpecBuilder {

    static final DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.CHINA);

    @Override
    public <T extends Element> AnnotationSpec build(ProcessorContext context, @Nullable T source, TargetElement type) {
        AnnotationSpec generator = AnnotationSpec.builder(Generated.class)
            .addMember("value", "{$S}", format.format(new Date()))
            .build();
        return generator;
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
