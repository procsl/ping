package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.ProcessorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;

import javax.annotation.Nullable;
import javax.annotation.processing.Generated;
import javax.lang.model.element.Element;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


@AutoService(AnnotationSpecBuilder.class)
public class GeneratedAnnotationBuilder implements AnnotationSpecBuilder {

    static final DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.CHINA);

    @Override
    public <T extends Element> void build(ProcessorContext context, @Nullable T source, Object target, String type) {
        AnnotationSpec generator = AnnotationSpec.builder(Generated.class)
            .addMember("value", "{$S}", format.format(new Date()))
            .build();

    }
}
