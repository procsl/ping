package cn.procsl.ping.processor.api.builder;


import cn.procsl.ping.processor.api.AbstractAnnotationSpecBuilder;
import cn.procsl.ping.processor.api.AnnotationSpecBuilder;
import cn.procsl.ping.processor.api.ProcessorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;


@AutoService(AnnotationSpecBuilder.class)
public class RequestParamAnnotationBuilder extends AbstractAnnotationSpecBuilder<ParameterSpec.Builder> {

    final static String requestParam = "org.springframework.web.bind.annotation.RequestParam";
    final static String responseBody = "org.springframework.web.bind.annotation.RequestBody";


    @Override
    protected boolean isType(String type) {
        return "CONTROLLER".equals(type);
    }

    @Override
    protected <E extends Element> void buildTargetAnnotation(ProcessorContext context, E source, ParameterSpec.Builder target) {
        if (source instanceof VariableElement) {
            AnnotationSpec spec = AnnotationSpec
                .builder(ClassName.bestGuess(requestParam))
                .build();
            target.addAnnotation(spec);
        }

        if (source instanceof ExecutableElement) {
            AnnotationSpec spec = AnnotationSpec
                .builder(ClassName.bestGuess(responseBody))
                .build();
            target.addAnnotation(spec);
        }
    }

    @Override
    protected Class<ParameterSpec.Builder> target() {
        return ParameterSpec.Builder.class;
    }

}
