package cn.procsl.ping.processor.builder;


import cn.procsl.ping.processor.ProcessorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.element.Element;


@AutoService(AnnotationSpecBuilder.class)
public class RequestParamAnnotationBuilder extends AbstractAnnotationSpecBuilder<ParameterSpec.Builder> {

    final static String requestParam = "org.springframework.web.bind.annotation.RequestParam";


    @Override
    protected boolean isType(String type) {
        return "CONTROLLER".equals(type);
    }

    @Override
    protected <E extends Element> void buildTargetAnnotation(ProcessorContext context, E source, ParameterSpec.Builder target) {

    }

    @Override
    protected Class<ParameterSpec.Builder> target() {
        return ParameterSpec.Builder.class;
    }

}
