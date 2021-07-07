package cn.procsl.ping.processor.api.builder;

import cn.procsl.ping.processor.api.AbstractAnnotationSpecBuilder;
import cn.procsl.ping.processor.api.AnnotationSpecBuilder;
import cn.procsl.ping.processor.api.ProcessorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Element;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


@AutoService(AnnotationSpecBuilder.class)
public class GeneratedAnnotationBuilder extends AbstractAnnotationSpecBuilder<TypeSpec.Builder> {

    static final DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.CHINA);


    @Override
    protected boolean isType(String type) {
        return "CONTROLLER".equals(type);
    }

    @Override
    protected <E extends Element> void buildTargetAnnotation(ProcessorContext context, E source, TypeSpec.Builder target) {
        AnnotationSpec generator = AnnotationSpec
            .builder(Generated.class)
            .addMember("value", "{$S}", format.format(new Date()))
            .build();
        target.addAnnotation(generator);
    }

    @Override
    protected Class<TypeSpec.Builder> target() {
        return TypeSpec.Builder.class;
    }

}
