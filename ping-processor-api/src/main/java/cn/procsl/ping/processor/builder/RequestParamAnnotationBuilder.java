package cn.procsl.ping.processor.builder;


import cn.procsl.ping.processor.ProcessorContext;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;


public class RequestParamAnnotationBuilder implements AnnotationSpecBuilder {

    final static String requestParam = "org.springframework.web.bind.annotation.RequestParam";

    public RequestParamAnnotationBuilder(VariableElement param) {
    }

    @Override
    public <T extends Element> void build(ProcessorContext context, @Nullable T source, Object target, String type) {

    }
}
