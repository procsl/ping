package cn.procsl.ping.processor.builder;


import cn.procsl.ping.processor.ProcessorContext;
import com.google.auto.service.AutoService;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;


@AutoService(AnnotationSpecBuilder.class)
public class RequestParamAnnotationBuilder implements AnnotationSpecBuilder {

    final static String requestParam = "org.springframework.web.bind.annotation.RequestParam";

    @Override
    public <T extends Element> void build(ProcessorContext context, @Nullable T source, Object target, String type) {

    }
}
