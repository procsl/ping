package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.GeneratorContext;
import cn.procsl.ping.processor.generator.TypeAnnotationSpecBuilder;
import com.squareup.javapoet.AnnotationSpec;

import javax.annotation.processing.Generated;
import javax.lang.model.element.TypeElement;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class GeneratedAnnotationBuilder implements TypeAnnotationSpecBuilder {

    static final DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.CHINA);

    @Override
    public AnnotationSpec build(GeneratorContext context, TypeElement source) {
        AnnotationSpec generator = AnnotationSpec.builder(Generated.class)
            .addMember("value", "{$S}", format.format(new Date()))
            .build();
        return generator;
    }
}
