package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.ProcessorContext;
import cn.procsl.ping.processor.generator.TypeAnnotationSpecBuilder;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;

import javax.annotation.processing.Generated;
import javax.lang.model.element.TypeElement;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


@AutoService(TypeAnnotationSpecBuilder.class)
public class GeneratedAnnotationBuilder implements TypeAnnotationSpecBuilder {

    static final DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.CHINA);

    @Override
    public AnnotationSpec build(ProcessorContext context, TypeElement source) {
        AnnotationSpec generator = AnnotationSpec.builder(Generated.class)
            .addMember("value", "{$S}", format.format(new Date()))
            .build();
        return generator;
    }
}
